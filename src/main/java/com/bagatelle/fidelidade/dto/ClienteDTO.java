package com.bagatelle.fidelidade.dto;

import com.bagatelle.fidelidade.model.Cliente;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ClienteDTO {

    private Long id;
    private String nomeCompleto;
    private String cpf;
    private String telefone;
    private String email;
    private BigDecimal saldoCashback;

    public static ClienteDTO fromEntity(Cliente cliente) {
        return ClienteDTO.builder()
                .id(cliente.getId())
                .nomeCompleto(cliente.getNomeCompleto())
                .cpf(cliente.getCpf())
                .telefone(cliente.getTelefone())
                .email(cliente.getEmail())
                .saldoCashback(cliente.getSaldoCashback())
                .build();
    }
}
