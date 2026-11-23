#!/usr/bin/env bash
# ==============================================================================
# TESTE END-TO-END DE PROCESSOS AUTÔNOMOS DO TOKEN RING UDP
# Executa os 4 processos reais compilados como instâncias separadas do SO
# Responsável: Miqueias
# ==============================================================================
set -e

DIR_SCRIPT="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
ROOT_DIR="$(dirname "$DIR_SCRIPT")"
cd "$ROOT_DIR"

# Garante compilação atualizada
bash "$DIR_SCRIPT/compilar.sh"

echo "================================================================================"
echo "  INICIANDO BATERIA DE TESTES END-TO-END COM 4 PROCESSOS SEPARADOS DO SO"
echo "================================================================================"

executar_cenario() {
    local NOME="$1"
    local OP_MESTRE="$2"
    local OP_E1="$3"
    local OP_E2="$4"
    local OP_E3="$5"
    local VOLTAS="$6"
    local VALOR_ESPERADO="$7"

    echo "--- [CENÁRIO: $NOME] ---"
    
    # Inicia os 3 escravos em background
    java -Dtokenring.intervalo=0 -cp bin Escravo 3 "$OP_E3" "$VOLTAS" > /tmp/test_e3.log 2>&1 &
    PID_E3=$!
    java -Dtokenring.intervalo=0 -cp bin Escravo 2 "$OP_E2" "$VOLTAS" > /tmp/test_e2.log 2>&1 &
    PID_E2=$!
    java -Dtokenring.intervalo=0 -cp bin Escravo 1 "$OP_E1" "$VOLTAS" > /tmp/test_e1.log 2>&1 &
    PID_E1=$!

    sleep 0.5

    # Executa o mestre e captura saída
    SAIDA_MESTRE=$(java -Dtokenring.intervalo=0 -cp bin Mestre 0 "$OP_MESTRE" "$VOLTAS")

    wait $PID_E1
    wait $PID_E2
    wait $PID_E3

    echo "$SAIDA_MESTRE" | grep "VALOR FINAL DO TOKEN:" || true
    if echo "$SAIDA_MESTRE" | grep -q "VALOR FINAL DO TOKEN:[[:space:]]*$VALOR_ESPERADO"; then
        echo "  ==> [APROVADO] Resultado confere com o esperado: $VALOR_ESPERADO"
    else
        echo "  ==> [FALHOU] Esperado: $VALOR_ESPERADO. Saída do Mestre:"
        echo "$SAIDA_MESTRE"
        exit 1
    fi
}

# Teste 1: Todos incrementando (Op 0) por 1 volta -> Esperado: 6
executar_cenario "Teste 1: Todos Incrementando (+1)" 0 0 0 0 1 6

# Teste 2: Todos multiplicando (Op 1) por 1 volta -> Esperado: 32
executar_cenario "Teste 2: Todos Multiplicando (*2)" 1 1 1 1 1 32

# Teste 3: Duas voltas incrementando (Op 0) por 2 voltas -> Esperado: 10
executar_cenario "Teste 3: Duas Voltas Incrementando (+1)" 0 0 0 0 2 10

# Teste 4: Heterogêneo: E1(+1), E2(*2), E3(+1), Mestre(*2) -> Esperado: 14
executar_cenario "Teste 4: Operações Diferentes (Heterogêneo)" 1 0 1 0 1 14

echo ""
echo "--- [VALIDAÇÃO DE PARÂMETROS INVÁLIDOS (TESTE 5)] ---"

testar_parametro_invalido() {
    local CMD="$1"
    set +e
    $CMD > /dev/null 2>&1
    local STATUS=$?
    set -e
    if [ $STATUS -ne 0 ]; then
        echo "  [OK: Rejeitado Corretamente] $CMD (exit code: $STATUS)"
    else
        echo "  [FALHA] Comando deveria ter retornado erro: $CMD"
        exit 1
    fi
}

testar_parametro_invalido "java -cp bin Mestre"
testar_parametro_invalido "java -cp bin Mestre 1 0 1"
testar_parametro_invalido "java -cp bin Mestre 0 5 1"
testar_parametro_invalido "java -cp bin Mestre 0 0 0"
testar_parametro_invalido "java -cp bin Mestre 0 0 -1"
testar_parametro_invalido "java -cp bin Mestre abc 0 1"
testar_parametro_invalido "java -cp bin Escravo"
testar_parametro_invalido "java -cp bin Escravo 0 0 1"
testar_parametro_invalido "java -cp bin Escravo 4 0 1"
testar_parametro_invalido "java -cp bin Escravo 1 5 1"
testar_parametro_invalido "java -cp bin Escravo 1 0 0"
testar_parametro_invalido "java -cp bin Escravo abc 0 1"

echo "================================================================================"
echo "TODOS OS CENÁRIOS E PARÂMETROS FORAM VALIDADOS COM 100% DE SUCESSO!"
echo "================================================================================"
