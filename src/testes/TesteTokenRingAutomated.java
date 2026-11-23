package testes;

import comunicacao.ConfiguracaoRede;
import comunicacao.MensagemToken;
import modelo.OperacaoToken;

import java.net.BindException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketTimeoutException;
import java.nio.BufferUnderflowException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Suíte de Testes Automatizados Unitários e de Integração do Token Ring UDP.
 * 
 * Executa todos os testes exigidos na especificação:
 * - Teste 1: Todos incrementando (Resultado esperado: 6) sobre sockets UDP reais
 * - Teste 2: Todos multiplicando (Resultado esperado: 32) sobre sockets UDP reais
 * - Teste 3: Duas voltas incrementando (Resultado esperado: 10) sobre sockets UDP reais
 * - Teste 4: Operações heterogêneas (Resultado esperado: 14) sobre sockets UDP reais
 * - Teste 5: Validação semântica de parâmetros e limites
 * - Teste 6: Resiliência de rede UDP (BindException, SocketTimeoutException, Malformed)
 * - Teste 7: Topologia e roteamento do anel
 * - Teste 8: Serialização binária ByteBuffer de 8 bytes (Heitor)
 * 
 * Coordenador de Testes: Miqueias
 */
public class TesteTokenRingAutomated {

    private static int testesExecutados = 0;
    private static int testesAprovados = 0;
    private static int testesFalhos = 0;

