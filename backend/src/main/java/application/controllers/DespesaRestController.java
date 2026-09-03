package application.controllers;

import application.dto.DespesaDTO;
import application.model.Despesa;
import application.services.DespesaService;
import application.services.FileStorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/despesas")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
public class DespesaRestController {

    private final DespesaService despesaService;
    private final FileStorageService fileStorageService;

    @GetMapping
    public ResponseEntity<List<DespesaDTO>> findAll() {
        List<DespesaDTO> lista = despesaService.findAll().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(lista);
    }

    @GetMapping("/{id}")
    public ResponseEntity<DespesaDTO> findById(@PathVariable Long id) {
        Despesa despesa = despesaService.findById(id);
        return ResponseEntity.ok(convertToDTO(despesa));
    }

    @PostMapping
    public ResponseEntity<?> insert(@RequestBody DespesaDTO dto) {
        try {
            Despesa despesaEntity = despesaService.insertFromDTO(dto); 
            DespesaDTO salvaDTO = convertToDTO(despesaEntity);
            
            // CORREÇÃO: Uso do ServletUriComponentsBuilder para gerar a URI correta automaticamente
            URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                    .path("/{id}")
                    .buildAndExpand(salvaDTO.getId())
                    .toUri();

            return ResponseEntity.created(location).body(salvaDTO);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/{id}/fatura")
    public ResponseEntity<?> uploadFatura(@PathVariable Long id, @RequestParam("file") MultipartFile file) {
        try {
            String fileUrl = fileStorageService.storeFile(file, "faturas");

            Despesa despesa = despesaService.findById(id);
            despesa.setFaturaUrl(fileUrl);
            
            Despesa despesaAtualizada = despesaService.updateFaturaUrl(id, fileUrl); 

            return ResponseEntity.ok(convertToDTO(despesaAtualizada));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Erro ao carregar a fatura: " + e.getMessage());
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable Long id, @RequestBody DespesaDTO dto) {
        try {
            Despesa despesaAtualizada = despesaService.updateFromDTO(id, dto);
            return ResponseEntity.ok(convertToDTO(despesaAtualizada));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        despesaService.delete(id);
        return ResponseEntity.noContent().build();
    }

    private DespesaDTO convertToDTO(Despesa despesa) {
        if (despesa == null) return null;
        
        DespesaDTO dto = new DespesaDTO();
        dto.setId(despesa.getId());
        dto.setNome(despesa.getNome());
        dto.setDescricao(despesa.getDescricao());
        dto.setData(despesa.getData());
        dto.setValor(despesa.getValor());
        dto.setQuantidade(despesa.getQuantidade());
        dto.setUnidade(despesa.getUnidade());
        dto.setFaturaUrl(despesa.getFaturaUrl());

        if (despesa.getCarro() != null) {
            dto.setCarroId(despesa.getCarro().getId());
        }
        if (despesa.getCartao() != null) {
            dto.setCartaoId(despesa.getCartao().getId());
        }
        if (despesa.getMotorista() != null) {
            dto.setMotoristaId(despesa.getMotorista().getId());
        }

        return dto;
    }
}