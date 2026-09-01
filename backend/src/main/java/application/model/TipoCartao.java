package application.model;

public enum TipoCartao {
    ABASTECIMENTO("Abastecimento"),
    PORTAGEM("Portagem"),
    OUTRA("Outra"); // Adicione este valor

    private final String descricao;

    TipoCartao(String descricao) {
        this.descricao = descricao;
    }

    public String getDescricao() {
        return descricao;
    }

    public static TipoCartao fromString(String text) {
        for (TipoCartao b : TipoCartao.values()) {
            if (b.descricao.equalsIgnoreCase(text)) {
                return b;
            }
        }
        return null;
    }
}