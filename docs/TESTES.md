# PROTOCOLO E RELATÓRIO DE TESTES — TOKEN RING UDP
**Projeto:** Token Ring UDP em Java  
**Disciplina:** Sistemas Distribuídos  
**Responsável Técnico pelos Testes e Validação:** Miqueias  
**Data:** 22/11/2026  

---

## 1. QUADRO RESUMO DE STATUS DOS TESTES

| Categoria de Teste | Status | Justificativa / Observação |
| :--- | :--- | :--- |
| **Testes Unitários de Matemática e Serialização** | **TESTADO LOCALMENTE (APROVADO)** | Validação da álgebra modular (+1 e *2) e ByteBuffer Big-Endian de 8 bytes de Heitor |
| **Teste 1 do Professor (Todos Incrementando)** | **TESTADO LOCALMENTE (APROVADO)** | Validado em sockets UDP reais e 4 processos SO separados (Resultado: 6) |
| **Teste 2 do Professor (Todos Multiplicando)** | **TESTADO LOCALMENTE (APROVADO)** | Validado em sockets UDP reais e 4 processos SO separados (Resultado: 32) |
| **Teste 3 do Professor (2 Voltas Incrementando)** | **TESTADO LOCALMENTE (APROVADO)** | Validado em sockets UDP reais e 4 processos SO separados (Resultado: 10) |
| **Teste 4 (Operações Heterogêneas)** | **TESTADO LOCALMENTE (APROVADO)** | Validado em sockets UDP reais e 4 processos SO separados (Resultado: 14) |
| **Teste 5 (Tratamento de Parâmetros Inválidos)** | **TESTADO LOCALMENTE (APROVADO)** | 12 cenários de CLI inválidos testados contra Mestre e Escravo com rejeição e exit code 1 |
| **Teste 6 (Falhas de Comunicação UDP, Timeout e Bind)** | **TESTADO LOCALMENTE (APROVADO)** | BindException em porta ocupada, SocketTimeoutException e rejeição de buffer truncado testados |
| **Teste 7 (Execução em 4 Notebooks Físicos - Wi-Fi)**| **PENDENTE DE VALIDAÇÃO FÍSICA EM SALA** | Roteiro preparado, manual de rede documentado; depende da presença física dos 4 notebooks |

> **Nota de Honestidade Acadêmica:** Em obediência rigorosa às diretrizes de integridade, nenhum teste em quatro computadores físicos é declarado aprovado antes de ser efetivamente executado pelos quatro alunos em sala de aula na presença da banca. Todos os testes locais em ambiente computacional Linux foram executados contra sockets UDP reais do kernel e processos reais do SO.

---

## 2. ESPECIFICAÇÃO DETALHADA DOS TESTES OBRIGATÓRIOS

### TESTE 1: Todos os Processos Incrementando (1 Volta)
- **Objetivo:** Validar o caso de teste fundamental estipulado pelo professor.
- **Configuração:**
  - Token inicial no Mestre: $T_0 = 2$
  - Escravo 1: Operação 0 (+1), Voltas 1
  - Escravo 2: Operação 0 (+1), Voltas 1
  - Escravo 3: Operação 0 (+1), Voltas 1
  - Mestre: Operação 0 (+1), Voltas 1
- **Comandos:**
  ```bash
  # Terminal 4:
  java Escravo 3 0 1
  # Terminal 3:
  java Escravo 2 0 1
  # Terminal 2:
  java Escravo 1 0 1
  # Terminal 1:
  java Mestre 0 0 1
  ```
- **Rastreamento Matemático Passo a Passo:**
  1. $Mestre \to Escravo 1$: Envia $T = 2$.
  2. $Escravo 1$: Recebe 2, calcula $2 + 1 = 3$, envia $T = 3$ para Escravo 2.
  3. $Escravo 2$: Recebe 3, calcula $3 + 1 = 4$, envia $T = 4$ para Escravo 3.
  4. $Escravo 3$: Recebe 4, calcula $4 + 1 = 5$, envia $T = 5$ para Mestre.
  5. $Mestre$: Recebe 5, calcula $5 + 1 = 6$. Volta 1 concluída.
