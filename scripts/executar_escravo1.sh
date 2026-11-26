#!/usr/bin/env bash
# Executa o Escravo 1 (ID 1)
# Uso: ./scripts/executar_escravo1.sh [operacao] [voltas] [args_extras...]
DIR_SCRIPT="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
ROOT_DIR="$(dirname "$DIR_SCRIPT")"
cd "$ROOT_DIR"

OP=${1:-0}
VOLTAS=${2:-1}
shift 2 2>/dev/null || true

echo "Iniciando Escravo 1 (ID 1) | Operacao: $OP | Voltas: $VOLTAS..."
java -cp bin:src Escravo 1 "$OP" "$VOLTAS" "$@"
