
<!-- Cabeçalho Animado -->
<div align="center">
  <a href="https://github.com/isaacinr/token-ring-udp-java">
    <img src="https://readme-typing-svg.demolab.com?font=JetBrains+Mono&weight=700&size=36&duration=3000&pause=1000&color=FF2A5F&center=true&vCenter=true&width=820&height=90&lines=Token+Ring+UDP+%E2%80%94+Sistemas+Distribu%C3%ADdos;Topologia+em+Anel+com+DatagramSockets;Comunica%C3%A7%C3%A3o+Bin%C3%A1ria+em+Rede+(Java+UDP)" alt="Token Ring UDP Banner" />
  </a>
</div>

<!-- Badges de Tecnologias -->
<div align="center">
  <img src="https://img.shields.io/badge/Java-17+-0B132B?style=for-the-badge&logo=openjdk&logoColor=00F5D4" alt="Java" />
  <img src="https://img.shields.io/badge/Protocol-UDP%2FIP-0B132B?style=for-the-badge&logo=cisco&logoColor=38BDF8" alt="UDP" />
  <img src="https://img.shields.io/badge/Topology-Token_Ring-0B132B?style=for-the-badge&logo=diagramsdotnet&logoColor=10B981" alt="Token Ring" />
  <img src="https://img.shields.io/badge/Payload-ByteBuffer_(8B)-0B132B?style=for-the-badge&logo=buffer&logoColor=A855F7" alt="ByteBuffer" />
</div>

<br>

<!-- Descrição Principal -->
<div align="center">
  <h3>📡 Simulação Distribuída de Topologia Lógica em Anel (Token Ring)</h3>
  <p>
    Implementação em <strong>Java</strong> sobre a camada de transporte <strong>UDP (User Datagram Protocol)</strong>.<br>
    O sistema orquestra <strong>4 nós independentes</strong> (1 Processo Mestre e 3 Processos Escravos) que roteiam e processam um <em>token</em> numérico no <strong>sentido horário</strong> através de datagramas binários de 8 bytes.
  </p>
</div>

<br>
<div align="center">
  <img width="380" alt="Topologia Token Ring UDP" src="https://github.com/user-attachments/assets/51de786a-5a70-463f-a539-158f77079542" />
  <br>
  <sub><b>Figura 1:</b> Fluxo de comunicação circular (sentido horário) entre o processo coordenador (<b>Mestre</b>) e os nós trabalhadores (<b>Escravos 1, 2 e 3</b>) via sockets UDP.</sub>
</div>

<br>

---

## 🌐 Características da Rede

<table>
  <tr>
    <td width="50%" valign="top">
      <h3>🔄 Roteamento em Anel (Sentido Horário)</h3>
      <p>O <em>token</em> (iniciado com valor inteiro <code>2</code>) parte do <strong>Mestre (Nó 0)</strong> e trafega sequencialmente pelos <strong>Escravos 1, 2 e 3</strong> até fechar o ciclo completo de volta ao coordenador.</p>
    </td>
    <td width="50%" valign="top">
      <h3>⚡ Transporte Não-Orientado a Conexão (UDP)</h3>
      <p>Comunicação leve e assíncrona via <code>DatagramSocket</code> e <code>DatagramPacket</code>, operando tanto em <em>loopback</em> (<code>127.0.0.1</code>) quanto entre múltiplos hosts na mesma sub-rede.</p>
    </td>
  </tr>
  <tr>
    <td width="50%" valign="top">
      <h3>📦 Serialização Binária (8 Bytes)</h3>
      <p>Os datagramas transportam pacotes compactos via <code>ByteBuffer</code> contendo dois inteiros de 32 bits: o <strong>valor atual do token</strong> (4 bytes) e o <strong>código da operação</strong> (4 bytes).</p>
    </td>
    <td width="50%" valign="top">
      <h3>🎛️ Parametrização Dinâmica via CLI</h3>
      <p>Cada nó é configurado em tempo de execução via argumentos de linha de comando, permitindo alternar operações (<code>+1</code> ou <code>×2</code>) e definir <code>N</code> voltas completas na topologia.</p>
    </td>
  </tr>
