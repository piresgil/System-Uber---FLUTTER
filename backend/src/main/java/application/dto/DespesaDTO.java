package application.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

/**
 * Data Transfer Object (DTO) para a entidade Despesa.
 * 
 * O objetivo desta classe é transferir os dados da despesa entre o backend
 * e o frontend de forma segura, expondo apenas os IDs necessários para as 
 * relações (Carro, Cartão e Colaborador/Motorista) em vez de carregar os 
 * objetos JPA completos, incluindo também o suporte para a imagem da fatura/recibo.
 * 
 * @author Daniel Gil (adaptado)
 */
@Data // Gera automaticamente os getters, setters, toString, equals e hashCode via Lombok
@NoArgsConstructor // Gera o construtor vazio (essencial para desserialização JSON)
@AllArgsConstructor // Gera o construtor com todos os argumentos
public class DespesaDTO {

    /**
     * Identificador único da despesa.
     */
    private Long id;
    
    /**
     * Identificador do cartão associado ao pagamento da despesa.
     */
    private Long cartaoId;
    
    /**
     * Nome resumido da despesa (Ex: "Combustível").
     */
    private String nome;
    
    /**
     * Descrição detalhada sobre o gasto realizado.
     */
    private String descricao;
    
    /**
     * Identificador do carro a que esta despesa está associada.
     */
    private Long carroId;
    
    /**
     * Identificador do colaborador/motorista responsável pela despesa (opcional).
     */
    private Long motoristaId;
    
    /**
     * Data e hora em que a despesa ocorreu.
     */
    private Instant data;
    
    /**
     * Valor monetário total da despesa.
     */
    private Double valor;
    
    /**
     * Quantidade associada (ex: litros de combustível), opcional.
     */
    private Integer quantidade;
    
    /**
     * Valor unitário (ex: preço por litro), opcional.
     */
    private Double unidade;

    /**
     * URL ou caminho do ficheiro da imagem da fatura/recibo da despesa.
     */
    private String faturaUrl;
}