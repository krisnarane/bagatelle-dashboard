package com.bagatelle.fidelidade.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NovaVendaDTO {

    private Long clienteId;
    private String clienteNome;
    private String clienteCpf;

    @Builder.Default
    private List<ItemVendaDTO> itens = new ArrayList<>();

    private BigDecimal valorCashbackUsado;
    private BigDecimal saldoCashbackDisponivel;

    public BigDecimal calcularSubtotal() {
        return itens.stream()
                .map(ItemVendaDTO::calcularSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public BigDecimal calcularTotal() {
        BigDecimal subtotal = calcularSubtotal();
        BigDecimal cashback = valorCashbackUsado != null ? valorCashbackUsado : BigDecimal.ZERO;
        return subtotal.subtract(cashback);
    }
}
