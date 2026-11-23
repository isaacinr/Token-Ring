package comunicacao;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.util.Properties;

/**
 * Gerenciador centralizado de configuração de rede para o Token Ring.
 * 
 * Permite a execução transparente em dois cenários:
 * 1. Execução Local (Loopback 127.0.0.1 com portas 5000 a 5003):
 *    Ativada automaticamente por padrão na ausência de rede.properties.
 * 2. Execução Distribuída (Quatro notebooks físicos na mesma rede Wi-Fi):
 *    Configurada via config/rede.properties, especificando o IP de cada máquina.
 * 
 * Mapeamento da topologia em anel:
 * - Mestre    (ID 0) -> Escuta na porta 5000 -> Envia para Escravo 1
 * - Escravo 1 (ID 1) -> Escuta na porta 5001 -> Envia para Escravo 2
 * - Escravo 2 (ID 2) -> Escuta na porta 5002 -> Envia para Escravo 3
 * - Escravo 3 (ID 3) -> Escuta na porta 5003 -> Envia para Mestre
 * 
 * Desenvolvido por: Miqueias
 */
public class ConfiguracaoRede {

    public static final int PORTA_PADRAO_MESTRE = 5000;
    public static final int PORTA_PADRAO_ESCRAVO1 = 5001;
    public static final int PORTA_PADRAO_ESCRAVO2 = 5002;
    public static final int PORTA_PADRAO_ESCRAVO3 = 5003;
    public static final String IP_PADRAO_LOOPBACK = "127.0.0.1";
    public static final int TIMEOUT_PADRAO_MS = 120000; // 120 segundos (conforme Vitor)

    private final String[] hosts = new String[4];
    private final int[] portas = new int[4];
    private int timeoutMs = TIMEOUT_PADRAO_MS;
    private String origemConfiguracao = "Padrão interno (localhost/loopback)";

    public ConfiguracaoRede() {
        // Inicializa com padrões locais de segurança
        hosts[0] = IP_PADRAO_LOOPBACK;
        portas[0] = PORTA_PADRAO_MESTRE;

        hosts[1] = IP_PADRAO_LOOPBACK;
        portas[1] = PORTA_PADRAO_ESCRAVO1;

        hosts[2] = IP_PADRAO_LOOPBACK;
        portas[2] = PORTA_PADRAO_ESCRAVO2;

        hosts[3] = IP_PADRAO_LOOPBACK;
        portas[3] = PORTA_PADRAO_ESCRAVO3;

        carregarArquivoSeExistir();
    }

    private void carregarArquivoSeExistir() {
        // Tenta localizar o arquivo nos caminhos comuns
        String[] caminhosTentados = {
            "config/rede.properties",
            "../config/rede.properties",
            System.getProperty("tokenring.config", "config/rede.properties")
        };

        File arquivoConfig = null;
        for (String caminho : caminhosTentados) {
            File f = new File(caminho);
            if (f.exists() && f.isFile()) {
                arquivoConfig = f;
                break;
            }
        }

        if (arquivoConfig == null) {
            return;
        }

        try (FileInputStream fis = new FileInputStream(arquivoConfig)) {
            Properties prop = new Properties();
            prop.load(fis);

            hosts[0] = prop.getProperty("mestre.host", hosts[0]).trim();
            portas[0] = Integer.parseInt(prop.getProperty("mestre.port", String.valueOf(portas[0])).trim());

            hosts[1] = prop.getProperty("escravo1.host", hosts[1]).trim();
            portas[1] = Integer.parseInt(prop.getProperty("escravo1.port", String.valueOf(portas[1])).trim());

            hosts[2] = prop.getProperty("escravo2.host", hosts[2]).trim();
            portas[2] = Integer.parseInt(prop.getProperty("escravo2.port", String.valueOf(portas[2])).trim());

            hosts[3] = prop.getProperty("escravo3.host", hosts[3]).trim();
            portas[3] = Integer.parseInt(prop.getProperty("escravo3.port", String.valueOf(portas[3])).trim());

            timeoutMs = Integer.parseInt(prop.getProperty("socket.timeout.ms", String.valueOf(timeoutMs)).trim());

            this.origemConfiguracao = "Arquivo carregado: " + arquivoConfig.getAbsolutePath();
        } catch (Exception e) {
            System.err.println("[AVISO] Falha ao ler arquivo de propriedades: " + e.getMessage() + ". Utilizando padrões.");
        }
    }

    public int getPortaEscuta(int idProcesso) {
        validarId(idProcesso);
        return portas[idProcesso];
    }

    public String getHostDestino(int idProcesso) {
        validarId(idProcesso);
        int idDestino = getProximoId(idProcesso);
        return hosts[idDestino];
    }

    public int getPortaDestino(int idProcesso) {
        validarId(idProcesso);
        int idDestino = getProximoId(idProcesso);
        return portas[idDestino];
    }

    public int getProximoId(int idProcesso) {
        validarId(idProcesso);
        // Anel lógico: 0 -> 1 -> 2 -> 3 -> 0
        return (idProcesso + 1) % 4;
    }

    public String getNomeProcesso(int idProcesso) {
        switch (idProcesso) {
            case 0: return "Mestre";
            case 1: return "Escravo 1";
            case 2: return "Escravo 2";
            case 3: return "Escravo 3";
            default: return "Processo " + idProcesso;
        }
    }

    public int getTimeoutMs() {
        return timeoutMs;
    }

    public String getOrigemConfiguracao() {
        return origemConfiguracao;
    }

    public void sobrescreverDestino(int idProcesso, String host, int porta) {
        validarId(idProcesso);
        int idDestino = getProximoId(idProcesso);
        if (host != null && !host.trim().isEmpty()) {
            hosts[idDestino] = host.trim();
        }
        if (porta > 0) {
            portas[idDestino] = porta;
        }
        this.origemConfiguracao += " + Parâmetros CLI sobrescritos";
    }

    public void sobrescreverPortaEscuta(int idProcesso, int porta) {
        validarId(idProcesso);
        if (porta > 0) {
            portas[idProcesso] = porta;
        }
    }

    private void validarId(int idProcesso) {
        if (idProcesso < 0 || idProcesso > 3) {
            throw new IllegalArgumentException("Identificador de processo inválido: " + idProcesso + ". Deve ser de 0 a 3.");
        }
    }
}
