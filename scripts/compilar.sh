#!/usr/bin/env bash
# ==============================================================================
# SCRIPT DE COMPILAÇÃO DO PROJETO TOKEN RING UDP
# Coordenador de Integração: Miqueias
# ==============================================================================
set -e

# Navega para a raiz do projeto (independente de onde o script for chamado)
DIR_SCRIPT="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
ROOT_DIR="$(dirname "$DIR_SCRIPT")"

cd "$ROOT_DIR"

echo "================================================================================"
echo "  COMPILANDO O PROJETO TOKEN RING UDP..."
echo "================================================================================"

# Cria diretório de binários
mkdir -p bin

# Compila todos os arquivos Java a partir de src/
javac -d bin \
    src/modelo/OperacaoToken.java \
    src/comunicacao/MensagemToken.java \
    src/comunicacao/Message.java \
    src/comunicacao/ConfiguracaoRede.java \
    src/util/TerminalUI.java \
    src/Mestre.java \
    src/Escravo.java \
    src/testes/TesteTokenRingAutomated.java

echo "[SUCESSO] Compilação concluída com êxito em 'bin/'!"

if [ "$1" == "--limpar-originais" ]; then
    rm -rf "parte Heitor" "parte Isaac" "parte Vitor" "parte de Camila"
    echo "[LIMPEZA] Pastas originais migradas e validadas foram removidas da raiz com sucesso (preservadas em backup/originais/)."
fi
echo "Para executar:"
echo "  java -cp bin Mestre 0 <operacao> <voltas>"
echo "  java -cp bin Escravo <id> <operacao> <voltas>"
echo "================================================================================"
