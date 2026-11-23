# GUIA DE CONFIGURAÇÃO DE REDE LOCAL PARA QUATRO NOTEBOOKS
**Projeto:** Token Ring UDP em Java  
**Disciplina:** Sistemas Distribuídos  
**Responsável Técnico de Rede e Integração:** Miqueias  

---

## 1. VISÃO GERAL DA ARQUITETURA DE REDE
A execução final do projeto acadêmico ocorre em quatro computadores portáteis (notebooks físicos) independentes, conectados ao mesmo Ponto de Acesso Wi-Fi ou Switch Ethernet local.

Cada notebook executa exclusivamente um único processo do anel:
- **Notebook 1:** Processo 0 — Mestre
- **Notebook 2:** Processo 1 — Escravo 1
- **Notebook 3:** Processo 2 — Escravo 2
- **Notebook 4:** Processo 3 — Escravo 3

```
  +-------------------------------------------------------------------------+
  |                           REDE WI-FI LOCAL                              |
  |                                                                         |
  |   +---------------+      UDP 5001      +---------------+                |
  |   |  Notebook 1   | -----------------> |  Notebook 2   |                |
  |   |    MESTRE     |                    |   ESCRAVO 1   |                |
  |   |   (Port 5000) |                    |  (Port 5001)  |                |
  |   +---------------+                    +---------------+                |
  |           ^                                    |                        |
  |           | UDP 5000                           | UDP 5002               |
  |           |                                    v                        |
  |   +---------------+      UDP 5003      +---------------+                |
  |   |  Notebook 4   | <----------------- |  Notebook 3   |                |
  |   |   ESCRAVO 3   |                    |   ESCRAVO 2   |                |
  |   |  (Port 5003)  |                    |  (Port 5002)  |                |
  |   +---------------+                    +---------------+                |
  +-------------------------------------------------------------------------+
```

---

## 2. POR QUE NÃO USAMOS TÚNEIS EXTERNOS (NGROK, TAILSCALE) NEM TCP?
1. **Exigência Acadêmica Estrita:** O professor especificou explicitamente o uso do protocolo **UDP** em rede local direta.
2. **Semântica do Protocolo de Transporte:** 
   - O UDP opera sem conexão prévia (*connectionless*), transmitindo datagramas autônomos sem o *three-way handshake* ou sobrecarga de reconexão do TCP.
   - Ferramentas como ngrok foram concebidas primordialmente para túneis HTTP/TCP com servidores intermediários na nuvem, o que viola o princípio de sistema distribuído autônomo em rede local ad-hoc.
3. **Desempenho e Latência:** A comunicação ponto a ponto na LAN elimina saltos na internet, garantindo tempos de trânsito em milissegundos.

---

## 3. PASSO A PASSO DE PREPARAÇÃO DOS QUATRO NOTEBOOKS

### Passo 1: Verificar a Instalação do Java (JDK 8 ou Superior)
Em cada notebook, abra o terminal e verifique:
```bash
java -version
javac -version
```
*Recomendação:* Qualquer versão do Java SE a partir da versão 8 (incluindo JDK 11, 17, 21) é 100% suportada.

---

### Passo 2: Descobrir o Endereço IP Local (IPv4) de Cada Máquina

#### No Linux:
```bash
ip addr show | grep "inet "
# ou
hostname -I
```
Identifique o IP associado à interface Wi-Fi (normalmente `wlan0`, `wlp2s0` ou similar), por exemplo `192.168.1.105`.

#### No Windows (PowerShell / CMD):
```cmd
ipconfig
```
Localize o adaptador de Rede Sem Fio Wi-Fi e anote o **Endereço IPv4**.

#### No macOS:
```bash
ipconfig getifaddr en0
```

---

### Passo 3: Configurar o Arquivo `config/rede.properties`
Em **todos os quatro notebooks**, crie o arquivo `config/rede.properties` copiando o modelo de exemplo:

