#!/usr/bin/env bash
# ==============================================================================
# EXECUTA A TOPOLOGIA COMPLETA EM UMA ÚNICA MÁQUINA (LOOPBACK LOCAL)
# Ordem de inicialização:
# 1. Escravo 3 (Porta 5003)
# 2. Escravo 2 (Porta 5002)
# 3. Escravo 1 (Porta 5001)
# 4. Mestre    (Porta 5000)
# ==============================================================================
set -e

DIR_SCRIPT="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
ROOT_DIR="$(dirname "$DIR_SCRIPT")"
cd "$ROOT_DIR"

# Suporta sintaxe uniforme (OP VOLTAS) ou individual (OP_MESTRE OP_E1 OP_E2 OP_E3 VOLTAS)
if [ $# -ge 5 ]; then
    OP_MESTRE=$1
    OP_E1=$2
    OP_E2=$3
    OP_E3=$4
    VOLTAS=$5
else
    OP_MESTRE=${1:-0}
    OP_E1=${1:-0}
    OP_E2=${1:-0}
    OP_E3=${1:-0}
    VOLTAS=${2:-1}
fi

echo "================================================================================"
echo "  INICIANDO TOKEN RING LOCALMENTE (4 PROCESSOS)"
echo "  Operações: Mestre=$OP_MESTRE | E1=$OP_E1 | E2=$OP_E2 | E3=$OP_E3 | Voltas=$VOLTAS"
echo "================================================================================"

# Garante compilação atualizada
bash "$DIR_SCRIPT/compilar.sh"

echo "Iniciando Escravos em background..."
java -cp bin:src Escravo 3 "$OP_E3" "$VOLTAS" > /tmp/tokenring_escravo3.log 2>&1 &
PID_E3=$!
echo "  [PID $PID_E3] Escravo 3 iniciado (porta 5003 | op $OP_E3)"

java -cp bin:src Escravo 2 "$OP_E2" "$VOLTAS" > /tmp/tokenring_escravo2.log 2>&1 &
PID_E2=$!
echo "  [PID $PID_E2] Escravo 2 iniciado (porta 5002 | op $OP_E2)"

java -cp bin:src Escravo 1 "$OP_E1" "$VOLTAS" > /tmp/tokenring_escravo1.log 2>&1 &
PID_E1=$!
echo "  [PID $PID_E1] Escravo 1 iniciado (porta 5001 | op $OP_E1)"

sleep 1

echo "Iniciando Mestre em primeiro plano..."
java -cp bin:src Mestre 0 "$OP_MESTRE" "$VOLTAS"

wait $PID_E1 || true
wait $PID_E2 || true
wait $PID_E3 || true

echo "================================================================================"
echo "Execução local concluída! Logs gravados em /tmp/tokenring_*.log"
