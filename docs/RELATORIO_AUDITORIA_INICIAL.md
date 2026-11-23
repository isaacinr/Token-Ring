# RELATÓRIO DE AUDITORIA INICIAL — PROJETO TOKEN RING
**Disciplina:** Sistemas Distribuídos  
**Coordenador de Integração e Testes:** Miqueias  
**Data da Auditoria:** 22/11/2026  
**Repositório Base:** https://github.com/isaacinr/Token-Ring  

---

## 1. OBJETIVO DA AUDITORIA
Este relatório apresenta o diagnóstico técnico exaustivo dos arquivos recebidos dos cinco integrantes da equipe (Heitor, Isaac, Vitor, Camila e Miqueias) para o projeto de comunicação em anel (*Token Ring*) sobre UDP em Java.

O objetivo do projeto acadêmico consiste em implementar quatro processos autônomos:
- **Processo 0 (Mestre)**
- **Processo 1 (Escravo 1)**
- **Processo 2 (Escravo 2)**
- **Processo 3 (Escravo 3)**

A circulação obedece ao anel lógico:
$$\text{Mestre (0)} \longrightarrow \text{Escravo 1 (1)} \longrightarrow \text{Escravo 2 (2)} \longrightarrow \text{Escravo 3 (3)} \longrightarrow \text{Mestre (0)}$$

O token é inicializado em **2**. Cada processo executa sua respectiva operação matemática configurada:
- **Operação 0:** Incrementar o token em 1 ($token \leftarrow token + 1$).
- **Operação 1:** Multiplicar o token por 2 ($token \leftarrow token \times 2$).

Os comandos obrigatórios estipulados pelo professor são:
```bash
java Mestre 0 <operacao> <voltas>
java Escravo 1 <operacao> <voltas>
java Escravo 2 <operacao> <voltas>
java Escravo 3 <operacao> <voltas>
```

---

## 2. INVENTÁRIO DOS ARQUIVOS RECEBIDOS

A varredura recursiva do diretório raiz `/home/miqueias/Projetos/Token-Ring` identificou as seguintes pastas e arquivos originais:

| Diretório Original | Arquivo | Tamanho (Bytes) | Linhas | Tipo | Responsável |
| :--- | :--- | :--- | :--- | :--- | :--- |
| `parte Heitor/` | `Message.java` | 2.103 | 70 | Código Fonte Java | Heitor |
| `parte Isaac/Token Ring/` | `Mestre.java` | 4.005 | 85 | Código Fonte Java | Isaac |
| `parte Vitor/Trabalho Token Ring - Escravos/` | `Mestre.java` | 5.064 | 155 | Código Fonte Java | Vitor |
| `parte Vitor/Trabalho Token Ring - Escravos/` | `Escravo.java` | 5.043 | 151 | Código Fonte Java | Vitor |
| `parte Vitor/Trabalho Token Ring - Escravos/` | `Mestre.class` | 3.673 | — | Bytecode Compilado | Vitor |
| `parte Vitor/Trabalho Token Ring - Escravos/` | `Escravo.class` | 3.646 | — | Bytecode Compilado | Vitor |
| `parte Vitor/Trabalho Token Ring - Escravos/.idea/` | `misc.xml` | 363 | 14 | Configuração IDE | Vitor |
| `parte Vitor/Trabalho Token Ring - Escravos/.idea/` | `workspace.xml` | 245 | 8 | Configuração IDE | Vitor |
| `parte Vitor/Trabalho Token Ring - Escravos/.idea/` | `.gitignore` | 238 | 11 | Configuração Git IDE | Vitor |
| `parte de Camila/Trabalho de SD/` | `Escravo.java` | 1.481 | 54 | Código Fonte Java | Camila |
| `parte de Camila/Trabalho de SD/` | `Escravo.class` | 1.578 | — | Bytecode Compilado | Camila |

**Observação sobre Git e ambiente:**
- O diretório raiz ainda não possuía um repositório `.git` ativo localmente.
- Nenhuma dependência externa (Maven/Gradle) foi encontrada; todas as implementações usam Java SE Standard Library puro (`java.net.*`, `java.io.*`, `java.nio.*`).

---

## 3. ANÁLISE TÉCNICA INDIVIDUAL DAS CONTRIBUIÇÕES

### 3.1. Heitor — Comunicação UDP (`Message.java`)
- **Papel:** Implementação de referência para a camada de transporte de dados via UDP.
- **Estrutura de Dados:**
  - Atributos: `public int valor;`, `public int operacao;`, `public String ip;`, `public int porta;`.
  - Serialização: Utiliza `ByteBuffer.allocate(8)` gravando `valor` (4 bytes) e `operacao` (4 bytes).
  - Envelope: `DatagramPacket(dados, dados.length, InetAddress.getByName(this.ip), this.porta)`.
  - Recepção: `socketReceive.receive(pacote)` e desserialização com `ByteBuffer.wrap(pacote.getData(), 0, pacote.getLength())`.
