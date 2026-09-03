package application.mappers;

import application.dto.CarroDTO;
import application.dto.DespesaDTO;
import application.model.Carro;

import java.util.ArrayList;
import java.util.stream.Collectors;

/**
 * Mapper responsável por converter entre a entidade Carro e o CarroDTO.
 * Atualizado para suportar os novos campos de imagens individuais e despesas.
 */
public class CarroMapper {

    /**
     * Converte uma entidade Carro para um CarroDTO, incluindo as respetivas despesas e imagens.
     */
    public static CarroDTO toDTO(Carro entity) {
        if (entity == null) return null;

        CarroDTO dto = new CarroDTO();
        dto.setId(entity.getId());
        dto.setMarca(entity.getMarca());
        dto.setModelo(entity.getModelo());
        dto.setMatricula(entity.getMatricula());
        dto.setAlugado(entity.isAlugado());
        dto.setKilometragem(entity.getKilometragem());
        dto.setAtivo(entity.isAtivo());
        
        // Mapeamento dos 3 novos campos de imagem
        dto.setDocumentoUrl(entity.getDocumentoUrl());
        dto.setSeguroUrl(entity.getSeguroUrl());
        dto.setInspecaoUrl(entity.getInspecaoUrl());

        // Mapeia a lista de despesas se existirem
        if (entity.getDespesas() != null) {
            dto.setDespesas(
                entity.getDespesas().stream().map(despesa -> {
                    DespesaDTO dDto = new DespesaDTO();
                    dDto.setId(despesa.getId());
                    dDto.setNome(despesa.getNome());
                    dDto.setDescricao(despesa.getDescricao());
                    dDto.setData(despesa.getData());
                    dDto.setValor(despesa.getValor());
                    dDto.setQuantidade(despesa.getQuantidade());
                    dDto.setUnidade(despesa.getUnidade());
                    dDto.setFaturaUrl(despesa.getFaturaUrl());
                    return dDto;
                }).collect(Collectors.toList())
            );
        } else {
            dto.setDespesas(new ArrayList<>());
        }

        return dto;
    }

    /**
     * Converte um CarroDTO para a entidade Carro, incluindo os novos campos de imagem.
     */
    public static Carro toEntity(CarroDTO dto) {
        if (dto == null) return null;

        Carro carro = new Carro();
        carro.setId(dto.getId());
        carro.setMarca(dto.getMarca());
        carro.setModelo(dto.getModelo());
        carro.setMatricula(dto.getMatricula());
        carro.setAlugado(dto.isAlugado());
        carro.setKilometragem(dto.getKilometragem());
        carro.setAtivo(dto.isAtivo());
        
        // Mapeamento dos 3 novos campos de imagem para a entidade
        carro.setDocumentoUrl(dto.getDocumentoUrl());
        carro.setSeguroUrl(dto.getSeguroUrl());
        carro.setInspecaoUrl(dto.getInspecaoUrl());
        
        return carro;
    }
}