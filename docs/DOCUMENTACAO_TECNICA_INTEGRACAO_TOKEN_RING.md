# DOCUMENTAÇÃO TÉCNICA E ACADÊMICA — PROJETO TOKEN RING UDP
**Disciplina:** Sistemas Distribuídos  
**Instituição:** Universidade  
**Repositório Oficial:** https://github.com/isaacinr/Token-Ring  
**Equipe de Desenvolvimento:**
- **Heitor:** Especialista em Transporte e Comunicação UDP (`Message.java`)
- **Isaac:** Desenvolvedor do Processo Mestre (`Mestre.java`)
- **Camila:** Desenvolvedora de Regras de Negócio e Validações do Escravo (`Escravo.java`)
- **Vitor:** Desenvolvedor de Tolerância a Falhas e Ciclo de Vida do Escravo (`Escravo.java`)
- **Miqueias:** Engenheiro Coordenador de Integração, Redes e Testes  

---

## SUMÁRIO
1. [Seção A — Fundamentos Conceituais do Projeto](#seção-a--fundamentos-conceituais-do-projeto)
2. [Seção B — Arquitetura de Software e Componentes](#seção-b--arquitetura-de-software-e-componentes)
3. [Seção C — Explicação Detalhada do Código-Fonte](#seção-c--explicação-detalhada-do-código-fonte)
4. [Seção D — Engenharia de Comunicação UDP](#seção-d--engenharia-de-comunicação-udp)
5. [Seção E — Relatório Detalhado da Integração por Miqueias](#seção-e--relatório-detalhado-da-integração-por-miqueias)
6. [Seção F — Guia de Configuração dos Quatro Notebooks](#seção-f--guia-de-configuração-dos-quatro-notebooks)
7. [Seção G — Roteiro de Demonstração Acadêmica](#seção-g--roteiro-de-demonstração-acadêmica)
8. [Seção H — Banco de Questões da Banca Examinadora (20+ Perguntas e Respostas)](#seção-h--banco-de-questões-da-banca-examinadora-20-perguntas-e-respostas)

---

## SEÇÃO A — FUNDAMENTOS CONCEITUAIS DO PROJETO

### 1. O que é um Sistema Distribuído?
Um sistema distribuído é uma coleção de nós computacionais autônomos, interconectados por uma rede de comunicação, que coordenam suas ações mediante troca de mensagens (*message passing*) para atingir um objetivo comum. Cada nó possui seu próprio espaço de endereçamento de memória física e seu próprio relógio local, inexistindo memória compartilhada.

No presente projeto, cada processo representa um nó distribuído que desconhece a memória interna dos demais, dependendo exclusivamente do envio de datagramas UDP para coordenar o estado global.

### 2. O que é a Topologia Token Ring?
A topologia em anel lógico (*Token Ring*) estabelece que cada nó computacional possui exatamente um predecessor fixo e um sucessor fixo. A comunicação ocorre em sentido unidirecional:
$$\text{Processo } i \longrightarrow \text{Processo } (i+1) \pmod N$$

Para $N = 4$ nós:
$$\text{Mestre (0)} \longrightarrow \text{Escravo 1 (1)} \longrightarrow \text{Escravo 2 (2)} \longrightarrow \text{Escravo 3 (3)} \longrightarrow \text{Mestre (0)}$$

### 3. O que Representa o Token?
O **Token** é uma entidade de dados lógica que confere ao nó possuidor a exclusividade de processamento e transmissão. No contexto acadêmico deste trabalho, o token transporta:
1. Um **valor inteiro de estado** (iniciado em 2 pelo Mestre).
2. O **código da operação** correspondente à etapa da transmissão.
Enquanto um processo não detém o token (aguardando no `socket.receive`), ele permanece ocioso. Ao receber o pacote, executa sua computação local e repassa o bastão (*token passing*).

### 4. Como Ocorre a Circulação e Controle de Voltas?
A circulação se dá em ciclos fechados chamados de **voltas**. Uma volta completa ocorre quando o token parte do Mestre, é transformado pelos três escravos em ordem estrita e retorna ao Mestre, onde é submetido à operação local final. A quantidade de voltas $V$ é passada como parâmetro no disparo dos processos.

---

## SEÇÃO B — ARQUITETURA DE SOFTWARE E COMPONENTES

A árvore do projeto foi projetada para combinar elegância profissional e simplicidade didática, sem dependências externas:

```
Token-Ring/
│
├── src/                                  # Código-fonte principal integrado em Java puro
│   ├── Mestre.java                       # Ponto de entrada do Processo 0 (Coordenação)
│   ├── Escravo.java                      # Ponto de entrada único dos Processos 1, 2 e 3
│   │
│   ├── modelo/                           # Camada de domínio do sistema
│   │   └── OperacaoToken.java            # Enumeração e lógica matemática (+1 e *2)
│   │
│   ├── comunicacao/                      # Camada de rede e transporte UDP
│   │   ├── MensagemToken.java            # Serialização binária em ByteBuffer (8 bytes)
│   │   ├── Message.java                  # Adaptador preservando a API concebida por Heitor
│   │   └── ConfiguracaoRede.java         # Mapeador de topologia, IPs e portas
│   │
│   ├── util/                             # Utilitários de terminal e visualização
│   │   └── TerminalUI.java               # Molduras ANSI e contador regressivo de 5s
│   │
│   └── testes/                           # Suíte de verificação automatizada
│       └── TesteTokenRingAutomated.java  # Validação unitária e de integração
│
├── config/                               # Arquivos de infraestrutura
│   └── rede.properties.example           # Modelo comentado para preenchimento dos IPs reais
│
├── docs/                                 # Documentação oficial exigida
│   ├── RELATORIO_AUDITORIA_INICIAL.md    # Inventário e diagnóstico das 5 partes originais
│   ├── DOCUMENTACAO_TECNICA_INTEGRACAO_TOKEN_RING.md # Documento técnico mestre
│   ├── CONFIGURACAO_REDE_LOCAL.md        # Manual de rede Wi-Fi e portas para os 4 PCs
│   ├── HISTORICO_DE_ALTERACOES.md        # Registro cronológico detalhado das mudanças
│   └── TESTES.md                         # Protocolo de testes com provas matemáticas
│
├── scripts/                              # Scripts de automação em Bash
│   ├── compilar.sh                       # Compilador via javac
│   ├── executar_mestre.sh                # Atalho de execução do Mestre
│   ├── executar_escravo1.sh              # Atalho de execução do Escravo 1
│   ├── executar_escravo2.sh              # Atalho de execução do Escravo 2
│   ├── executar_escravo3.sh              # Atalho de execução do Escravo 3
│   ├── executar_todos_local.sh           # Orquestrador local de 4 processos
│   └── executar_testes.sh                # Executador da suíte de testes
│
├── backup/                               # Preservação imutável dos arquivos originais
│   └── originais/                        # Submissões intactas de Heitor, Isaac, Vitor e Camila
│       ├── MANIFESTO_HASHES_SHA256.md    # Manifesto com assinaturas SHA-256
│       └── VerificadorManifesto.java     # Validador de integridade dos backups
│
├── README.md                             # Apresentação do repositório no GitHub
└── .gitignore                            # Proteção contra commit de IPs, .class e backups
```

---

## SEÇÃO C — EXPLICAÇÃO DETALHADA DO CÓDIGO-FONTE

### 1. `Mestre.java`
- **Responsabilidade:** Inicializar a circulação do token, sincronizar as voltas, aplicar sua operação matemática e apresentar o resultado final.
- **Parâmetros de Entrada:**
  `java Mestre <id=0> <operacao: 0 ou 1> <voltas> [ipEscravo1] [portaEscravo1] [portaMestre]`
- **Atributos Principais:**
  - `INTERVALO_DIDATICO_SEGUNDOS = 5`: define o tempo de espera visual antes da transmissão.
- **Método `main(String[] args)`:**
  1. *Validação:* Checa se `args.length >= 3`, valida se `id == 0`, se `operacao` é 0 ou 1, e se `voltas >= 1`.
  2. *Carregamento da Rede:* Instancia `ConfiguracaoRede` que consulta `config/rede.properties` ou aplica defaults locais (porta 5000 para escuta e 5001 para destino).
  3. *Inicialização do Token:* Atribui `int token = 2`.
  4. *Abertura do Socket:* Instancia `DatagramSocket(portaEscuta)` com timeout configurado para 120s.
  5. *Loop de Voltas:* Para cada volta:
     - Imprime os dados de início da volta no terminal.
     - Chama `TerminalUI.contagemRegressiva(5)`.
     - Serializa o token em `MensagemToken` e envia para `enderecoDestino:portaDestino` via `socket.send()`.
     - Bloqueia em `MensagemToken.receberUDP(socket)` aguardando o pacote devolvido pelo Escravo 3.
     - Ao receber, aplica `OperacaoToken.aplicar(token, codigoOperacao)`.
  6. *Conclusão:* Após o loop de voltas, exibe o painel de resultado final chamando `TerminalUI.exibirResultadoFinal(token, voltas)` e fecha o socket automaticamente no encerramento do bloco *try-with-resources*.

### 2. `Escravo.java`
- **Responsabilidade:** Processar o token recebido pelo nó anterior, executar sua operação local e repassar para o próximo nó. Uma única classe atende a qualquer um dos três nós escravos através do seu parâmetro ID.
- **Parâmetros de Entrada:**
  `java Escravo <id: 1, 2 ou 3> <operacao: 0 ou 1> <voltas> [ipProximo] [portaProximo] [portaEscravo]`
- **Mapeamento Topológico Automático:**
  - Se ID = 1: Escuta na porta 5001; Destino é Escravo 2 (porta 5002); Origem é Mestre (0).
  - Se ID = 2: Escuta na porta 5002; Destino é Escravo 3 (porta 5003); Origem é Escravo 1 (1).
  - Se ID = 3: Escuta na porta 5003; Destino é Mestre (porta 5000); Origem é Escravo 2 (2).
- **Método `processarToken(int token, int operacao)`:**
  - Herança direta do método concebido por Camila: recebe o valor inteiro e a operação, devolvendo o novo inteiro calculado.

### 3. `comunicacao/MensagemToken.java`
- **Responsabilidade:** Empacotar e desempacotar o token em formato binário estável.
- **Método `serializar()`:**
  - Aloca um `ByteBuffer` de 8 bytes na memória nativa:
    ```java
    ByteBuffer buffer = ByteBuffer.allocate(8);
    buffer.putInt(this.valor);
    buffer.putInt(this.operacao);
    return buffer.array();
    ```
- **Método `desserializar(byte[] dados, int offset, int length)`:**
  - Envolve os bytes recebidos via `ByteBuffer.wrap()` e extrai os dois inteiros consecutivamente em Big-Endian:
    ```java
    ByteBuffer byteBuffer = ByteBuffer.wrap(dados, offset, length);
    int valor = byteBuffer.getInt();
    int op = byteBuffer.getInt();
    return new MensagemToken(valor, op);
    ```

### 4. `comunicacao/ConfiguracaoRede.java`
- **Responsabilidade:** Isolar toda e qualquer menção a endereços IP e números de portas do código de controle.
- **Estratégia de Busca:**
  1. Verifica se existe o arquivo `config/rede.properties`.
  2. Se existir, extrai as chaves `mestre.host`, `mestre.port`, `escravoX.host`, `escravoX.port`.
  3. Se não existir, utiliza `127.0.0.1` e portas 5000 a 5003 transparentemente.
  4. Permite sobrescrever destinos caso argumentos extras sejam passados via linha de comando.

### 5. `util/TerminalUI.java`
- **Responsabilidade:** Renderizar mensagens padronizadas em todos os terminais.
- **Destaque Técnico:**
  - O método `contagemRegressiva(int segundos)` imprime os segundos na mesma linha (`System.out.print`) com `Thread.sleep(1000)`, evitando o comando de limpeza de tela (`\033[H\033[2J`), de modo que toda a trilha de auditoria permaneça no terminal para conferência do professor.

---

## SEÇÃO D — ENGENHARIA DE COMUNICAÇÃO UDP

### 1. O que é o Protocolo UDP?
O **User Datagram Protocol (UDP)** é um protocolo da camada de transporte da pilha TCP/IP (definido na RFC 768). Ao contrário do TCP:
- É **não orientado a conexão** (*connectionless*): não há *three-way handshake* prévio.
- É **não confiável** (*unreliable*): não há retransmissão automática nativa nem confirmações de recebimento (ACK).
- É **baseado em datagramas**: preserva os limites de cada mensagem, sem intercalação de fluxo contínuo de bytes (*stream*).

### 2. O que são `DatagramSocket` e `DatagramPacket` no Java?
- **`DatagramSocket`:** É o canal de E/S do sistema operacional que se vincula (*bind*) a uma porta UDP local para enviar e receber datagramas.
- **`DatagramPacket`:** É o invólucro do pacote. Contém um array de bytes (`byte[]`), o comprimento dos dados, e — quando usado para envio — o endereço de destino (`InetAddress`) e a porta remota (`port`).

### 3. Formato Real das Mensagens Transmitidas
O pacote transmitido pelo Token Ring possui exatamente **8 bytes** em formato binário padronizado:

```
 0                   1                   2                   3
 0 1 2 3 4 5 6 7 8 9 0 1 2 3 4 5 6 7 8 9 0 1 2 3 4 5 6 7 8 9 0 1
+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
|                 VALOR DO TOKEN (int, 4 bytes)                 |
+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
|              CÓDIGO DA OPERAÇÃO (int, 4 bytes)                |
+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
```
- **Codificação:** Binária pura em complemento de dois (*signed 32-bit integer*).
- **Ordem de Bytes (Endianness):** Big-Endian (ordem padrão de rede - MSB no byte 0).

---

## SEÇÃO E — RELATÓRIO DETALHADO DA INTEGRAÇÃO POR MIQUEIAS

A integração coordenada por Miqueias obedeceu às melhores práticas de Engenharia de Software:
1. **Auditoria Prévia:** Identificou-se que Heitor e Isaac já compartilhavam o mesmo formato de 8 bytes com `ByteBuffer`, o que foi mantido como espinha dorsal.
2. **Resolução do Conflito entre Camila e Vitor:**
   - Camila trouxe a conformidade absoluta aos 3 argumentos do professor e o método puro `processarToken`.
   - Vitor trouxe a robustez de fluxo de controle, timeout de 120s e encerramento gracioso.
   - Ambas foram combinadas na classe `src/Escravo.java`.
3. **Resolução de Portas e IPs:** Eliminou-se a limitação de porta única 5000 de Vitor que impedia testes locais, adotando-se o modelo de portas 5000 a 5003 com arquivo externo `rede.properties`.

### Tabela Oficial de Transformação de Código
| Arquivo Original | Integrante | Alteração Realizada | Motivo Técnico | Resultado Obtido |
| :--- | :--- | :--- | :--- | :--- |
| `parte Heitor/Message.java` | Heitor | Socket desacoplado do ciclo de leitura; portas dinâmicas | O socket era recriado a cada leitura na porta fixa 5000 | Conexão UDP estável e compatível com 4 processos simultâneos |
| `parte Isaac/.../Mestre.java` | Isaac | Adicionados contagem regressiva de 5s, timeout de 120s e leitura de `rede.properties` | Requisito didático do professor e resiliência em rede | Mestre autônomo, configurável e visualmente didático |
| `parte de Camila/.../Escravo.java` | Camila | Adicionada camada de transporte UDP ao redor de `processarToken` | A implementação continha apenas testes locais com `token=5` | Lógica pura de Camila promovida a nó distribuído funcional |
| `parte Vitor/.../Escravo.java` | Vitor | Migrado payload textual para binário de Heitor e flexibilizadas as portas | Vitor usava porta fixa 5000 e String UTF-8 | Robustez e tratamento de erro de Vitor integrados ao padrão da equipe |

---

## SEÇÃO F — GUIA DE CONFIGURAÇÃO DOS NOTEBOOKS

1. Conecte os 4 notebooks ao mesmo Wi-Fi.
2. Obtenha o IP com `hostname -I` (Linux) ou `ipconfig` (Windows).
3. Crie `config/rede.properties` em cada máquina:
   ```properties
   mestre.host=192.168.1.100
   mestre.port=5000
   escravo1.host=192.168.1.101
   escravo1.port=5001
   escravo2.host=192.168.1.102
   escravo2.port=5002
   escravo3.host=192.168.1.103
   escravo3.port=5003
   socket.timeout.ms=120000
   ```
4. Libere as portas UDP no firewall de cada SO (5000 no PC 1, 5001 no PC 2, 5002 no PC 3, 5003 no PC 4).
5. Compile o projeto: `scripts/compilar.sh`.
6. Inicie na ordem: Escravo 3 $\to$ Escravo 2 $\to$ Escravo 1 $\to$ Mestre.

---

## SEÇÃO G — ROTEIRO DE DEMONSTRAÇÃO ACADÊMICA

Para uma apresentação impecável diante do professor:
1. **Posicionamento dos Computadores:**
   - Notebook 1 (Mestre): posicionado à esquerda do professor.
   - Notebooks 2, 3 e 4 (Escravos 1, 2 e 3): alinhados sequencialmente em círculo ou linha na mesa.
2. **Roteiro dos Casos de Teste Solicitados:**
   - **Caso 1 (Todos +1, 1 Volta):** Mestre dispara `java Mestre 0 0 1`. Acompanhar o token ir de 2 para 3 no Escravo 1, para 4 no Escravo 2, para 5 no Escravo 3, e retornar ao Mestre resultando em **6**.
   - **Caso 2 (Todos *2, 1 Volta):** Disparar com operação 1. Mostrar no terminal a evolução 2 $\to$ 4 $\to$ 8 $\to$ 16 $\to$ **32**.
   - **Caso 3 (Duas Voltas +1):** Mostrar a volta 1 gerando 6 e a volta 2 reinjetando 6 e finalizando em **10**.
3. **Papel de Miqueias durante a Apresentação:**
   - Miqueias atuará explicando a arquitetura de integração, o protocolo de serialização binária de Heitor, as proteções de timeout de Vitor, a lógica de negócio de Camila e o controle de voltas de Isaac.

---

## SEÇÃO H — BANCO DE QUESTÕES DA BANCA EXAMINADORA (20+ QUESTÕES)

### Q01: Por que foi escolhido UDP em vez de TCP para este trabalho?
**R:** Porque o professor especificou expressamente o UDP e porque o Token Ring troca mensagens pontuais de controle de estado. O UDP elimina a sobrecarga de aperto de mão (*handshake*) e encerramento de conexão do TCP.

### Q02: Como o sistema impede que dois processos entrem em conflito de portas ao rodar no mesmo computador?
**R:** O sistema adota portas distintas para cada processo: Mestre (5000), Escravo 1 (5001), Escravo 2 (5002) e Escravo 3 (5003).

### Q03: Qual o formato exato da mensagem enviada na rede?
**R:** Exatamente 8 bytes contendo dois números inteiros de 4 bytes gravados em Big-Endian através de `ByteBuffer`: os primeiros 4 bytes contêm o valor do token e os 4 bytes subsequentes contêm a operação.

### Q04: O que acontece se o pacote UDP for perdido na rede Wi-Fi?
**R:** O UDP não retransmite pacotes perdidos. No nosso código, o processo receptor aguarda até atingir o tempo limite configurado (`socket.setSoTimeout(120000)`), momento em que captura `SocketTimeoutException` e exibe mensagem amigável sem travar o terminal.

### Q05: Por que os escravos devem ser ligados antes do Mestre?
**R:** Como o UDP não estabelece conexão e não tem fila de persistência se o receptor não estiver escutando, o Escravo 1 precisa estar com seu socket aberto e bloqueado em `receive` antes do Mestre enviar o primeiro pacote.

### Q06: Como os escravos sabem quando devem encerrar sua execução?
**R:** Cada escravo recebe a quantidade total de voltas pela linha de comando (`args[2]`). Ele mantém um contador local `for (int volta = 1; volta <= voltas; volta++)`. Ao processar a última volta e enviar para o sucessor, ele encerra seu loop e fecha o socket graciosamente.

### Q07: Como o Mestre sabe quando não deve mais enviar o token?
**R:** O Mestre controla o loop de voltas. Na última volta, após receber o token do Escravo 3 e aplicar sua própria operação matemática, o laço termina e ele imprime o resultado final sem executar um novo envio.

### Q08: Qual o papel da classe `ConfiguracaoRede`?
**R:** Desacoplar os endereços IP e portas do código das regras de negócio, permitindo que a aplicação leia de `config/rede.properties` em 4 notebooks físicos ou use `127.0.0.1` automaticamente sem qualquer parâmetro em testes locais.

### Q09: Por que a contagem regressiva de 5 segundos foi implementada?
**R:** Para proporcionar uma experiência didática que permita à banca examinadora acompanhar com clareza o processamento em cada um dos quatro notebooks físicos.

### Q10: Por que não se deve usar `\033[H\033[2J` para limpar a tela a cada volta?
**R:** Porque o professor precisa auditar o histórico de todas as voltas e operações anteriores sem que sejam apagadas do terminal.

### Q11: Qual foi a contribuição de Heitor para o projeto integrado?
**R:** Heitor desenvolveu a implementação de referência da camada UDP com a serialização binária `ByteBuffer` de 8 bytes na classe `Message.java`, que foi mantida como padrão do protocolo.

### Q12: Qual foi a contribuição de Isaac para o projeto integrado?
**R:** Isaac desenvolveu o processo Mestre (`Mestre.java`), implementando a validação dos 3 parâmetros, a inicialização do token em 2, o controle de voltas e a reinjeção do token.

### Q13: Qual foi a contribuição de Camila para o projeto integrado?
**R:** Camila concebeu a validação estrita dos 3 parâmetros do professor e o método puro `processarToken(int token, int operacao)`.

### Q14: Qual foi a contribuição de Vitor para o projeto integrado?
**R:** Vitor implementou a lógica de loop contínuo dos escravos, o gerenciamento de encerramento por voltas e a captura robusta de exceções com `SocketTimeoutException`.

### Q15: Qual foi a contribuição de Miqueias para o projeto integrado?
**R:** Miqueias coordenou a integração, resolveu as incompatibilidades de portas e parâmetros, implementou a interface visual com contador de 5s, criou a infraestrutura de configuração de rede, escreveu a suíte de testes e redigiu a documentação técnica.

### Q16: O que é e por que ocorre o erro `java.net.BindException: Address already in use`?
**R:** Ocorre quando um processo tenta abrir um `DatagramSocket` em uma porta que já está sendo utilizada por outro processo no mesmo sistema operacional.

### Q17: O que é isolamento de ponto de acesso (*AP Isolation*) e como contorná-lo?
**R:** É uma política de segurança em redes Wi-Fi que bloqueia datagramas diretos entre clientes da mesma rede sem fio. Pode ser contornado utilizando o roteador pessoal do smartphone (hotspot móvel).

### Q18: Como foi garantido que os comandos oficiais exigidos pelo professor continuassem funcionando?
**R:** Mantendo os 3 argumentos originais na linha de comando e extraindo as configurações de rede do arquivo `rede.properties` ou de valores padrão estáticos.

### Q19: O que acontece se for passado um número negativo de voltas?
**R:** Os validadores de entrada em `Mestre.java` e `Escravo.java` barram o valor imediatamente, imprimem mensagem de erro sem lançar crash não tratado e saem com código de erro 1.

### Q20: Como a ordem de bytes (endianness) é tratada na serialização?
**R:** O `ByteBuffer` do Java utiliza Big-Endian (*network byte order*) por padrão, garantindo que mesmo entre arquiteturas de hardware diferentes (x86, ARM) os inteiros sejam lidos de forma idêntica.

### Q21: O que é o endereço 0.0.0.0 utilizado no bind do socket?
**R:** Representa o endereço curinga (*INADDR_ANY*), instruindo o sistema operacional a aceitar datagramas UDP em qualquer interface de rede ativa (loopback, cabo de rede ou Wi-Fi).

### Q22: Por que a cópia dos códigos originais foi preservada em `backup/originais/`?
**R:** Para garantir integridade acadêmica, permitir que a equipe e a banca examinadora confiram as submissões originais de cada estudante e assegurar rastreabilidade com hashes SHA-256.
