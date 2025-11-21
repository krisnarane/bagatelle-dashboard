package com.bagatelle.fidelidade.service;

import com.bagatelle.fidelidade.model.RegistroCashback;
import com.bagatelle.fidelidade.model.Venda;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DashboardService {

    private final CashbackService cashbackService;
    private final VendaService vendaService;

    /**
     * Retorna lista de cashbacks que expirarão nos próximos 7 dias.
     * Para cada registro, retorna: nome do cliente, valor do cashback e telefone.
     */
    public List<RegistroCashback> buscarCashbackExpirando() {
        return cashbackService.buscarCashbackExpirando();
    }

    /**
     * Retorna lista de vendas onde os produtos estão estimados para acabar agora.
     * Baseado no volume dos perfumes.
     */
    public List<Venda> buscarSugestoesRecompra() {
        // Alterado de buscarVendasDe5MesesAtras() para a nova lógica
        return vendaService.buscarSugestoesRecompraDinamica();
    }
}
