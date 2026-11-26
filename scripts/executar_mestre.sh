#!/usr/bin/env bash
# Executa o Processo Mestre (ID 0)
# Uso: ./scripts/executar_mestre.sh [operacao] [voltas] [args_extras...]
# Padrão: operacao=0 (+1), voltas=1
DIR_SCRIPT="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
ROOT_DIR="$(dirname "$DIR_SCRIPT")"
cd "$ROOT_DIR"

OP=${1:-0}
VOLTAS=${2:-1}
shift 2 2>/dev/null || true

echo "Iniciando Mestre (ID 0) | Operacao: $OP | Voltas: $VOLTAS..."
java -cp bin:src Mestre 0 "$OP" "$VOLTAS" "$@"
