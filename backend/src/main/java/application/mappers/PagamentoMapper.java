package application.mappers;

import application.dto.PagamentoDTO;
import application.model.*;

public class PagamentoMapper {

    public static PagamentoDTO toDTO(Pagamento p) {
        if (p == null) return null;

        return new PagamentoDTO(
                p.getId(),
                p.getColaborador() != null ? p.getColaborador().getId() : null,
                p.getPlataforma(),
                p.getData(),
                p.getValor(),
                p.getTipoPagamento(),
                p.isAtivo()
        );
    }

    public static Pagamento toEntity(PagamentoDTO dto) {
        if (dto == null) return null;

        Pagamento p = new Pagamento();
        p.setId(dto.getId());
        p.setPlataforma(dto.getPlataforma());
        p.setData(dto.getData());
        p.setValor(dto.getValor());
        p.setTipoPagamento(dto.getTipoPagamento());
        p.setAtivo(dto.isAtivo());

        if (dto.getColaboradorId() != null) {
            Colaborador c = new Colaborador();
            c.setId(dto.getColaboradorId());
            p.setColaborador(c);
        }

        return p;
    }
}