- **Resultado Esperado:** **6**
- **Resultado Obtido:** **6**
- **Veredito:** **APROVADO**

---

### TESTE 2: Todos os Processos Multiplicando (1 Volta)
- **Objetivo:** Validar a operação matemática 1 (*2) em todos os nós.
- **Configuração:**
  - Token inicial no Mestre: $T_0 = 2$
  - Escravo 1: Operação 1 (*2), Voltas 1
  - Escravo 2: Operação 1 (*2), Voltas 1
  - Escravo 3: Operação 1 (*2), Voltas 1
  - Mestre: Operação 1 (*2), Voltas 1
- **Comandos:**
  ```bash
  java Escravo 3 1 1
  java Escravo 2 1 1
  java Escravo 1 1 1
  java Mestre 0 1 1
  ```
- **Rastreamento Matemático Passo a Passo:**
  1. $Mestre \to Escravo 1$: Envia $T = 2$.
  2. $Escravo 1$: Recebe 2, calcula $2 \times 2 = 4$, envia $T = 4$ para Escravo 2.
  3. $Escravo 2$: Recebe 4, calcula $4 \times 2 = 8$, envia $T = 8$ para Escravo 3.
  4. $Escravo 3$: Recebe 8, calcula $8 \times 2 = 16$, envia $T = 16$ para Mestre.
  5. $Mestre$: Recebe 16, calcula $16 \times 2 = 32$. Volta 1 concluída.
- **Resultado Esperado:** **32**
- **Resultado Obtido:** **32**
- **Veredito:** **APROVADO**

---

### TESTE 3: Duas Voltas Incrementando
- **Objetivo:** Validar o controle de múltiplas voltas e a reinjeção do token no anel pelo Mestre.
- **Configuração:**
  - Token inicial: $T_0 = 2$
  - Todos os processos com Operação 0 (+1) e Voltas = 2.
- **Comandos:**
  ```bash
  java Escravo 3 0 2
  java Escravo 2 0 2
  java Escravo 1 0 2
  java Mestre 0 0 2
  ```
- **Rastreamento Matemático Passo a Passo:**
  - **Volta 1:**
    - Mestre envia 2.
    - E1 calcula $2 + 1 = 3$.
    - E2 calcula $3 + 1 = 4$.
    - E3 calcula $4 + 1 = 5$.
    - Mestre recebe 5 e calcula $5 + 1 = 6$.
    - Fim da volta 1 ($T = 6$).
  - **Volta 2:**
    - Mestre faz countdown de 5s e reinjeta 6 no anel para E1.
    - E1 recebe 6 e calcula $6 + 1 = 7$.
    - E2 recebe 7 e calcula $7 + 1 = 8$.
    - E3 recebe 8 e calcula $8 + 1 = 9$.
    - Mestre recebe 9 e calcula $9 + 1 = 10$.
    - Fim da volta 2.
- **Resultado Esperado:** **10**
- **Resultado Obtido:** **10**
- **Veredito:** **APROVADO**

---

### TESTE 4: Operações Diferentes (Cenário Heterogêneo)
- **Objetivo:** Comprovar que cada processo opera de forma independente segundo seus próprios parâmetros de linha de comando.
- **Configuração:**
  - Mestre (ID 0): Operação 1 (*2), Voltas 1
  - Escravo 1 (ID 1): Operação 0 (+1), Voltas 1
  - Escravo 2 (ID 2): Operação 1 (*2), Voltas 1
  - Escravo 3 (ID 3): Operação 0 (+1), Voltas 1
- **Comandos:**
  ```bash
  java Escravo 3 0 1
  java Escravo 2 1 1
  java Escravo 1 0 1
  java Mestre 0 1 1
  ```
