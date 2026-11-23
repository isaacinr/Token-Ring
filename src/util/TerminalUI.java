package util;

import modelo.OperacaoToken;

/**
 * Utilitário de interface visual para terminais de texto.
 * 
 * Responsável por:
 * 1. Formatar a apresentação das etapas em cada um dos 4 processos.
 * 2. Exibir caixas informativas e cores ANSI (com suporte a fallback).
 * 3. Executar a contagem regressiva de 5 segundos de forma didática.
 * 4. Manter o histórico no terminal (sem limpar a tela) para acompanhamento da banca.
 * 
 * Desenvolvido por: Miqueias
 */
public class TerminalUI {

    // Detecção e suporte a cores ANSI
    private static final boolean CORES_HABILITADAS = !Boolean.getBoolean("noColor") 
            && System.console() != null || "true".equalsIgnoreCase(System.getenv("FORCE_COLOR"));

    // Cores ANSI
    public static final String RESET = CORES_HABILITADAS ? "\u001B[0m" : "";
    public static final String BOLD = CORES_HABILITADAS ? "\u001B[1m" : "";
    public static final String VERMELHO = CORES_HABILITADAS ? "\u001B[31m" : "";
    public static final String VERDE = CORES_HABILITADAS ? "\u001B[32m" : "";
    public static final String AMARELO = CORES_HABILITADAS ? "\u001B[33m" : "";
    public static final String AZUL = CORES_HABILITADAS ? "\u001B[34m" : "";
    public static final String MAGENTA = CORES_HABILITADAS ? "\u001B[35m" : "";
    public static final String CIANO = CORES_HABILITADAS ? "\u001B[36m" : "";
    public static final String BRANCO = CORES_HABILITADAS ? "\u001B[37m" : "";

    public static void exibirCabecalhoInicial(String nomeProcesso, int id, OperacaoToken op, int totalVoltas, int portaEscuta, String infoRede) {
        System.out.println();
        System.out.println(CIANO + BOLD + "================================================================================" + RESET);
        System.out.println(CIANO + BOLD + "   TOKEN RING UDP — SISTEMAS DISTRIBUÍDOS (" + nomeProcesso.toUpperCase() + " - ID: " + id + ")" + RESET);
        System.out.println(CIANO + BOLD + "================================================================================" + RESET);
        System.out.printf("  Configuração Inicial:%n");
        System.out.printf("  • Processo:            %s%s%s%n", BOLD, nomeProcesso, RESET);
        System.out.printf("  • Operação Local:      %s%s (Código %d)%s%n", VERDE + BOLD, op.getDescricao(), op.getCodigo(), RESET);
        System.out.printf("  • Total de Voltas:     %s%d%s%n", BOLD, totalVoltas, RESET);
        System.out.printf("  • Porta UDP de Escuta: %s%d%s%n", AMARELO + BOLD, portaEscuta, RESET);
        System.out.printf("  • Origem da Rede:      %s%n", infoRede);
        System.out.println(CIANO + "--------------------------------------------------------------------------------" + RESET);
        System.out.println("  Status: Socket inicializado. Aguardando circulação do anel...");
        System.out.println(CIANO + "================================================================================" + RESET);
        System.out.println();
    }

    public static void exibirRecebimentoToken(int voltaAtual, int totalVoltas, String origem, int valorRecebido) {
        System.out.println();
        System.out.println(AZUL + BOLD + "┌──────────────────────────────────────────────────────────────────────────────┐" + RESET);
        System.out.printf(AZUL + BOLD + "│ VOLTA %d DE %d — TOKEN RECEBIDO" + RESET + "%n", voltaAtual, totalVoltas);
        System.out.println(AZUL + "├──────────────────────────────────────────────────────────────────────────────┤" + RESET);
        System.out.printf("│  • Origem do Token:  %s%-55s%s │%n", BOLD, origem, RESET);
        System.out.printf("│  • Valor Recebido:   %s%-55d%s │%n", AMARELO + BOLD, valorRecebido, RESET);
    }

    public static void exibirProcessamento(OperacaoToken op, int valorAnterior, int valorNovo) {
        System.out.printf("│  • Operação:         %s%-55s%s │%n", VERDE + BOLD, op.getDescricao(), RESET);
        System.out.printf("│  • Valor Anterior:   %-55d │%n", valorAnterior);
        System.out.printf("│  • Valor Atualizado: %s%-55d%s │%n", VERDE + BOLD, valorNovo, RESET);
        System.out.println(AZUL + BOLD + "└──────────────────────────────────────────────────────────────────────────────┘" + RESET);
    }

    /**
     * Executa a contagem regressiva de 5 segundos de forma didática e elegante,
     * permitindo que o professor e os colegas acompanhem a evolução em tempo real.
     */
    public static void contagemRegressiva(int segundos) {
        if (segundos <= 0) {
            return;
        }
        System.out.print(MAGENTA + "  Aguardando próxima transmissão: " + RESET);
        for (int i = segundos; i > 0; i--) {
            System.out.print(MAGENTA + BOLD + "[" + i + "s] " + RESET);
            System.out.flush();
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                System.out.println();
                return;
            }
        }
        System.out.println(VERDE + BOLD + "-> TRANSMITINDO!" + RESET);
    }

    public static void exibirTransmissao(String destino, String ipDestino, int portaDestino, int valorEnviado) {
        System.out.printf("  » Enviando token (%s%d%s) para %s%s%s (%s:%d)%n",
                AMARELO + BOLD, valorEnviado, RESET,
                BOLD, destino, RESET,
                ipDestino, portaDestino);
        System.out.println(CIANO + "--------------------------------------------------------------------------------" + RESET);
    }

    public static void exibirResultadoFinal(int resultado, int totalVoltas) {
        System.out.println();
        System.out.println(VERDE + BOLD + "╔══════════════════════════════════════════════════════════════════════════════╗" + RESET);
        System.out.println(VERDE + BOLD + "║                        CIRCULAÇÃO CONCLUÍDA COM SUCESSO                      ║" + RESET);
        System.out.println(VERDE + BOLD + "╠══════════════════════════════════════════════════════════════════════════════╣" + RESET);
        System.out.printf(VERDE + BOLD + "║   Total de Voltas Realizadas: %-46d ║%n", totalVoltas);
        System.out.printf(VERDE + BOLD + "║   VALOR FINAL DO TOKEN:       %-46d ║%n", resultado);
        System.out.println(VERDE + BOLD + "╚══════════════════════════════════════════════════════════════════════════════╝" + RESET);
        System.out.println();
    }

    public static void exibirEncerramentoEscravo(String nomeProcesso, int id) {
        System.out.println();
        System.out.println(VERDE + "  [" + nomeProcesso + " - ID " + id + "] Todas as voltas processadas com sucesso. Encerrando." + RESET);
        System.out.println();
    }

    public static void exibirErro(String contexto, Exception e) {
        System.err.println();
        System.err.println(VERMELHO + BOLD + "================================================================================" + RESET);
        System.err.printf(VERMELHO + BOLD + "  [ERRO NO TOKEN RING] Contexto: %s%n" + RESET, contexto);
        System.err.printf(VERMELHO + "  Mensagem: %s%n" + RESET, e.getMessage());
        System.err.println(VERMELHO + BOLD + "================================================================================" + RESET);
    }
}
