package com.bagatelle.fidelidade.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "vendas")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Venda {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "Cliente é obrigatório")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cliente_id", nullable = false)
    private Cliente cliente;

    @NotNull(message = "Data da venda é obrigatória")
    @Column(name = "data_venda", nullable = false)
    private LocalDateTime dataVenda;

    @NotNull(message = "Valor total é obrigatório")
    @DecimalMin(value = "0.01", message = "Valor total deve ser maior que zero")
    @Column(name = "valor_total", nullable = false, precision = 10, scale = 2)
    private BigDecimal valorTotal;

    @Column(name = "valor_cashback_usado", nullable = false, precision = 10, scale = 2)
    @Builder.Default
    private BigDecimal valorCashbackUsado = BigDecimal.ZERO;

    @OneToMany(mappedBy = "venda", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<ItemVenda> itens = new ArrayList<>();

    @PrePersist
    private void prePersist() {
        if (dataVenda == null) {
            dataVenda = LocalDateTime.now();
        }
        if (valorCashbackUsado == null) {
            valorCashbackUsado = BigDecimal.ZERO;
        }
    }

    @PreUpdate
    private void preUpdate() {
        if (valorCashbackUsado == null) {
            valorCashbackUsado = BigDecimal.ZERO;
        }
    }

    public void adicionarItem(ItemVenda item) {
        itens.add(item);
        item.setVenda(this);
    }

    public void removerItem(ItemVenda item) {
        itens.remove(item);
        item.setVenda(null);
    }

    public BigDecimal calcularSubtotal() {
        return itens.stream()
                .map(ItemVenda::calcularSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public BigDecimal calcularValorFinal() {
        return calcularSubtotal().subtract(valorCashbackUsado);
    }
}
