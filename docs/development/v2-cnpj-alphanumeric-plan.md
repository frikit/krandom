# V2 CNPJ Alphanumeric Compatibility Plan

**Status:** Complete
**Master plan:** [Step 2.9](v2-master-implementation-plan.md#step-29--add-explicit-financial-and-identity-safety-modes)

## Stage 1: Establish the official boundary

**Goal:** Record the official CNPJ alphanumeric character, checksum, and fictitious-fixture
contracts without calling an arbitrary valid identifier safe.

**Success Criteria:** The implementation and tests use the Receita Federal's twelve-character
body, ASCII-minus-48 value mapping, Mod-11 weights, and a documented fictitious reference vector.
The scope distinguishes format compatibility from the official browser-local simulator's separate
fictitious-value contract.

**Tests:** The published fictitious simulator vector validates independently; legacy numeric CNPJ
fixtures remain valid.

**Status:** Complete

## Stage 2: Add an explicit alphanumeric compatibility mode

**Goal:** Let an explicitly unclassified `CnpjGenerator` produce formatted or bare valid
alphanumeric CNPJ shapes while keeping current numeric output stable.

**Success Criteria:** The new mode has at least one alphabetic body character, preserves the
configured formatting choice, uses the official check-digit algorithm, and remains blocked by the
default business-tax safety policy.

**Tests:** Seeded output is reproducible; formatted and bare outputs validate independently;
default configuration still throws.

**Status:** Complete

## Stage 3: Document safe-fixture limits and release readiness

**Goal:** Explain the coexistence of numeric and alphanumeric formats and state why krandom does
not claim that locally generated valid CNPJs are fictitious.

**Success Criteria:** Migration and safety documentation link to the Receita Federal technical
specification and simulator; public API inventory classifies any additive method; the full
pre-commit and consumer-example matrices pass.

**Tests:** Documentation checks, public API evolution gate, full test suite, coverage gate, and
consumer examples pass.

**Status:** Complete

## Sources

- [Receita Federal check-digit specification](https://www.gov.br/receitafederal/pt-br/centrais-de-conteudo/publicacoes/documentos-tecnicos/cnpj)
- [Receita Federal CNPJ alphanumeric FAQ](https://www.gov.br/receitafederal/pt-br/centrais-de-conteudo/publicacoes/perguntas-e-respostas/cnpj/cnpj-alfanumerico.pdf/%40%40download/file)
- [Receita Federal national CNPJ simulator](https://servicos.receitafederal.gov.br/servico/cnpj-alfa)
