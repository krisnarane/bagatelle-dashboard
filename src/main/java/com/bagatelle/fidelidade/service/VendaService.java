package com.bagatelle.fidelidade.service;

import com.bagatelle.fidelidade.model.Cliente;
import com.bagatelle.fidelidade.model.ItemVenda;
import com.bagatelle.fidelidade.model.Produto;
import com.bagatelle.fidelidade.model.Venda;
import com.bagatelle.fidelidade.repository.VendaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class VendaService {

    private final VendaRepository vendaRepository;
    private final ClienteService clienteService;
    private final ProdutoService produtoService;
    private final CashbackService cashbackService;

    public List<Venda> listarTodas() {
        return vendaRepository.findAllOrdenadas();
    }

    public Venda buscarPorId(Long id) {
        return vendaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Venda não encontrada com ID: " + id));
    }

    public List<Venda> buscarPorCliente(Long clienteId) {
        return vendaRepository.buscarPorCliente(clienteId);
    }

    /**
     * Busca sugestões de recompra baseadas no volume dos produtos comprados.
     * Lógica: Recupera vendas do último ano e verifica item a item se a previsão de término está próxima.
     */
    public List<Venda> buscarSugestoesRecompraDinamica() {
        LocalDateTime hoje = LocalDateTime.now();
        // Busca vendas de até 1 ano atrás para analisar (pode ajustar conforme necessidade)
        LocalDateTime umAnoAtras = hoje.minusYears(1);
        
        // método que busca por intervalo
        List<Venda> vendasRecentes = vendaRepository.buscarVendasEntreDatas(umAnoAtras, hoje);

        // Filtra apenas as vendas que têm produtos "vencendo" agora
        return vendasRecentes.stream()
                .filter(venda -> isHoraDeRecomprar(venda))
                .collect(Collectors.toList()); // ou .toList() se estiver no Java 16+
    }

    private boolean isHoraDeRecomprar(Venda venda) {
        LocalDate dataVenda = venda.getDataVenda().toLocalDate();
        LocalDate hoje = LocalDate.now();

        // Verifica cada item da venda
        for (ItemVenda item : venda.getItens()) {
            Produto produto = item.getProduto();
            Integer volumeMl = produto.getVolumeNumerico();
            
            // Calcula a duração estimada em dias
            long diasDuracao = calcularDuracaoEstimada(volumeMl);
            
            // Multiplica pela quantidade comprada (ex: comprou 2 frascos, dura o dobro)
            diasDuracao = diasDuracao * item.getQuantidade();

            LocalDate dataPrevisaoTermino = dataVenda.plusDays(diasDuracao);

            // Lógica da Sugestão:
            // Sugerimos recompra se a data de término prevista for nos próximos 15 dias
            // OU se já passou até 30 dias da data prevista (cliente atrasado)
            LocalDate inicioJanela = hoje.minusDays(30); 
            LocalDate fimJanela = hoje.plusDays(15);

            if (dataPrevisaoTermino.isAfter(inicioJanela) && dataPrevisaoTermino.isBefore(fimJanela)) {
                return true; // Encontrou pelo menos um produto nessa venda que precisa de reposição
            }
        }
        return false;
    }

    private long calcularDuracaoEstimada(Integer volumeMl) {
        if (volumeMl <= 0) return 90; // Padrão de 3 meses se não conseguir ler o volume

        // LÓGICA DE NEGÓCIO:
        // Baseado no exemplo: 120ml dura 3 meses (90 dias).
        // Fator = 90 / 120 = 0.75 dias por ml.
        
        // Podemos criar faixas para ser mais realista
        
        if (volumeMl <= 30) return 30;  // 30ml dura ~1 mês
        if (volumeMl <= 50) return 60;  // 50ml dura ~2 meses
        if (volumeMl <= 100) return 90; // 100ml dura ~3 meses
        if (volumeMl <= 150) return 120; // 120-150ml dura ~4 meses
        
        return 150; // Frascos gigantes (200ml+)
    }

    /**
     * Registra uma nova venda com os itens fornecidos.
     * Valida o cashback utilizado e gera novo cashback automaticamente.
     */
    public Venda registrarVenda(Long clienteId, List<ItemVenda> itens, BigDecimal valorCashbackUsado) {
        if (itens == null || itens.isEmpty()) {
            throw new RuntimeException("A venda deve conter pelo menos um item");
        }

        // Busca o cliente
        Cliente cliente = clienteService.buscarPorId(clienteId);

        // Valida o cashback a ser utilizado
        if (valorCashbackUsado == null) {
            valorCashbackUsado = BigDecimal.ZERO;
        }
        validarCashbackUsado(cliente, valorCashbackUsado, itens);

        // Cria a venda
        Venda venda = Venda.builder()
                .cliente(cliente)
                .dataVenda(LocalDateTime.now())
                .valorCashbackUsado(valorCashbackUsado)
                .build();

        // Calcula o valor total dos itens
        BigDecimal valorTotal = BigDecimal.ZERO;
        for (ItemVenda item : itens) {
            Produto produto = produtoService.buscarPorId(item.getProduto().getId());
            item.setPrecoUnitario(produto.getPrecoVenda());
            item.setVenda(venda);
            valorTotal = valorTotal.add(item.calcularSubtotal());
        }

        venda.setValorTotal(valorTotal);
        venda.setItens(itens);

        // Valida se o cashback usado não é maior que o valor total
        if (valorCashbackUsado.compareTo(valorTotal) > 0) {
            throw new RuntimeException("Valor de cashback usado não pode ser maior que o valor total da compra");
        }

        // Utiliza o cashback (se houver)
        if (valorCashbackUsado.compareTo(BigDecimal.ZERO) > 0) {
            cashbackService.utilizarCashback(cliente, valorCashbackUsado);
        }

        // Salva a venda
        venda = vendaRepository.save(venda);

        // Gera cashback de 5% do valor final
        cashbackService.gerarCashback(venda);

        return venda;
    }

    /**
     * Valida se o cashback a ser utilizado está dentro dos limites permitidos.
     */
    public void validarCashbackUsado(Cliente cliente, BigDecimal valorCashbackUsado, List<ItemVenda> itens) {
        if (valorCashbackUsado.compareTo(BigDecimal.ZERO) < 0) {
            throw new RuntimeException("Valor de cashback não pode ser negativo");
        }

        if (valorCashbackUsado.compareTo(BigDecimal.ZERO) == 0) {
            return; // Não há cashback a validar
        }

        // Calcula o valor total dos itens
        BigDecimal valorTotal = itens.stream()
                .map(ItemVenda::calcularSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Valida se o cashback usado não é maior que o valor total
        if (valorCashbackUsado.compareTo(valorTotal) > 0) {
            throw new RuntimeException("Valor de cashback usado não pode ser maior que o valor total da compra");
        }

        // Valida se o cliente tem saldo suficiente
        BigDecimal saldoDisponivel = cashbackService.calcularSaldoDisponivel(cliente);
        if (valorCashbackUsado.compareTo(saldoDisponivel) > 0) {
            throw new RuntimeException(
                    String.format("Saldo de cashback insuficiente. Disponível: R$ %.2f", saldoDisponivel)
            );
        }
    }

    /**
     * Calcula o saldo de cashback disponível do cliente.
     */
    public BigDecimal calcularSaldoDisponivel(Long clienteId) {
        Cliente cliente = clienteService.buscarPorId(clienteId);
        return cashbackService.calcularSaldoDisponivel(cliente);
    }
}