    public static void main(String[] args) {
        System.out.println("================================================================================");
        System.out.println("  SUÍTE DE TESTES AUTOMATIZADOS — TOKEN RING UDP (SISTEMAS DISTRIBUÍDOS)");
        System.out.println("  Responsável pela Validação: Miqueias");
        System.out.println("================================================================================");

        // --- TESTES DE DOMÍNIO E MATEMÁTICA ---
        executarTeste("1. Teste de Operações Matemáticas (+1 e *2)", () -> {
            afirmar(OperacaoToken.aplicar(2, 0) == 3, "2 + 1 deve ser 3");
            afirmar(OperacaoToken.aplicar(5, 0) == 6, "5 + 1 deve ser 6");
            afirmar(OperacaoToken.aplicar(2, 1) == 4, "2 * 2 deve ser 4");
            afirmar(OperacaoToken.aplicar(8, 1) == 16, "8 * 2 deve ser 16");

            try {
                OperacaoToken.aplicar(2, 9);
                afirmar(false, "Operação 9 deveria lançar IllegalArgumentException");
            } catch (IllegalArgumentException e) {
                // Sucesso esperado
            }
        });

        // --- TESTES DE SERIALIZAÇÃO BINÁRIA ---
        executarTeste("2. Teste de Serialização Binária ByteBuffer (Heitor - 8 Bytes)", () -> {
            MensagemToken msgOriginal = new MensagemToken(42, 1);
            byte[] bytes = msgOriginal.serializar();

            afirmar(bytes.length == 8, "Buffer serializado deve possuir exatamente 8 bytes");

            MensagemToken msgDesserializada = MensagemToken.desserializar(bytes, 0, bytes.length);
            afirmar(msgDesserializada.getValor() == 42, "Valor recuperado deve ser 42");
            afirmar(msgDesserializada.getOperacao() == 1, "Operação recuperada deve ser 1");

            // Teste de limites numéricos
            MensagemToken msgLimites = new MensagemToken(Integer.MAX_VALUE, 0);
            byte[] bytesLimites = msgLimites.serializar();
            MensagemToken recLimites = MensagemToken.desserializar(bytesLimites, 0, bytesLimites.length);
            afirmar(recLimites.getValor() == Integer.MAX_VALUE, "Valor MAX_VALUE deve ser preservado");
        });

        // --- TESTES DE TOPOLOGIA ---
        executarTeste("3. Teste de Topologia e Roteamento do Anel", () -> {
            ConfiguracaoRede config = new ConfiguracaoRede();
            afirmar(config.getProximoId(0) == 1, "Mestre (0) deve encaminhar para Escravo 1 (1)");
            afirmar(config.getProximoId(1) == 2, "Escravo 1 (1) deve encaminhar para Escravo 2 (2)");
            afirmar(config.getProximoId(2) == 3, "Escravo 2 (2) deve encaminhar para Escravo 3 (3)");
            afirmar(config.getProximoId(3) == 0, "Escravo 3 (3) deve encaminhar para Mestre (0)");

            afirmar(config.getPortaEscuta(0) == 5000, "Porta Mestre deve ser 5000");
            afirmar(config.getPortaEscuta(1) == 5001, "Porta Escravo 1 deve ser 5001");
            afirmar(config.getPortaEscuta(2) == 5002, "Porta Escravo 2 deve ser 5002");
            afirmar(config.getPortaEscuta(3) == 5003, "Porta Escravo 3 deve ser 5003");
        });

        // --- TESTE DE RESILIÊNCIA DE REDE UDP (TESTE 6 DO PROFESSOR) ---
        executarTeste("4. Teste de Resiliência UDP: Detecção de Porta Ocupada (BindException)", () -> {
            int portaConflito = 18001;
            try (DatagramSocket socket1 = new DatagramSocket(portaConflito)) {
                try (DatagramSocket socket2 = new DatagramSocket(portaConflito)) {
                    afirmar(false, "Segundo socket na mesma porta deveria lançar BindException");
                } catch (BindException e) {
                    // Sucesso esperado: o sistema operacional bloqueia bind duplicado
                }
            }
        });

        executarTeste("5. Teste de Resiliência UDP: Timeout de Espera (SocketTimeoutException)", () -> {
            int portaTimeout = 18002;
            try (DatagramSocket socket = new DatagramSocket(portaTimeout)) {
                socket.setSoTimeout(200); // 200ms de timeout
                byte[] buf = new byte[8];
                DatagramPacket p = new DatagramPacket(buf, buf.length);
                try {
                    socket.receive(p);
                    afirmar(false, "Deveria ter disparado SocketTimeoutException");
                } catch (SocketTimeoutException e) {
                    // Sucesso esperado
                }
            }
        });

        executarTeste("6. Teste de Resiliência UDP: Detecção de Pacote Truncado/Inválido", () -> {
            byte[] pacoteInvalido = new byte[]{1, 2, 3}; // menos de 8 bytes
            try {
                MensagemToken.desserializar(pacoteInvalido, 0, pacoteInvalido.length);
                afirmar(false, "Pacote menor que 8 bytes deveria falhar");
            } catch (BufferUnderflowException e) {
                // Sucesso esperado
            }
        });

        // --- TESTES DE ANEL REAL EM SOCKETS UDP DISTRIBUÍDOS ---
        executarTeste("7. Teste 1 do Professor em Sockets UDP Reais: Todos Incrementando (Esperado: 6)", () -> {
            int resultado = simularAnelUDP(19000, 0, 0, 0, 0, 1);
            afirmar(resultado == 6, "Resultado do Teste 1 deve ser rigorosamente 6. Obtido: " + resultado);
        });

        executarTeste("8. Teste 2 do Professor em Sockets UDP Reais: Todos Multiplicando (Esperado: 32)", () -> {
            int resultado = simularAnelUDP(19010, 1, 1, 1, 1, 1);
            afirmar(resultado == 32, "Resultado do Teste 2 deve ser rigorosamente 32. Obtido: " + resultado);
        });

        executarTeste("9. Teste 3 do Professor em Sockets UDP Reais: Duas Voltas Incrementando (Esperado: 10)", () -> {
            int resultado = simularAnelUDP(19020, 0, 0, 0, 0, 2);
            afirmar(resultado == 10, "Resultado do Teste 3 deve ser rigorosamente 10. Obtido: " + resultado);
        });

        executarTeste("10. Teste 4 em Sockets UDP Reais: Operações Heterogêneas (Esperado: 14)", () -> {
            // E1: +1 (2+1=3), E2: *2 (3*2=6), E3: +1 (6+1=7), Mestre: *2 (7*2=14)
            int resultado = simularAnelUDP(19030, 1, 0, 1, 0, 1);
            afirmar(resultado == 14, "Resultado do Teste 4 deve ser rigorosamente 14. Obtido: " + resultado);
        });

        System.out.println("================================================================================");
        System.out.println("  RESUMO FINAL DOS TESTES:");
        System.out.println("  • Total de Testes Executados: " + testesExecutados);
        System.out.println("  • Testes Aprovados:           " + testesAprovados);
        System.out.println("  • Testes Falhos:              " + testesFalhos);
        System.out.println("================================================================================");

        if (testesFalhos > 0) {
            System.err.println("ALERTA: Houve falhas na validação.");
            System.exit(1);
        } else {
            System.out.println("STATUS: TODOS OS TESTES PASSARAM COM SUCESSO!");
        }
    }