- **Pontos Fortes:**
  - Serialização binária eficiente, compacta (exatamente 8 bytes), independente de codificações textuais ou locale.
  - Ordem de bytes padrão de rede (*Big-Endian* garantida pelo `ByteBuffer`).
- **Problemas e Incompatibilidades Identificados:**
  1. *Porta de escuta fixa:* O método `receiveUDP()` fixa `portaEscuta = 5000`. Em uma topologia de anel com 4 processos (ou no teste local em uma mesma máquina), múltiplos processos não podem escutar simultaneamente na porta 5000 (`java.net.BindException: Address already in use`).
  2. *Ciclo de vida do Socket:* O método cria um novo `DatagramSocket(portaEscuta)` dentro de um bloco *try-with-resources* e o fecha a cada pacote recebido. Isso degrada o desempenho e impede o reaproveitamento do socket local durante múltiplas voltas.
  3. *Tratamento de Exceções:* Em caso de erro na recepção, retorna `null` sem propagar a causa ou permitir rearmar o timeout.
- **Decisão de Integração:**
  - Manter o formato binário de 8 bytes de Heitor como padrão oficial da mensagem (`ByteBuffer` com `valor` e `operacao`).
  - Modularizar a comunicação permitindo portas de escuta configuráveis por processo e mantendo o socket aberto durante o ciclo de vida da execução.

---

### 3.2. Isaac — Processo Mestre (`Mestre.java`)
- **Papel:** Implementação do processo coordenador (Processo 0).
- **Interface CLI:**
  - Validação estrita dos 3 parâmetros obrigatórios: `java Mestre <id=0> <operacao: 0=+1 | 1=*2> <voltas>`.
  - Suporte inteligente a argumentos extras opcionais para testes de rede: `[ipEscravo1] [portaEscravo1] [portaMestre]`.
- **Fluxo do Algoritmo:**
  - Inicializa o token com 2.
  - Abre o `DatagramSocket` na porta do Mestre (padrão 5000).
  - Itera `for (int volta = 1; volta <= voltas; volta++)`:
    - Empacota o token e a operação em `ByteBuffer.allocate(8)`.
    - Envia via UDP para o Escravo 1.
    - Bloqueia e recebe o token de volta do Escravo 3.
    - Executa a operação configurada no Mestre (incrementar ou multiplicar).
    - Exibe o progresso no terminal.
  - Ao final de todas as voltas, exibe: `Resultado final = <token>`.
- **Pontos Fortes:**
  - Totalmente compatível com a serialização binária de Heitor (`ByteBuffer` de 8 bytes).
  - Respeita rigorosamente os 3 parâmetros exigidos pelo professor.
  - Lógica do anel correta (envia para Escravo 1 e recebe do Escravo 3).
- **Problemas e Lacunas:**
  1. *Ausência de Timeout:* O socket não define `setSoTimeout()`. Se o anel quebrar, o processo trava indefinidamente.
  2. *Ausência do Intervalo de 5 Segundos:* Não possui a contagem regressiva visual antes da transmissão, requisito mandatório para a demonstração didática.
  3. *Configuração de Rede:* Não lê arquivo externo `rede.properties`.
- **Decisão de Integração:**
  - O código do Isaac será a espinha dorsal do `Mestre.java` final.
  - Serão integrados: o leitor de `rede.properties`, o temporizador visual de 5 segundos, timeout no socket e interface ANSI.

---

### 3.3. Vitor — Implementação Completa de Mestre e Escravo
- **Papel:** Implementação dos nós Escravos (`Escravo.java`) e versão alternativa do Mestre (`Mestre.java`).
- **Interface CLI:**
  - Exige 4 parâmetros: `java Escravo ID OPERACAO VOLTAS IP_PROXIMO`.
- **Comunicação e Formato:**
  - Serialização textual: converte inteiro para String e obtém bytes via UTF-8 (`String.valueOf(token).getBytes(StandardCharsets.UTF_8)`).
  - Lê com buffer de 1024 bytes e `Integer.parseInt(mensagem)`.
  - Todas as instâncias utilizam a mesma porta fixa `PORTA = 5000`.
- **Pontos Fortes:**
  - Tratamento de exceções exemplar: captura `SocketTimeoutException`, `NumberFormatException`, `IllegalArgumentException` e `IOException` com mensagens claras.
  - Implementa controle de timeout no socket: `socket.setSoTimeout(120000)`.
  - Estrutura completa de loop nos Escravos (recebe, opera, encaminha, decrementa voltas e encerra com limpeza).
- **Problemas e Incompatibilidades:**
  1. *Incompatibilidade de Parâmetros:* Exige 4 argumentos na linha de comando, violando o comando exigido pelo professor (`java Escravo 1 0 1`).
  2. *Porta Única (5000):* Torna impossível a execução e o teste em uma única máquina (loopback), gerando conflito de portas.
  3. *Incompatibilidade de Payload:* Serialização em texto (UTF-8) colide com a serialização binária de Heitor e Isaac.
