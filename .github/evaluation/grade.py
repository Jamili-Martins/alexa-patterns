#!/usr/bin/env python3
import argparse
import hashlib
import html
import json
import re
import shutil
import subprocess
import sys
import xml.etree.ElementTree as ET
from decimal import Decimal, ROUND_HALF_UP
from pathlib import Path


MARKER = "<!-- avaliador-alexapatterns -->"
COUNTERS = ("passed", "failed", "errors", "skipped")


def load_rubric():
    raw = Path(__file__).with_name("rubric.json").read_bytes()
    rubric = json.loads(raw)
    rules = rubric["requisitos"]
    classes = [r for r in rules if "classe" in r] + rubric["obrigatorios"]
    if sum(r["peso"] for r in rules) != 100:
        raise ValueError("Os pesos precisam somar 100.")
    if len({r["classe"] for r in classes}) != len(classes):
        raise ValueError("Há classes repetidas na configuração.")
    for rule in classes:
        if not re.fullmatch(r"(?:[A-Za-z_]\w*\.)+[A-Za-z_]\w*", rule["classe"]):
            raise ValueError("Nome de classe inválido na configuração.")
        if type(rule["testesEsperados"]) is not int or rule["testesEsperados"] < 1:
            raise ValueError("Quantidade esperada inválida.")
    return rubric, classes, hashlib.sha256(raw).hexdigest()


def empty_report(classes, digest, pr, revision):
    return {
        "schema": 1,
        "rubricSha256": digest,
        "pr": pr,
        "revision": revision,
        "stages": {"validate": None, "compile": None, "tests": None},
        "violations": None,
        "classes": {r["classe"]: dict.fromkeys(COUNTERS, 0) for r in classes},
        "problems": [],
    }


def read_xml(path):
    root = ET.parse(path).getroot()
    for node in root.iter():
        node.tag = node.tag.rsplit("}", 1)[-1]
    return root


def read_checkstyle(project, report):
    try:
        root = read_xml(project / "target/checkstyle-result.xml")
        if root.tag != "checkstyle":
            raise ValueError("Formato inesperado")
        report["violations"] = sum(1 for _ in root.iter("error"))
    except (OSError, ET.ParseError, ValueError):
        report["problems"].append("Relatório do Checkstyle ausente ou inválido.")
        return
    if (report["stages"]["validate"] == 0) != (report["violations"] == 0):
        report["problems"].append("Resultado do Maven incompatível com o Checkstyle.")


def read_tests(project, classes, report):
    expected = {r["classe"]: r for r in classes}
    seen = set()
    for path in sorted((project / "target/surefire-reports").glob("TEST-*.xml")):
        try:
            root = read_xml(path)
        except (OSError, ET.ParseError):
            report["problems"].append("Relatório XML do Surefire inválido.")
            continue
        for suite in root.iter("testsuite"):
            for case in suite.findall("testcase"):
                hints = {
                    suite.get("name"), case.get("classname"), path.stem[5:]
                } & expected.keys()
                if len(hints) != 1:
                    report["problems"].append("Caso de teste sem classe oficial inequívoca.")
                    continue
                name = hints.pop()
                key = (name, case.get("name"))
                if key in seen:
                    report["problems"].append(f"Teste duplicado em {expected[name]['titulo']}.")
                    continue
                seen.add(key)
                status = next(
                    (kind for tag, kind in (
                        ("error", "errors"), ("failure", "failed"), ("skipped", "skipped")
                    ) if case.find(tag) is not None),
                    "passed",
                )
                report["classes"][name][status] += 1
    for rule in classes:
        count = sum(report["classes"][rule["classe"]].values())
        if count != rule["testesEsperados"]:
            report["problems"].append(
                f"{rule['titulo']}: esperados {rule['testesEsperados']} testes; encontrados {count}."
            )
    failed = sum(c["failed"] + c["errors"] for c in report["classes"].values())
    if report["stages"]["tests"] != 0 and failed == 0:
        report["problems"].append("Maven falhou sem registrar falhas nos testes oficiais.")


def copy_submission(project, submission, paths):
    pending = []
    for item in paths:
        relative = Path(item)
        source = submission / relative
        destination = project / relative
        if (relative.is_absolute() or ".." in relative.parts
                or not source.is_file()
                or any(p.is_symlink() for p in [source, *source.parents]
                       if p != submission.parent)
                or not source.resolve().is_relative_to(submission)
                or not destination.resolve().is_relative_to(project)):
            raise ValueError("Arquivo permitido ausente ou caminho inválido na entrega.")
        pending.append((destination, source.read_bytes()))
    for destination, content in pending:
        destination.write_bytes(content)