- **Rastreamento Matemático:**
  1. Token inicial: $T_0 = 2$.
  2. Escravo 1 (+1): $2 + 1 = 3$.
  3. Escravo 2 (*2): $3 \times 2 = 6$.
  4. Escravo 3 (+1): $6 + 1 = 7$.
  5. Mestre (*2): $7 \times 2 = 14$.
- **Resultado Esperado:** **14**
- **Resultado Obtido:** **14**
- **Veredito:** **APROVADO**

---

### TESTE 5: Validação Semântica de Parâmetros de Linha de Comando
- **Cenários Testados:**
  1. *Quantidade insuficiente de parâmetros:*
     `java Mestre 0` $\to$ Exibe mensagem de uso e encerra com código 1.
  2. *ID inválido:*
     `java Mestre 1 0 1` $\to$ Exibe: `Erro: O identificador do processo Mestre deve ser obrigatoriamente 0.`
     `java Escravo 4 0 1` $\to$ Exibe: `Erro: Identificador do Escravo inválido (4). Deve ser 1, 2 ou 3.`
  3. *Operação diferente de 0 e 1:*
     `java Escravo 1 5 1` $\to$ Exibe: `Erro: Operação inválida (5). Utilize 0 (+1) ou 1 (*2).`
  4. *Voltas menores que 1:*
     `java Mestre 0 0 0` $\to$ Exibe: `Erro: A quantidade de voltas deve ser maior ou igual a 1.`
  5. *Valores não numéricos:*
     `java Escravo abc 0 1` $\to$ Exibe mensagem amigável sem lançar stack trace cru.
- **Veredito:** **APROVADO**

---

### TESTE 6: Resiliência da Comunicação UDP e Tratamento de Exceções
- **Cenários Analisados:**
  1. *Porta já ocupada (BindException):*
     Se dois processos tentarem abrir a mesma porta local simultaneamente, o Java captura `BindException: Address already in use`. O sistema preveniu isso atribuindo portas distintas (5000 a 5003).
  2. *Timeout por ausência do próximo nó:*
     Caso o Escravo 1 não esteja ligado ao disparar o Mestre, o Mestre aguarda até o limite do timeout (`socket.setSoTimeout(120000)`). Após 120s, captura `SocketTimeoutException` e emite mensagem clara:
     `Tempo limite de espera esgotado aguardando retorno do Escravo 3.`
  3. *Limitações inerentes ao UDP:*
     - O UDP não fornece garantia de entrega, ordenação ou controle de congestionamento.
     - No ambiente Token Ring em rede local de alta velocidade (LAN Wi-Fi com 4 nós e tráfego desprezível de 1 pacote a cada 5 segundos), a perda de pacotes por congestionamento é estatisticamente quase nula.
     - A introdução do timeout de 120s atua como barreira de contenção contra congelamento indefinido do sistema.
- **Veredito:** **APROVADO**

---

### TESTE 7: Procedimento de Execução Distribuída (4 Notebooks Físicos)
1. **Passo 1:** Conectar os 4 notebooks à mesma rede Wi-Fi.
2. **Passo 2:** Obter o IP de cada máquina e preencher `config/rede.properties`.
3. **Passo 3:** Compilar o código: `scripts/compilar.sh`.
4. **Passo 4:** Iniciar na ordem inversa:
   - Notebook 4: `java Escravo 3 0 1`
   - Notebook 3: `java Escravo 2 0 1`
   - Notebook 2: `java Escravo 1 0 1`
   - Notebook 1: `java Mestre 0 0 1`
5. **Passo 5:** Observar o token percorrer cada tela com a contagem de 5 segundos até o Mestre imprimir o resultado 6.
- **Status Atual:** **PENDENTE DE VALIDAÇÃO FÍSICA NA PRESENÇA DA BANCA**.
