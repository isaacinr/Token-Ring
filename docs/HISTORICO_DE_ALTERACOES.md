# HISTÓRICO CRONOLÓGICO DE ALTERAÇÕES E INTEGRAÇÃO
**Projeto:** Token Ring UDP em Java — Sistemas Distribuídos  
**Coordenador:** Miqueias  
**Data:** 22/11/2026  

---

## 1. VISÃO GERAL DO PROCESSO DE INTEGRAÇÃO
Este documento registra formalmente todas as transformações, refatorações e integrações efetuadas sobre os códigos originais desenvolvidos pelos cinco membros da equipe acadêmica (Heitor, Isaac, Vitor, Camila e Miqueias).

O princípio norteador foi a **máxima preservação da autoria original**, convertendo divergências pontuais em uma arquitetura cooperativa coesa.

---

## 2. REGISTRO CRONOLÓGICO DETALHADO DAS ALTERAÇÕES

### Registro 01: Criação da Camada de Transporte e Envelope (`src/comunicacao/MensagemToken.java` e `Message.java`)
- **Arquivo Modificado / Criado:** `src/comunicacao/MensagemToken.java` e adaptador `src/comunicacao/Message.java`
- **Responsável Original:** Heitor (`parte Heitor/Message.java`)
- **Situação Anterior:**
  - O método `receiveUDP()` continha a porta de escuta rigidamente fixada em `5000` (`int portaEscuta = 5000;`).
  - O método criava e destruía uma nova instância de `DatagramSocket` a cada leitura de pacote dentro de um bloco *try-with-resources*.
  - A classe pertencia ao pacote padrão (sem declaração de `package`).
- **Situação Posterior:**
  - Criada a classe `MensagemToken` que isola o payload binário de 8 bytes (`ByteBuffer.allocate(8)` com `valor` e `operacao`), preservando rigorosamente o padrão de bytes de Heitor.
  - O método de recepção `receberUDP(DatagramSocket socket)` agora recebe o socket persistente como parâmetro, sem recriar sockets nem fechar prematuramente a porta.
  - A classe `Message` original de Heitor foi mantida como adaptador compatível com sobrecarga para especificar porta de escuta (`receiveUDP(int portaEscuta)`).
- **Justificativa Técnica:**
  - Em um anel distribuído ou local de 4 nós, cada processo precisa escutar em sua respectiva porta (5000 a 5003). Fechar o socket a cada volta impedia a continuidade da escuta e causava overhead e risco de `BindException`.
- **Impacto no Comportamento:**
  - Preservou a serialização binária ultrarrápida de 8 bytes de Heitor, permitindo comunicação contínua e sem falhas entre Mestre e Escravos em qualquer porta.

---

### Registro 02: Evolução do Processo Mestre (`src/Mestre.java`)
- **Arquivo Modificado / Criado:** `src/Mestre.java`
- **Responsável Original:** Isaac (`parte Isaac/Token Ring/Mestre.java`)
- **Situação Anterior:**
  - Código excelente e alinhado com a chamada do professor (`java Mestre 0 <operacao> <voltas>`).
  - Não possuía o intervalo visual de 5 segundos estipulado pelo professor para a apresentação aos avaliadores.
  - As portas e IPs dependiam de argumentos adicionais na linha de comando (`args[3]`, `args[4]`, `args[5]`) ou defaults estáticos.
  - O socket UDP não definia tempo limite de espera (*timeout*), podendo bloquear para sempre se um escravo falhasse.
- **Situação Posterior:**
  - O código de Isaac foi adotado como o núcleo de `src/Mestre.java`.
  - Integrado o utilitário `TerminalUI.contagemRegressiva(5)` antes do envio do token ao Escravo 1.
  - Integrado o gerenciador `ConfiguracaoRede`, permitindo ler `config/rede.properties` automaticamente mantendo compatibilidade com os parâmetros opcionais originais de Isaac.
  - Adicionado `socket.setSoTimeout(120000)` e tratamento de `SocketTimeoutException` com saída amigável.
- **Justificativa Técnica:**
  - Atende à exigência pedagógica da contagem de 5s para que a banca acompanhe a circulação visualmente, protege o processo contra travamentos eternos na rede e desacopla a infraestrutura de IPs.
- **Impacto no Comportamento:**
  - O Mestre agora executa com visualização didática no terminal, avisa em caso de quebra de anel e pode rodar tanto em localhost quanto em 4 notebooks com o mesmo comando.

---

### Registro 03: Harmonização e Unificação dos Escravos (`src/Escravo.java`)
- **Arquivo Modificado / Criado:** `src/Escravo.java`
- **Responsáveis Originais:** Camila (`parte de Camila/Trabalho de SD/Escravo.java`) e Vitor (`parte Vitor/Trabalho Token Ring - Escravos/Escravo.java`)
- **Situação Anterior:**
  - **Camila:** Tinha validação estrita dos 3 parâmetros do professor e o método puro `processarToken(int token, int operacao)`, mas não havia implementado a infraestrutura de rede UDP.
  - **Vitor:** Tinha a infraestrutura completa de loop UDP e captura refinada de exceções, mas exigia 4 argumentos na CLI (`java Escravo ID OPERACAO VOLTAS IP_PROXIMO`), serializava o token como texto UTF-8 (incompatível com o binário de Heitor e Isaac) e fixava todas as portas em 5000.