def run_maven(project, executable, arguments, log):
    command = [executable, "-B", "-ntp", *arguments]
    log.write("\n$ " + " ".join(command) + "\n")
    log.flush()
    return subprocess.run(
        command, cwd=project, stdout=log, stderr=subprocess.STDOUT,
        timeout=600, check=False,
    ).returncode


def evaluate(args, rubric, classes, digest):
    project = args.project.resolve()
    output = args.output.resolve()
    report = empty_report(classes, digest, args.pr, args.revision)
    output.mkdir(parents=True, exist_ok=True)
    for name in ("grade.json", "grade.md"):
        (output / name).unlink(missing_ok=True)
    try:
        target = project / "target"
        if output.is_relative_to(target) or target.is_symlink():
            raise ValueError("A saída deve ficar fora de target, que não pode ser um link.")
        if args.submission:
            copy_submission(project, args.submission.resolve(), rubric["arquivosPermitidos"])
        if target.exists():
            shutil.rmtree(target)
        with (output / "maven.log").open("w", encoding="utf-8") as log:
            stages = report["stages"]
            stages["validate"] = run_maven(project, args.maven, ["validate"], log)
            read_checkstyle(project, report)
            if stages["validate"] == 0 and not report["problems"]:
                stages["compile"] = run_maven(project, args.maven, ["test-compile"], log)
                if stages["compile"] == 0:
                    selected = ",".join(r["classe"].rsplit(".", 1)[-1] for r in classes)
                    stages["tests"] = run_maven(project, args.maven, [f"-Dtest={selected}", "test"], log)
                    read_tests(project, classes, report)
    except (OSError, ValueError, subprocess.TimeoutExpired) as error:
        report["problems"].append("Não foi possível concluir a execução da avaliação.")
        with (output / "maven.log").open("a", encoding="utf-8") as log:
            log.write(f"\n{type(error).__name__}: {error}\n")
    return report


def validate_report(report, classes, digest):
    def integer(value, minimum=0, maximum=10000):
        return type(value) is int and minimum <= value <= maximum

    if (report.get("schema") != 1 or report.get("rubricSha256") != digest
            or not integer(report.get("pr"), maximum=10**9)
            or not isinstance(report.get("revision"), str)
            or not re.fullmatch(r"(?:[0-9a-f]{40})?", report["revision"])):
        raise ValueError("Metadados do relatório inválidos ou configuração desatualizada.")
    stages = report.get("stages", {})
    if set(stages) != {"validate", "compile", "tests"} or any(
        value is not None and not integer(value, -1000, 1000) for value in stages.values()
    ):
        raise ValueError("Etapas inválidas no relatório.")
    if report.get("violations") is not None and not integer(report["violations"]):
        raise ValueError("Contagem de estilo inválida.")
    if set(report.get("classes", {})) != {r["classe"] for r in classes}:
        raise ValueError("Classes incompatíveis com a configuração oficial.")
    for counts in report["classes"].values():
        if set(counts) != set(COUNTERS) or not all(integer(v) for v in counts.values()):
            raise ValueError("Contagens inválidas no relatório.")
    problems = report.get("problems")
    if (not isinstance(problems, list) or len(problems) > 100
            or not all(isinstance(p, str) and len(p) <= 300 for p in problems)):
        raise ValueError("Diagnósticos inválidos no relatório.")


def safe_text(value):
    text = html.escape(value.replace("\n", " ").replace("\r", " "))
    return re.sub(r"([\\`*_{}\[\]()#+.!|@])", r"\\\1", text)


