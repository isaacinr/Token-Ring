package comunicacao;

import java.io.IOException;
import java.net.DatagramSocket;
import java.net.InetAddress;

/**
 * Classe adaptadora de compatibilidade com a implementação de referência de Heitor.
 * 
 * Mantém os atributos e métodos concebidos por Heitor (Message.java),
 * direcionando a execução para a arquitetura com socket gerenciado.
 * 
 * Autor original: Heitor
 * Refatoração de integração: Miqueias
 */
public class Message {

    public int valor;
    public int operacao;
    public String ip;
    public int porta;

    public Message(int valor, int operacao, String ip, int porta) {
        this.valor = valor;
        this.operacao = operacao;
        this.ip = ip;
        this.porta = porta;
    }

    public void sendUDP() {
        try (DatagramSocket socket = new DatagramSocket()) {
            MensagemToken token = new MensagemToken(this.valor, this.operacao);
            token.enviarUDP(socket, InetAddress.getByName(this.ip), this.porta);
        } catch (IOException e) {
            System.err.println("Erro ao enviar UDP (Message): " + e.getMessage());
        }
    }

    public int[] receiveUDP() {
        return receiveUDP(this.porta > 0 ? this.porta : 5000);
    }

    public int[] receiveUDP(int portaEscuta) {
        try (DatagramSocket socket = new DatagramSocket(portaEscuta)) {
            MensagemToken msg = MensagemToken.receberUDP(socket);
            return new int[] { msg.getValor(), msg.getOperacao() };
        } catch (IOException e) {
            System.err.println("Erro ao receber UDP (Message): " + e.getMessage());
            return null;
        }
    }
}