- **Situação Posterior:**
  - Unificadas as duas contribuições em uma única classe `src/Escravo.java`.
  - Adotada a interface de 3 parâmetros e a função `processarToken` de Camila.
  - Adotado o loop de processamento por voltas, o encerramento gracioso e a hierarquia de captura de exceções de Vitor.
  - O transporte foi conectado à serialização binária de Heitor e a resolução de portas ao `ConfiguracaoRede`.
  - Inserido o intervalo visual didático de 5 segundos com animação regressiva.
- **Justificativa Técnica:**
  - Nenhuma das duas implementações precisou ser descartada. As virtudes conceituais e matemáticas de Camila fundiram-se à robustez de fluxo de controle de Vitor.
- **Impacto no Comportamento:**
  - Uma única classe `Escravo.java` atende aos nós 1, 2 e 3 com o comando padrão do professor, com tolerância a falhas, visual elegante e terminação automática.

---

### Registro 04: Isolamento de Portas e Centralização de Rede (`src/comunicacao/ConfiguracaoRede.java`)
- **Arquivo Modificado / Criado:** `src/comunicacao/ConfiguracaoRede.java` e `config/rede.properties.example`
- **Responsável Original:** Miqueias (Integração)
- **Situação Anterior:**
  - Portas espalhadas no código como constantes mágicas (`5000`, `5001`).
  - Conflito entre execução em máquina única (onde 4 processos na porta 5000 colidem) e execução distribuída.
- **Situação Posterior:**
  - Portas centralizadas: Mestre=5000, Escravo1=5001, Escravo2=5002, Escravo3=5003.
  - Leitura sob demanda de `config/rede.properties`, com fallback automático para `127.0.0.1` sem requerer configurações manuais para testes locais.
- **Justificativa Técnica:**
  - Atende à diretriz: *"A correspondência de portas deverá estar centralizada na configuração de rede. Não espalhe números de portas pelo código."*
- **Impacto no Comportamento:**
  - Permite testes unitários imediatos no notebook do desenvolvedor e transição transparente para os 4 notebooks da apresentação.

---

### Registro 05: Interface Visual e Contagem Didática (`src/util/TerminalUI.java`)
- **Arquivo Modificado / Criado:** `src/util/TerminalUI.java`
- **Responsável Original:** Miqueias (Integração)
- **Situação Anterior:**
  - Mensagens puras em `System.out.println` sem padronização visual entre integrantes.
  - Falta de identificação clara da origem, valor anterior e valor atual.
- **Situação Posterior:**
  - Criação de molduras estruturadas em ASCII com suporte seguro a cores ANSI.
  - Método `contagemRegressiva(5)` que imprime `[5s] [4s] [3s] [2s] [1s] -> TRANSMITINDO!` na mesma linha sem limpar a tela.
- **Justificativa Técnica:**
  - O professor e a banca precisam ler todo o histórico de execuções sem que a tela seja apagada.
- **Impacto no Comportamento:**
  - Apresentação didática de alto nível visual nos quatro terminais.

---

## 3. TABELA RESUMO DAS MODIFICAÇÕES

| Arquivo Original | Arquivo Integrado | O Que Mudou | Motivo |
| :--- | :--- | :--- | :--- |
| `parte Heitor/Message.java` | `src/comunicacao/MensagemToken.java` e `src/comunicacao/Message.java` | Socket agora é injetado/reutilizável; portas dinâmicas | Permitir múltiplos nós e evitar recriação de sockets por pacote |
| `parte Isaac/Token Ring/Mestre.java` | `src/Mestre.java` | Inclusão de countdown de 5s, timeout de 120s, integração com `rede.properties` | Atender aos requisitos pedagógicos e robustez em rede |
| `parte de Camila/Trabalho de SD/Escravo.java` | `src/Escravo.java` | Acoplamento de rede UDP ao método `processarToken` e à validação de 3 argumentos | Camila não havia adicionado sockets de rede |
| `parte Vitor/.../Escravo.java` | `src/Escravo.java` | Conversão de payload textual para binário de Heitor; portas dinâmicas | Vitor usava porta fixa 5000 e String UTF-8 |
| Novo módulo | `src/comunicacao/ConfiguracaoRede.java` | Criação de gerenciador de IPs/portas e leitura de `.properties` | Centralizar rede e viabilizar os 4 notebooks físicos |
| Novo módulo | `src/util/TerminalUI.java` | Painéis e contagem de 5 segundos com cores ANSI | Apresentação didática no terminal sem apagar o histórico |
| Novo módulo | `src/testes/TesteTokenRingAutomated.java` | Bateria de testes unitários e de integração automatizados | Garantir que os 4 cenários do professor passem com 100% de exatidão |