```bash
cp config/rede.properties.example config/rede.properties
```

Abra o arquivo e preencha com os IPs reais identificados no Passo 2:

```properties
# Exemplo de configuração real na mesma rede Wi-Fi:
mestre.host=192.168.1.15
mestre.port=5000

escravo1.host=192.168.1.22
escravo1.port=5001

escravo2.host=192.168.1.34
escravo2.port=5002

escravo3.host=192.168.1.41
escravo3.port=5003

socket.timeout.ms=120000
```
> **Atenção:** O conteúdo deste arquivo deve ser **exatamente idêntico** nos 4 notebooks.

---

### Passo 4: Liberar as Portas UDP no Firewall do Sistema Operacional

Para que os datagramas UDP não sejam bloqueados pelo sistema operacional receptor:

#### No Linux (UFW):
```bash
# No Notebook 1 (Mestre):
sudo ufw allow 5000/udp

# No Notebook 2 (Escravo 1):
sudo ufw allow 5001/udp

# No Notebook 3 (Escravo 2):
sudo ufw allow 5002/udp

# No Notebook 4 (Escravo 3):
sudo ufw allow 5003/udp
```

#### No Windows (PowerShell como Administrador):
```powershell
New-NetFirewallRule -DisplayName "TokenRing-UDP" -Direction Inbound -Protocol UDP -LocalPort 5000-5003 -Action Allow
```

---

### Passo 5: Atenção ao Ponto Crítico — Isolamento de Clientes Wi-Fi (*AP Isolation*)
Algumas redes corporativas, universitárias ou roteadores residenciais possuem uma função ativada chamada **Client Isolation** (ou *AP Isolation*).
- **O que faz:** Impede que dois dispositivos conectados ao mesmo Wi-Fi conversem diretamente entre si, permitindo apenas acesso à internet.
- **Como diagnosticar:** 
  No Notebook 1, tente executar um ping no Notebook 2:
  ```bash
  ping 192.168.1.22
  ```
  Se o ping falhar mesmo com firewall desativado, o roteador está isolando os clientes.
- **Solução Recomendada para Apresentação:**
  1. Utilizar o recurso de **Ponto de Acesso Móvel** (*Roteador Wi-Fi do Celular / Hotspot 4G/5G*). Hotspots de smartphone normalmente **não** aplicam isolamento de clientes.
  2. Alternativamente, utilizar um roteador dedicado configurado com reserva DHCP.

---

### Passo 6: Reserva DHCP no Roteador (Recomendação Sênior)
Para evitar que o IP de qualquer notebook mude no meio da apresentação acadêmica por renovação do *lease* DHCP:
1. Acesse o painel do roteador e fixe o IP dos 4 endereços MAC dos notebooks.
2. Ou configure os IPs manualmente como **IPs Estáticos** nas configurações de rede dos sistemas operacionais durante a apresentação.

---

## 4. SEQUÊNCIA DE INICIALIZAÇÃO NA DEMONSTRAÇÃO
Para garantir que nenhum pacote seja enviado antes do receptor estar pronto para escutar:

1. **Passo 1:** Iniciar o **Escravo 3** no Notebook 4:
   ```bash
   java -cp bin:src Escravo 3 0 1
   ```
2. **Passo 2:** Iniciar o **Escravo 2** no Notebook 3:
   ```bash
   java -cp bin:src Escravo 2 0 1
   ```
3. **Passo 3:** Iniciar o **Escravo 1** no Notebook 2:
   ```bash
   java -cp bin:src Escravo 1 0 1
   ```
4. **Passo 4:** Iniciar o **Mestre** no Notebook 1 (dispara a circulação):
   ```bash
   java -cp bin:src Mestre 0 0 1
   ```

Ao iniciar o Mestre, o terminal exibirá a contagem de 5 segundos, enviará o token de valor 2 ao Escravo 1, que processará e passará para o Escravo 2, depois para o Escravo 3, retornando triunfalmente ao Mestre!
