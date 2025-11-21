package com.bagatelle.fidelidade.dto;

import com.bagatelle.fidelidade.model.Produto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProdutoDTO {

    private Long id;
    private String nomePerfume;
    private String marca;
    private String volume;
    private BigDecimal precoVenda;

    public static ProdutoDTO fromEntity(Produto produto) {
        return ProdutoDTO.builder()
                .id(produto.getId())
                .nomePerfume(produto.getNomePerfume())
                .marca(produto.getMarca())
                .volume(produto.getVolume())
                .precoVenda(produto.getPrecoVenda())
                .build();
    }

    public String getDescricaoCompleta() {
        return nomePerfume + " - " + marca + " (" + volume + ")";
    }
}
