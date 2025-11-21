package com.bagatelle.fidelidade.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "produtos")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Produto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Nome do perfume é obrigatório")
    @Size(min = 2, max = 150, message = "Nome deve ter entre 2 e 150 caracteres")
    @Column(name = "nome_perfume", nullable = false, length = 150)
    private String nomePerfume;

    @NotBlank(message = "Marca é obrigatória")
    @Size(min = 2, max = 100, message = "Marca deve ter entre 2 e 100 caracteres")
    @Column(nullable = false, length = 100)
    private String marca;

    @NotBlank(message = "Volume é obrigatório")
    @Size(max = 20, message = "Volume deve ter no máximo 20 caracteres")
    @Column(nullable = false, length = 20)
    private String volume;

    @NotNull(message = "Preço de venda é obrigatório")
    @DecimalMin(value = "0.01", message = "Preço deve ser maior que zero")
    @Column(name = "preco_venda", nullable = false, precision = 10, scale = 2)
    private BigDecimal precoVenda;

    @OneToMany(mappedBy = "produto")
    @Builder.Default
    private List<ItemVenda> itensVenda = new ArrayList<>();

    @PrePersist
    @PreUpdate
    private void validarCampos() {
        if (precoVenda != null && precoVenda.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Preço de venda deve ser maior que zero");
        }
    }
    
    public Integer getVolumeNumerico() {
        if (volume == null || volume.isEmpty()) {
            return 0;
        }
        try {
            // Remove tudo que não for dígito (ex: "100ml" vira "100", "Kit 2un" vira "2")
            String apenasNumeros = volume.replaceAll("\\D", "");
            if (apenasNumeros.isEmpty()) return 0;
            return Integer.parseInt(apenasNumeros);
        } catch (NumberFormatException e) {
            return 0;
        }
    }
}
