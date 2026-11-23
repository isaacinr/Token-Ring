package modelo;

/**
 * Representa as operações matemáticas suportadas pelo Token Ring,
 * conforme estipulado pelo professor:
 * - Operação 0: Incrementar o token em 1 (+1).
 * - Operação 1: Multiplicar o token atual por 2 (*2).
 * 
 * Integração e padronização: Miqueias
 * Lógica matemática original: Camila e Isaac
 */
public enum OperacaoToken {
    INCREMENTAR(0, "Incrementar (+1)", "+1"),
    MULTIPLICAR(1, "Multiplicar por 2 (*2)", "*2");

    private final int codigo;
    private final String descricao;
    private final String simbolo;

    OperacaoToken(int codigo, String descricao, String simbolo) {
        this.codigo = codigo;
        this.descricao = descricao;
        this.simbolo = simbolo;
    }

    public int getCodigo() {
        return codigo;
    }

    public String getDescricao() {
        return descricao;
    }

    public String getSimbolo() {
        return simbolo;
    }

    /**
     * Aplica a operação matemática ao valor atual do token.
     * Preserva a lógica original de processamento formulada por Camila e Isaac.
     * 
     * @param valorAtual valor inteiro do token
     * @param codigoOperacao 0 para incremento, 1 para multiplicação
     * @return novo valor do token
     */
    public static int aplicar(int valorAtual, int codigoOperacao) {
        if (codigoOperacao == 0) {
            return valorAtual + 1;
        } else if (codigoOperacao == 1) {
            return valorAtual * 2;
        } else {
            throw new IllegalArgumentException("Código de operação inválido: " + codigoOperacao + ". Use 0 (+1) ou 1 (*2).");
        }
    }

    public static OperacaoToken porCodigo(int codigoOperacao) {
        for (OperacaoToken op : values()) {
            if (op.codigo == codigoOperacao) {
                return op;
            }
        }
        throw new IllegalArgumentException("Operação desconhecida: " + codigoOperacao);
    }
}