def build_result(rubric, classes, report):
    problems = list(report["problems"])
    stages = report["stages"]
    lint_ok = stages["validate"] == 0 and report["violations"] == 0
    ran_tests = stages["tests"] is not None
    if stages["validate"] is None:
        problems.append("A validação do projeto não foi concluída.")
    elif report["violations"] is None:
        problems.append("Não há resultado válido do Checkstyle.")
    elif stages["validate"] != 0 and report["violations"] == 0:
        problems.append("Falha de execução na validação do projeto.")
    if lint_ok and stages["compile"] is None:
        problems.append("A compilação não foi concluída.")
    if stages["compile"] == 0 and not ran_tests:
        problems.append("A execução dos testes não foi concluída.")
    if ran_tests:
        if not lint_ok or stages["compile"] != 0:
            problems.append("Sequência de etapas inválida.")
        for rule in classes:
            if sum(report["classes"][rule["classe"]].values()) != rule["testesEsperados"]:
                problems.append(f"Contagem incompleta para {rule['titulo']}.")
        failures = sum(c["failed"] + c["errors"] for c in report["classes"].values())
        if (stages["tests"] == 0 and failures) or (stages["tests"] != 0 and not failures):
            problems.append("Resultado do Maven incompatível com os testes.")
    rows = []
    total = Decimal(0)
    for rule in rubric["requisitos"]:
        if rule.get("tipo") == "lint":
            ok = lint_ok
            earned = Decimal(rule["peso"] if ok else 0)
            status = "sem violações" if ok else "Checkstyle não aprovado"
        else:
            passed = report["classes"][rule["classe"]]["passed"] if ran_tests else 0
            expected = rule["testesEsperados"]
            ok = passed == expected
            earned = (Decimal(rule["peso"]) * passed / expected).quantize(
                Decimal("0.1"), rounding=ROUND_HALF_UP
            )
            status = f"{passed}/{expected} testes"
            if not ran_tests:
                status = "bloqueado pelo Checkstyle" if not lint_ok else "bloqueado na compilação"
        total += earned
        rows.append((ok, rule["titulo"], status, rule["peso"], earned))
    gates = {
        rule["titulo"]: ran_tests and report["classes"][rule["classe"]]["passed"] == rule["testesEsperados"]
        for rule in rubric["obrigatorios"]
    }
    approved = not problems and lint_ok and all(gates.values()) and total >= rubric["notaMinima"]
    result = {"nota": None if problems else float(total), "aprovado": approved,
              "codigo": 2 if problems else (0 if approved else 1)}
    title = "AVALIAÇÃO INCOMPLETA" if problems else f"{'APROVADO' if approved else 'AINDA NÃO APROVADO'}"
    score = "indisponível" if problems else f"{total:.1f}/100"
    lines = [MARKER, "## Avaliação automática — AlexaPatterns", "",
             f"**Nota: {score} — {title}** (mínimo {rubric['notaMinima']})", ""]
    if problems:
        lines += ["A avaliação não produziu uma nota confiável. Consulte os diagnósticos e o artefato do job.", ""]
    lines += ["; ".join(f"**{name}:** {'passou' if ok and not problems else 'não aprovado'}"
                        for name, ok in gates.items()) + ". Ambos são exigidos para aprovação.", "",
              "| Requisito | Resultado | Peso | Nota |", "| --- | --- | ---: | ---: |"]
    for ok, title, status, weight, earned in rows:
        lines.append(f"| {'✅' if ok and not problems else '❌'} {title} | {status} | {weight} | {'—' if problems else f'{earned:.1f}'} |")
    if problems:
        lines += ["", *[f"- {safe_text(p)}" for p in dict.fromkeys(problems)]]
    lines += ["", "*A nota é recalculada a cada push no PR. Testes ignorados não pontuam.*", ""]
    return result, "\n".join(lines)


def main():
    parser = argparse.ArgumentParser(description="Avalia o AlexaPatterns com os testes oficiais.")
    parser.add_argument("--project", type=Path, default=Path.cwd())
    parser.add_argument("--submission", type=Path)
    parser.add_argument("--output", type=Path, default=Path("evaluation-results"))
    parser.add_argument("--maven", default="mvn")
    parser.add_argument("--pr", type=int, default=0)
    parser.add_argument("--revision", default="")
    parser.add_argument("--render", type=Path, help="Somente valida e renderiza um JSON; não executa código.")
    args = parser.parse_args()
    try:
        rubric, classes, digest = load_rubric()
        if args.render:
            if args.render.stat().st_size > 100000:
                raise ValueError("Relatório excede o tamanho permitido.")
            report = json.loads(args.render.read_text(encoding="utf-8"))
        else:
            report = evaluate(args, rubric, classes, digest)
        validate_report(report, classes, digest)
        result, markdown = build_result(rubric, classes, report)
        report["resultado"] = result
        args.output.mkdir(parents=True, exist_ok=True)
        (args.output / "grade.json").write_text(
            json.dumps(report, ensure_ascii=False, indent=2) + "\n", encoding="utf-8"
        )
        (args.output / "grade.md").write_text(markdown, encoding="utf-8")
        print(markdown)
        return 0 if args.render else result["codigo"]
    except (OSError, ValueError, KeyError, TypeError) as error:
        print(f"Erro na avaliação: {error}", file=sys.stderr)
        return 2


if __name__ == "__main__":
    sys.exit(main())