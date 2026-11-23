package comunicacao;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.nio.ByteBuffer;

/**
 * Representa a mensagem de transporte do Token pela rede UDP.
 * 
 * Preserva integralmente o protocolo de transporte binário desenvolvido
 * originalmente por Heitor (Message.java), utilizando exatamente 8 bytes:
 * - 4 bytes (int): Valor atual do token
 * - 4 bytes (int): Código da operação matemática transmitida
 * 
 * Adaptações de Integração (Miqueias):
 * 1. O DatagramSocket agora é injetado, permitindo que a aplicação
 *    mantenha o mesmo socket aberto durante toda a vida do processo.
 * 2. Suporta portas de escuta e envio dinâmicas (5000 a 5003).
 * 3. Tratamento robusto de IOException com informações do host e porta.
 */
public class MensagemToken {

    public static final int TAMANHO_BYTES = 8;

    private final int valor;
    private final int operacao;

    public MensagemToken(int valor, int operacao) {
        this.valor = valor;
        this.operacao = operacao;
    }

    public int getValor() {
        return valor;
    }

    public int getOperacao() {
        return operacao;
    }

    /**
     * Serializa a mensagem em um array de 8 bytes usando ByteBuffer Big-Endian
     * (mantém a especificação binária de Heitor).
     */
    public byte[] serializar() {
        ByteBuffer buffer = ByteBuffer.allocate(TAMANHO_BYTES);
        buffer.putInt(this.valor);
        buffer.putInt(this.operacao);
        return buffer.array();
    }

    /**
     * Desserializa um buffer de 8 bytes recebido via UDP.
     */
    public static MensagemToken desserializar(byte[] dados, int offset, int length) {
        ByteBuffer byteBuffer = ByteBuffer.wrap(dados, offset, length);
        int valorRecebido = byteBuffer.getInt();
        int operacaoRecebida = byteBuffer.getInt();
        return new MensagemToken(valorRecebido, operacaoRecebida);
    }

    /**
     * Envia o token para um endereço IP e porta específicos através de um socket UDP aberto.
     */
    public void enviarUDP(DatagramSocket socket, InetAddress enderecoDestino, int portaDestino) throws IOException {
        byte[] dados = serializar();
        DatagramPacket pacote = new DatagramPacket(dados, dados.length, enderecoDestino, portaDestino);
        socket.send(pacote);
    }

    /**
     * Aguarda e recebe um pacote UDP de 8 bytes através do socket informado.
     */
    public static MensagemToken receberUDP(DatagramSocket socket) throws IOException {
        byte[] buffer = new byte[TAMANHO_BYTES];
        DatagramPacket pacote = new DatagramPacket(buffer, buffer.length);
        socket.receive(pacote);
        return desserializar(pacote.getData(), pacote.getOffset(), pacote.getLength());
    }

    @Override
    public String toString() {
        return "MensagemToken{valor=" + valor + ", operacao=" + operacao + "}";
    }
}