</table>

---


  
<h3 align="center">👨‍💻 Criadores</h3>

<table align="center">
  <tr>
    <td align="center" width="240px" valign="top">
      <a href="https://github.com/isaacinr" target="_blank">
        <img src="https://github.com/isaacinr.png?size=90" width="90px" height="90px" alt="Isaac Nascimento"/><br />
        <sub><b>Isaac Nascimento</b></sub>
      </a>
      <p align="center">
        <a href="https://github.com/isaacinr" target="_blank">
          <img src="https://img.shields.io/badge/GitHub-181717?style=for-the-badge&logo=github&logoColor=white" alt="GitHub Badge"/>
        </a>
      </p>
    </td>
    <td align="center" width="240px" valign="top">
      <a href="https://github.com/VitorP2007" target="_blank">
        <img src="https://github.com/VitorP2007.png?size=90" width="90px" height="90px" alt="Vitor Pizolato"/><br />
        <sub><b>Vitor Pizolato</b></sub>
      </a>
      <p align="center">
        <a href="https://github.com/VitorP2007" target="_blank">
          <img src="https://img.shields.io/badge/GitHub-181717?style=for-the-badge&logo=github&logoColor=white" alt="GitHub Badge"/>
        </a>
      </p>
    </td>
    <td align="center" width="240px" valign="top">
      <a href="https://github.com/MiqueiasNogueira" target="_blank">
        <img src="https://github.com/MiqueiasNogueira.png?size=90" width="90px" height="90px" alt="Miqueias Nogueira"/><br />
        <sub><b>Miqueias Nogueira</b></sub>
      </a>
      <p align="center">
        <a href="https://github.com/MiqueiasNogueira" target="_blank">
          <img src="https://img.shields.io/badge/GitHub-181717?style=for-the-badge&logo=github&logoColor=white" alt="GitHub Badge"/>
        </a>
      </p>
    </td>
  </tr>
  <tr>
    <td align="center" width="240px" valign="top">
      <a href="https://github.com/heitor-fernandes-silva" target="_blank">
        <img src="https://github.com/heitor-fernandes-silva.png?size=90" width="90px" height="90px" alt="Heitor Fernandes"/><br />
        <sub><b>Heitor Fernandes</b></sub>
      </a>
      <p align="center">
        <a href="https://github.com/heitor-fernandes-silva" target="_blank">
          <img src="https://img.shields.io/badge/GitHub-181717?style=for-the-badge&logo=github&logoColor=white" alt="GitHub Badge"/>
        </a>
      </p>
    </td>
    <td align="center" width="240px" valign="top">
      <a href="https://www.linkedin.com/in/camila-cotrim-447a65a4/" target="_blank">
        <img src="https://ui-avatars.com/api/?name=Camila+Cotrim&background=181717&color=ffffff&size=90&bold=true" width="90px" height="90px" alt="Camila Cotrim"/><br />
        <sub><b>Camila Cotrim</b></sub>
      </a>
      <p align="center">
        <a href="https://www.linkedin.com/in/camila-cotrim-447a65a4/" target="_blank">
          <img src="https://img.shields.io/badge/LinkedIn-0077B5?style=for-the-badge&logo=linkedin&logoColor=white" alt="LinkedIn Badge"/>
        </a>
      </p>
    </td>
  </tr>
</table>

<p align="center">
  <img src="https://www.gitskins.com/api/section/social?username=isaacinr&theme=github-dark&avatar=https%3A%2F%2Favatars.githubusercontent.com%2Fu%2F244819485%3Fu%3D3504f8ab141c3322f1535b7a92a8d7c224e01df0%26v%3D4" alt="isaacinr social visual" />
</p>

<p align="center">
  <a href="https://www.linkedin.com/in/inrisaacnascimentoribeiro/" target="_blank">
    <img src="https://img.shields.io/badge/LinkedIn-0077B5?style=for-the-badge&logo=linkedin&logoColor=white" alt="LinkedIn Badge"/>
  </a>
</p>
