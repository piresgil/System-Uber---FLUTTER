package application.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CarroDTO {

    private Long id;
    private String marca;
    private String modelo;
    private String matricula;
    private boolean alugado;
    private Double kilometragem;
    private boolean ativo;
    
    // Os 3 caminhos/URLs das fotos
    private String documentoUrl;
    private String seguroUrl;
    private String inspecaoUrl;
    
    private List<DespesaDTO> despesas = new ArrayList<>();
}