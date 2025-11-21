package com.bagatelle.fidelidade.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "registros_cashback")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RegistroCashback {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "Cliente é obrigatório")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cliente_id", nullable = false)
    private Cliente cliente;

    @NotNull(message = "Valor é obrigatório")
    @DecimalMin(value = "0.01", message = "Valor deve ser maior que zero")
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal valor;

    @NotNull(message = "Data de geração é obrigatória")
    @Column(name = "data_geracao", nullable = false)
    private LocalDate dataGeracao;

    @NotNull(message = "Data de expiração é obrigatória")
    @Column(name = "data_expiracao", nullable = false)
    private LocalDate dataExpiracao;

    @Column(nullable = false)
    @Builder.Default
    private Boolean utilizado = false;

    @Column(name = "valor_utilizado", precision = 10, scale = 2)
    @Builder.Default
    private BigDecimal valorUtilizado = BigDecimal.ZERO;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "venda_id")
    private Venda vendaOrigem;

    @PrePersist
    private void prePersist() {
        if (dataGeracao == null) {
            dataGeracao = LocalDate.now();
        }
        if (dataExpiracao == null) {
            dataExpiracao = dataGeracao.plusDays(90);
        }
        if (utilizado == null) {
            utilizado = false;
        }
        if (valorUtilizado == null) {
            valorUtilizado = BigDecimal.ZERO;
        }
    }

    @PreUpdate
    private void preUpdate() {
        if (utilizado == null) {
            utilizado = false;
        }
        if (valorUtilizado == null) {
            valorUtilizado = BigDecimal.ZERO;
        }
    }

    public boolean isExpirado() {
        return LocalDate.now().isAfter(dataExpiracao);
    }

    public boolean isExpirando() {
        LocalDate daquiA7Dias = LocalDate.now().plusDays(7);
        return !isExpirado() && !dataExpiracao.isAfter(daquiA7Dias);
    }

    public BigDecimal getValorDisponivel() {
        if (utilizado || isExpirado()) {
            return BigDecimal.ZERO;
        }
        return valor.subtract(valorUtilizado);
    }
}
