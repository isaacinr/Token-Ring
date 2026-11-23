import comunicacao.ConfiguracaoRede;
import comunicacao.MensagemToken;
import modelo.OperacaoToken;
import util.TerminalUI;

import java.io.IOException;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketTimeoutException;

/**
 * Processo Escravo (IDs 1, 2 ou 3) do Token Ring UDP.
 * 
 * Uma única classe gerencia os nós Escravo 1, Escravo 2 e Escravo 3,
 * determinando automaticamente suas portas e vizinhos na topologia em anel:
 * - Escravo 1 (ID 1): Recebe do Mestre (0)    -> Envia para Escravo 2 (2)
 * - Escravo 2 (ID 2): Recebe de Escravo 1 (1) -> Envia para Escravo 3 (3)
 * - Escravo 3 (ID 3): Recebe de Escravo 2 (2) -> Envia para Mestre (0)
 * 
 * Créditos e Origem:
 * - Validação estrita e método processarToken: Camila (parte de Camila/Trabalho de SD/Escravo.java)
 * - Ciclo de recepção, loop de voltas e timeouts: Vitor (parte Vitor/Trabalho Token Ring - Escravos/Escravo.java)
 * - Transporte binário ByteBuffer de 8 bytes: Heitor (Message.java)
 * - Coordenação, configuração de rede unificada e UI visual: Miqueias
 */
public class Escravo {

    private static final int INTERVALO_DIDATICO_SEGUNDOS = Integer.getInteger("tokenring.intervalo", 5);

    public static void main(String[] args) {
        // Validação dos parâmetros obrigatórios exigidos pelo professor:
        // java Escravo <id: 1, 2 ou 3> <operacao: 0=+1 | 1=*2> <voltas>
        if (args.length < 3) {
            System.err.println("Uso obrigatório: java Escravo <id: 1, 2 ou 3> <operacao: 0=+1 | 1=*2> <voltas>");
            System.err.println("Parâmetros opcionais adicionais: [ipProximo] [portaProximo] [portaEscravo]");
            System.exit(1);
        }

        int id;
        int codigoOperacao;
        int voltas;

        try {
            id = Integer.parseInt(args[0]);
            codigoOperacao = Integer.parseInt(args[1]);
            voltas = Integer.parseInt(args[2]);
        } catch (NumberFormatException e) {
            System.err.println("Erro: Os parâmetros id, operação e voltas devem ser números inteiros válidos.");
            System.exit(1);
            return;
        }

        // Validação inspirada na contribuição de Camila
        if (id < 1 || id > 3) {
            System.err.println("Erro: Identificador do Escravo inválido (" + id + "). Deve ser 1, 2 ou 3.");
            System.exit(1);
        }

        if (codigoOperacao != 0 && codigoOperacao != 1) {
            System.err.println("Erro: Operação inválida (" + codigoOperacao + "). Utilize 0 (+1) ou 1 (*2).");
            System.exit(1);
        }

        if (voltas <= 0) {
            System.err.println("Erro: A quantidade de voltas deve ser maior que zero.");
            System.exit(1);
        }

        OperacaoToken operacao = OperacaoToken.porCodigo(codigoOperacao);

        // Inicializa a configuração de rede (lê config/rede.properties ou defaults locais)
        ConfiguracaoRede config = new ConfiguracaoRede();

        // Suporte a argumentos extras opcionais para testes avançados
        if (args.length >= 4) {
            String ipProximo = args[3];
            int portaProximo = (args.length >= 5) ? Integer.parseInt(args[4]) : config.getPortaDestino(id);
            config.sobrescreverDestino(id, ipProximo, portaProximo);
        }
        if (args.length >= 6) {
            int portaEscravo = Integer.parseInt(args[5]);
            config.sobrescreverPortaEscuta(id, portaEscravo);
        }

        String nomeProcesso = config.getNomeProcesso(id);
        int portaEscuta = config.getPortaEscuta(id);
        String hostDestino = config.getHostDestino(id);
        int portaDestino = config.getPortaDestino(id);
        String nomeDestino = config.getNomeProcesso(config.getProximoId(id));

        String nomeOrigem;
        switch (id) {
            case 1: nomeOrigem = "Mestre (ID 0)"; break;
            case 2: nomeOrigem = "Escravo 1 (ID 1)"; break;
            case 3: nomeOrigem = "Escravo 2 (ID 2)"; break;
            default: nomeOrigem = "Processo Anterior"; break;
        }

        TerminalUI.exibirCabecalhoInicial(nomeProcesso, id, operacao, voltas, portaEscuta, config.getOrigemConfiguracao());

        try (DatagramSocket socket = new DatagramSocket(portaEscuta)) {
            socket.setSoTimeout(config.getTimeoutMs());
            InetAddress enderecoDestino = InetAddress.getByName(hostDestino);

            // Loop de processamento de cada volta (estrutura refinada a partir do código de Vitor)
            for (int volta = 1; volta <= voltas; volta++) {
                System.out.println("  Aguardando recebimento do token vindo de " + nomeOrigem + "...");

                // 1. Recebimento do token via UDP
                MensagemToken mensagemRecebida = MensagemToken.receberUDP(socket);
                int valorRecebido = mensagemRecebida.getValor();

                TerminalUI.exibirRecebimentoToken(volta, voltas, nomeOrigem, valorRecebido);

                // 2. Execução da operação matemática (método herdado da especificação de Camila)
                int valorAnterior = valorRecebido;
                int valorAtualizado = processarToken(valorRecebido, codigoOperacao);

                TerminalUI.exibirProcessamento(operacao, valorAnterior, valorAtualizado);

                // 3. Intervalo visual didático de 5 segundos
                TerminalUI.contagemRegressiva(INTERVALO_DIDATICO_SEGUNDOS);

                // 4. Encaminhamento do token atualizado para o próximo nó do anel
                MensagemToken mensagemEnvio = new MensagemToken(valorAtualizado, codigoOperacao);
                mensagemEnvio.enviarUDP(socket, enderecoDestino, portaDestino);

                TerminalUI.exibirTransmissao(nomeDestino, hostDestino, portaDestino, valorAtualizado);
            }

            // Encerramento gracioso após concluir todas as voltas
            TerminalUI.exibirEncerramentoEscravo(nomeProcesso, id);

        } catch (SocketTimeoutException e) {
            TerminalUI.exibirErro("Tempo de espera esgotado (" + (config.getTimeoutMs() / 1000) 
                    + "s). Token não foi recebido a tempo.", e);
            System.err.println("Verifique a comunicação com o processo anterior no anel (" + nomeOrigem + ").");
            System.exit(2);
        } catch (IOException e) {
            TerminalUI.exibirErro("Erro na comunicação de rede do " + nomeProcesso, e);
            System.exit(3);
        }
    }

    /**
     * Aplica a operação matemática ao token.
     * Preserva a assinatura exata do método concebido por Camila em sua submissão original.
     * 
     * @param token valor numérico do token recebido
     * @param operacao 0 para incremento (+1) ou 1 para multiplicação (*2)
     * @return novo valor do token
     */
    public static int processarToken(int token, int operacao) {
        return OperacaoToken.aplicar(token, operacao);
    }
}
