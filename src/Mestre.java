import comunicacao.ConfiguracaoRede;
import comunicacao.MensagemToken;
import modelo.OperacaoToken;
import util.TerminalUI;

import java.io.IOException;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketTimeoutException;

/**
 * Processo Mestre (ID 0) do Token Ring UDP.
 * 
 * Responsabilidades:
 * 1. Inicializar o anel com o token de valor inteiro 2.
 * 2. Enviar o token para o Escravo 1 (início da circulação).
 * 3. Receber o token de volta do Escravo 3 ao término de cada volta.
 * 4. Aplicar a operação matemática local (+1 ou *2).
 * 5. Controlar o total de voltas configurado.
 * 6. Na última volta, exibir o resultado final sem retransmitir o token.
 * 
 * Créditos e Origem:
 * - Implementação base: Isaac (parte Isaac/Token Ring/Mestre.java)
 * - Serialização de rede: Heitor (formato binário ByteBuffer de 8 bytes)
 * - Tratamento de timeout e exceções: Vitor (SocketTimeoutException de 120s)
 * - Coordenação, configuração de rede e UI visual: Miqueias
 */
public class Mestre {

    private static final int INTERVALO_DIDATICO_SEGUNDOS = Integer.getInteger("tokenring.intervalo", 5);

    public static void main(String[] args) {
        // Validação dos parâmetros obrigatórios exigidos pelo professor:
        // java Mestre 0 <operacao> <voltas> [ipEscravo1] [portaEscravo1] [portaMestre]
        if (args.length < 3) {
            System.err.println("Uso obrigatório: java Mestre <id=0> <operacao: 0=+1 | 1=*2> <voltas>");
            System.err.println("Parâmetros opcionais adicionais: [ipEscravo1] [portaEscravo1] [portaMestre]");
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

        // Validações semânticas dos parâmetros
        if (id != 0) {
            System.err.println("Erro: O identificador do processo Mestre deve ser obrigatoriamente 0.");
            System.exit(1);
        }

        if (codigoOperacao != 0 && codigoOperacao != 1) {
            System.err.println("Erro: Operação inválida (" + codigoOperacao + "). Utilize 0 (+1) ou 1 (*2).");
            System.exit(1);
        }

        if (voltas < 1) {
            System.err.println("Erro: A quantidade de voltas deve ser maior ou igual a 1.");
            System.exit(1);
        }

        OperacaoToken operacao = OperacaoToken.porCodigo(codigoOperacao);

        // Inicializa a configuração de rede (lê config/rede.properties ou defaults locais)
        ConfiguracaoRede config = new ConfiguracaoRede();

        // Suporte aos parâmetros opcionais de Isaac para flexibilidade em testes
        if (args.length >= 4) {
            String ipEscravo1 = args[3];
            int portaEscravo1 = (args.length >= 5) ? Integer.parseInt(args[4]) : config.getPortaDestino(0);
            config.sobrescreverDestino(0, ipEscravo1, portaEscravo1);
        }
        if (args.length >= 6) {
            int portaMestre = Integer.parseInt(args[5]);
            config.sobrescreverPortaEscuta(0, portaMestre);
        }

        int portaEscuta = config.getPortaEscuta(0);
        String hostDestino = config.getHostDestino(0);
        int portaDestino = config.getPortaDestino(0);

        TerminalUI.exibirCabecalhoInicial("Mestre", id, operacao, voltas, portaEscuta, config.getOrigemConfiguracao());

        // Inicialização do Token conforme especificado pelo professor: valor 2
        int token = 2;

        try (DatagramSocket socket = new DatagramSocket(portaEscuta)) {
            socket.setSoTimeout(config.getTimeoutMs());
            InetAddress enderecoDestino = InetAddress.getByName(hostDestino);

            for (int volta = 1; volta <= voltas; volta++) {
                System.out.println("\n>>> INICIANDO VOLTA " + volta + " DE " + voltas + " <<<");
                System.out.println("  • Valor atual do token a ser injetado: " + token);

                // Intervalo visual de 5 segundos antes do envio
                TerminalUI.contagemRegressiva(INTERVALO_DIDATICO_SEGUNDOS);

                // Envia o token para o Escravo 1
                MensagemToken mensagemEnvio = new MensagemToken(token, codigoOperacao);
                mensagemEnvio.enviarUDP(socket, enderecoDestino, portaDestino);
                TerminalUI.exibirTransmissao("Escravo 1", hostDestino, portaDestino, token);

                // Aguarda o retorno do token do Escravo 3
                System.out.println("  Aguardando retorno do token vindo do Escravo 3...");
                MensagemToken mensagemRecebida = MensagemToken.receberUDP(socket);

                int valorRecebido = mensagemRecebida.getValor();
                TerminalUI.exibirRecebimentoToken(volta, voltas, "Escravo 3", valorRecebido);

                // Mestre executa sua própria operação matemática configurada
                int valorAnterior = valorRecebido;
                token = OperacaoToken.aplicar(valorRecebido, codigoOperacao);
                TerminalUI.exibirProcessamento(operacao, valorAnterior, token);
            }

            // Exibição do resultado final de todas as voltas
            TerminalUI.exibirResultadoFinal(token, voltas);

        } catch (SocketTimeoutException e) {
            TerminalUI.exibirErro("Tempo limite de espera (timeout de " + (config.getTimeoutMs() / 1000) 
                    + "s) esgotado aguardando retorno do Escravo 3.", e);
            System.err.println("Verifique se o Escravo 3 está em execução e conectado à mesma rede.");
            System.exit(2);
        } catch (IOException e) {
            TerminalUI.exibirErro("Falha de E/S na comunicação UDP do Mestre", e);
            System.exit(3);
        }
    }
}