- **Decisão de Integração:**
  - O loop de execução, o tratamento robusto de exceções e a estratégia de encerramento de Vitor serão diretamente incorporados no `Escravo.java` integrado.
  - O transporte de dados será convertido para o padrão binário de Heitor e as portas serão flexibilizadas.

---

### 3.4. Camila — Implementação do Escravo (`Escravo.java`)
- **Papel:** Implementação do processo Escravo com foco na interface e regras de negócio.
- **Interface CLI:**
  - Exige exatamente 3 parâmetros: `java Escravo <identificador> <operacao> <voltas>`.
  - Validações completas: `identificador` entre 1 e 3; `operacao` igual a 0 ou 1; `voltas > 0`.
- **Lógica de Processamento:**
  - Modulariza o cálculo no método puro `processarToken(int token, int operacao)`:
    ```java
    public static int processarToken(int token, int operacao) {
        if (operacao == 0) return token + 1;
        else if (operacao == 1) return token * 2;
        return token;
    }
    ```
- **Pontos Fortes:**
  - Alinhamento perfeito com os comandos oficiais exigidos pelo professor.
  - Código limpo, didático e método de processamento isolado e reutilizável.
- **Problemas e Lacunas:**
  1. *Comunicação UDP Ausente:* A implementação ainda não possuía a camada de sockets de rede (utilizava variável local de teste `int token = 5`).
- **Decisão de Integração:**
  - A validação de parâmetros e o método `processarToken` de Camila serão a base do cálculo matemático no `Escravo.java` final.
  - A camada de rede de Vitor/Heitor será acoplada a essa estrutura.

---

### 3.5. Miqueias — Integração, Redes e Testes
- **Papel:** Engenheiro Coordenador de Integração.
- **Responsabilidades:**
  1. Projetar e implementar a arquitetura final unificada.
  2. Resolver o conflito entre 3 parâmetros de linha de comando e configuração de endereços IP/portas através de um gerenciador de configurações (`config/rede.properties` com fallback inteligente para defaults locais).
  3. Unificar o protocolo de transporte no formato binário de Heitor com suporte a múltiplas portas locais (5000, 5001, 5002, 5003).
  4. Implementar a contagem regressiva visual de 5 segundos e a interface ANSI para terminal.
  5. Estruturar testes automatizados e procedimentos para execução nos 4 notebooks físicos em rede Wi-Fi.
  6. Gerar a documentação técnica acadêmica completa com mais de 20 questões para banca examinadora.

---

## 4. MATRIZ DE COMPATIBILIDADE E APROVEITAMENTO

| Integrante | Componente Original | Status de Compatibilidade | Decisão Técnica de Integração |
| :--- | :--- | :--- | :--- |
| **Heitor** | `Message.java` (Sockets UDP e `ByteBuffer`) | Parcialmente compatível (porta fixa e ciclo de socket efêmero) | **Aproveitado:** O padrão de serialização binária `ByteBuffer` (8 bytes) e a lógica de envelope UDP são o núcleo da classe `comunicacao.MensagemToken` e da infraestrutura de envio/recebimento. As portas tornam-se dinâmicas e o socket permanece persistente. |
| **Isaac** | `Mestre.java` (Processo 0, loop e controle de voltas) | Altamente compatível com o professor e com Heitor | **Aproveitado:** Serve de base direta para o `src/Mestre.java`. Integração com a contagem de 5 segundos, timeout de segurança, leitura de `rede.properties` e painel ANSI. |
| **Camila** | `Escravo.java` (Validação estrita de 3 parâmetros e `processarToken`) | Compatível com a interface do professor, sem camada UDP | **Aproveitado:** A estrutura de validação de argumentos e o método `processarToken(token, operacao)` compõem a lógica central de `src/Escravo.java`. |
| **Vitor** | `Escravo.java` / `Mestre.java` (Loop de rede e captura de erros) | Incompatível em portas/serialização, mas excelente em controle de ciclo de vida | **Aproveitado:** A rotina do loop de recepção de voltas, encerramento gracioso e o bloco de tratamento de exceções com timeout (`SocketTimeoutException`) de Vitor são integrados diretamente no `src/Escravo.java`. |
| **Miqueias** | Coordenação, Arquitetura, Visual e Testes | Nova camada integradora | **Criado:** `config/rede.properties`, utilitários de exibição visual (`util/TerminalUI.java`), temporizador de 5s, suíte de testes locais e distribuídos, e documentação técnica unificada. |

---

## 5. CONCLUSÃO DA AUDITORIA
Nenhum código foi descartado. Todas as cinco contribuições têm papel fundamental e complementar na solução integrada final:
- O protocolo de rede de **Heitor** conecta-se com o Mestre de **Isaac**.
- A elegância dos parâmetros e lógica de **Camila** une-se à robustez de fluxo de rede de **Vitor**.
- A arquitetura, configuração distribuída, temporização e testes de **Miqueias** viabilizam o funcionamento perfeito tanto em modo local quanto em 4 notebooks distintos.
