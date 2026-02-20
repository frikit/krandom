#!/bin/bash
# Run all pre-commit checks: format, license headers, tests, and coverage verification.
# Usage: ./scripts/pre_commit_check.sh
# Exit code: 0 = all passed, non-zero = something failed.

set -euo pipefail

REPO_ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
GRADLEW="${REPO_ROOT}/gradlew"

cd "${REPO_ROOT}"

# ── Helpers ───────────────────────────────────────────────────────────────────

GREEN="\033[0;32m"
RED="\033[0;31m"
RESET="\033[0m"

PASS="${GREEN}[PASS]${RESET}"
FAIL="${RED}[FAIL]${RESET}"

step() { echo; echo "==> $*"; }
ok()   { echo -e "${PASS} $*"; }
fail() { echo -e "${FAIL} $*"; }

# ── Steps ─────────────────────────────────────────────────────────────────────

step "Apply code formatting and license headers (spotlessApply)"
if "${GRADLEW}" spotlessApply --quiet; then
    ok "Formatting applied"
else
    fail "spotlessApply failed"
    exit 1
fi

step "Verify formatting and license headers (spotlessCheck)"
if "${GRADLEW}" spotlessCheck --quiet; then
    ok "Formatting OK"
else
    fail "spotlessCheck failed — run ./gradlew spotlessApply and re-check"
    exit 1
fi

step "Compile all modules"
if "${GRADLEW}" classes testClasses --quiet; then
    ok "Compilation OK"
else
    fail "Compilation failed"
    exit 1
fi

step "Run tests with coverage report"
if "${GRADLEW}" :core:test --quiet; then
    ok "Tests passed"
else
    fail "Tests failed — see core/build/reports/tests/test/index.html"
    exit 1
fi

step "Enforce coverage thresholds (80% line, 80% branch)"
if "${GRADLEW}" :core:jacocoTestCoverageVerification --quiet; then
    ok "Coverage thresholds met"
else
    fail "Coverage below threshold — see core/build/jacoco/html/index.html"
    exit 1
fi

# ── Coverage report ───────────────────────────────────────────────────────────

step "Coverage report"
CSV="${REPO_ROOT}/core/build/jacoco/jacoco.csv"

# Pass ANSI codes into awk via printf to avoid shell interpretation issues
G="$(printf '\033[0;32m')"
R="$(printf '\033[0;31m')"
RST="$(printf '\033[0m')"

# ── Summary box ───────────────────────────────────────────────────────────────
awk -F',' -v G="${G}" -v R="${R}" -v RST="${RST}" '
function bar(pct,    w, filled, i, b) {
    w = 20; filled = int(pct / 5 + 0.5); b = ""
    for (i = 1; i <= w; i++) b = b (i <= filled ? "#" : "-")
    return b
}
NR == 1 { next }
{ tlm += $8+0; tlc += $9+0; tbm += $6+0; tbc += $7+0 }
END {
    tl = tlm + tlc; tb = tbm + tbc
    lp = (tl > 0) ? tlc * 100 / tl : 0
    bp = (tb > 0) ? tbc * 100 / tb : 0

    lp_col = (lp >= 80 ? G : R) sprintf("%5.1f%%", lp) RST
    bp_col = (bp >= 80 ? G : R) sprintf("%5.1f%%", bp) RST
    ls_col = (lp >= 80 ? G "PASS" RST : R "FAIL" RST)
    bs_col = (bp >= 80 ? G "PASS" RST : R "FAIL" RST)

    print ""
    print "╔══════════════════════════════════════════════════════════════╗"
    print "║              CODE COVERAGE SUMMARY                          ║"
    print "╠══════════════════════════════════════════════════════════════╣"
    printf "║  Line   coverage : %s  [%s]           ║\n", lp_col, bar(lp)
    printf "║  Branch coverage : %s  [%s]           ║\n", bp_col, bar(bp)
    print  "║  Threshold       :  80.0%  minimum (both counters)         ║"
    printf "║  Line   status   :  %s                                   ║\n", ls_col
    printf "║  Branch status   :  %s                                   ║\n", bs_col
    print  "╠══════════════════════════════════════════════════════════════╣"
    printf "║  Lines  covered  : %5d / %-5d                            ║\n", tlc, tl
    printf "║  Branch covered  : %5d / %-5d                            ║\n", tbc, tb
    print  "╚══════════════════════════════════════════════════════════════╝"
}' "${CSV}"

# ── Per-package table (sorted alphabetically) ─────────────────────────────────
# fmt returns exactly 7 visible chars:
#   numbers  ->  sprintf("%6.1f", v) + mark  e.g. " 100.0 " or "  63.1!"
#   N/A      ->  "   N/A "
echo ""
printf "%-55s %7s   %7s\n" "PACKAGE" "LINE%" "BRANCH%"
printf '─%.0s' {1..73}; echo ""

awk -F',' -v G="${G}" -v R="${R}" -v RST="${RST}" '
function fmt(v,    c, mark) {
    # v < 0 means no branchable code in this package (N/A in JaCoCo).
    # Treat as 100.0: no branches means nothing can be missed.
    # Width must match the number path: sprintf("%6.1f") = 6 chars + 1 mark = 7 total.
    if (v < 0) return G " 100.0 " RST
    c    = (v >= 80) ? G : R
    mark = (v >= 80) ? " " : "!"
    return c sprintf("%6.1f", v) mark RST
}
NR == 1 { next }
{
    pkg_lm[$2] += $8+0; pkg_lc[$2] += $9+0; pkg_bm[$2] += $6+0; pkg_bc[$2] += $7+0
    tlm += $8+0; tlc += $9+0; tbm += $6+0; tbc += $7+0
}
END {
    for (p in pkg_lc) {
        pl = pkg_lm[p] + pkg_lc[p]; pb = pkg_bm[p] + pkg_bc[p]
        lpp = (pl > 0) ? pkg_lc[p] * 100 / pl : -1
        bpp = (pb > 0) ? pkg_bc[p] * 100 / pb : -1
        printf "%s\t%-55s %s   %s\n", p, p, fmt(lpp), fmt(bpp)
    }
    tl = tlm + tlc; tb = tbm + tbc
    tlp = (tl > 0) ? tlc * 100 / tl : -1
    tbp = (tb > 0) ? tbc * 100 / tb : -1
    printf "ZZZTOTAL\t%-55s %s   %s\n", "TOTAL", fmt(tlp), fmt(tbp)
}' "${CSV}" | sort | cut -f2- | awk '
/^TOTAL/ { total = $0; next }
        { print }
END     { printf "%-73s\n", "─────────────────────────────────────────────────────────────────────────"; print total }'

echo ""
printf "! = below 80%% threshold\n"

# ── Summary ───────────────────────────────────────────────────────────────────

echo
echo -e "${GREEN}============================================"
echo -e " All pre-commit checks passed. Safe to commit."
echo -e "============================================${RESET}"