    /**
     * Simula a topologia Token Ring completa com 4 processos autônomos
     * comunicando-se exclusivamente através de sockets UDP locais independentes.
     */
    private static int simularAnelUDP(int basePorta, int opMestre, int opE1, int opE2, int opE3, int voltas) throws Exception {
        int p0 = basePorta;
        int p1 = basePorta + 1;
        int p2 = basePorta + 2;
        int p3 = basePorta + 3;
        InetAddress loopback = InetAddress.getByName("127.0.0.1");

        CountDownLatch escravosProntos = new CountDownLatch(3);
        AtomicInteger resultadoMestre = new AtomicInteger(-1);
        AtomicReference<Throwable> erro = new AtomicReference<>();

        // Escravo 3: escuta na porta p3, recebe de E2, envia para Mestre (p0)
        Thread tE3 = new Thread(() -> {
            try (DatagramSocket socket = new DatagramSocket(p3, loopback)) {
                socket.setSoTimeout(5000);
                escravosProntos.countDown();
                for (int v = 1; v <= voltas; v++) {
                    MensagemToken msg = MensagemToken.receberUDP(socket);
                    int res = OperacaoToken.aplicar(msg.getValor(), opE3);
                    new MensagemToken(res, opE3).enviarUDP(socket, loopback, p0);
                }
            } catch (Throwable t) {
                erro.compareAndSet(null, t);
            }
        });

        // Escravo 2: escuta na porta p2, recebe de E1, envia para E3 (p3)
        Thread tE2 = new Thread(() -> {
            try (DatagramSocket socket = new DatagramSocket(p2, loopback)) {
                socket.setSoTimeout(5000);
                escravosProntos.countDown();
                for (int v = 1; v <= voltas; v++) {
                    MensagemToken msg = MensagemToken.receberUDP(socket);
                    int res = OperacaoToken.aplicar(msg.getValor(), opE2);
                    new MensagemToken(res, opE2).enviarUDP(socket, loopback, p3);
                }
            } catch (Throwable t) {
                erro.compareAndSet(null, t);
            }
        });

        // Escravo 1: escuta na porta p1, recebe de Mestre (p0), envia para E2 (p2)
        Thread tE1 = new Thread(() -> {
            try (DatagramSocket socket = new DatagramSocket(p1, loopback)) {
                socket.setSoTimeout(5000);
                escravosProntos.countDown();
                for (int v = 1; v <= voltas; v++) {
                    MensagemToken msg = MensagemToken.receberUDP(socket);
                    int res = OperacaoToken.aplicar(msg.getValor(), opE1);
                    new MensagemToken(res, opE1).enviarUDP(socket, loopback, p2);
                }
            } catch (Throwable t) {
                erro.compareAndSet(null, t);
            }
        });

        tE3.start();
        tE2.start();
        tE1.start();

        if (!escravosProntos.await(2, TimeUnit.SECONDS)) {
            throw new RuntimeException("Escravos não inicializaram a tempo");
        }

        // Mestre (0): escuta em p0, envia para E1 (p1), aguarda retorno de E3
        try (DatagramSocket socket = new DatagramSocket(p0, loopback)) {
            socket.setSoTimeout(5000);
            int token = 2; // Token inicial = 2
            for (int v = 1; v <= voltas; v++) {
                new MensagemToken(token, opMestre).enviarUDP(socket, loopback, p1);
                MensagemToken retorno = MensagemToken.receberUDP(socket);
                token = OperacaoToken.aplicar(retorno.getValor(), opMestre);
            }
            resultadoMestre.set(token);
        }

        tE1.join(3000);
        tE2.join(3000);
        tE3.join(3000);

        if (erro.get() != null) {
            throw new RuntimeException("Erro em nó do anel: " + erro.get().getMessage(), erro.get());
        }

        return resultadoMestre.get();
    }

    private static void executarTeste(String nome, RunnableTeste teste) {
        testesExecutados++;
        System.out.printf("[%02d] %-72s", testesExecutados, nome);
        try {
            teste.executar();
            testesAprovados++;
            System.out.println(" [PASSOU]");
        } catch (Throwable t) {
            testesFalhos++;
            System.out.println(" [FALHOU: " + t.getMessage() + "]");
        }
    }

    private static void afirmar(boolean condicao, String mensagem) {
        if (!condicao) {
            throw new AssertionError(mensagem);
        }
    }

    @FunctionalInterface
    interface RunnableTeste {
        void executar() throws Throwable;
    }
}
