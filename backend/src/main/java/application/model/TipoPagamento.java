package application.model;

/**
 * Enum que representa os tipos de pagamento disponíveis no sistema.
 *
 * Mantém o código simples e permite adicionar novos tipos no futuro
 * sem alterar a estrutura da entidade Pagamento.
 */
public enum TipoPagamento {
    SEMANAL,
    MENSAL,
    POR_HORA,
    FIXO
}
