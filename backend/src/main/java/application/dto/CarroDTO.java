package application.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Data Transfer Object (DTO) para a entidade Carro.
 *
 * O objetivo do DTO é:
 * - Evitar expor a entidade JPA diretamente ao cliente
 * - Controlar exatamente quais dados são enviados e recebidos
 * - Facilitar validações e transformações via Mapper
 *
 * Este DTO representa apenas os dados essenciais do Carro,
 * sem incluir relações complexas (motoristas, despesas, cartões).
 */
@Data // Gera automaticamente getters, setters, equals, hashCode e toString
@NoArgsConstructor // Construtor vazio (necessário para desserialização JSON)
@AllArgsConstructor // Construtor completo
public class CarroDTO {

    /**
     * Identificador único do carro.
     */
    private Long id;

    /**
     * Marca do carro (ex: Toyota, BMW).
     */
    private String marca;

    /**
     * Modelo do carro (ex: Corolla, Série 3).
     */
    private String modelo;

    /**
     * Matrícula do carro.
     */
    private String matricula;
}
