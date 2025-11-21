package com.bagatelle.fidelidade.service;

import com.bagatelle.fidelidade.model.Cliente;
import com.bagatelle.fidelidade.model.RegistroCashback;
import com.bagatelle.fidelidade.model.Venda;
import com.bagatelle.fidelidade.repository.RegistroCashbackRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class CashbackService {

    private final RegistroCashbackRepository registroCashbackRepository;
    private final ClienteService clienteService;

    private static final BigDecimal PERCENTUAL_CASHBACK = new BigDecimal("0.05"); // 5%
    private static final int DIAS_EXPIRACAO = 90;

    /**
     * Gera cashback de 5% do valor final da compra e adiciona ao saldo do cliente.
     * Cria um registro de cashback com data de expiração de 90 dias.
     */
    public RegistroCashback gerarCashback(Venda venda) {
        if (venda == null || venda.getValorTotal() == null) {
            throw new RuntimeException("Venda inválida para geração de cashback");
        }

        // Calcula 5% do valor final da compra (após desconto de cashback usado)
        BigDecimal valorFinal = venda.calcularValorFinal();
        BigDecimal valorCashback = valorFinal.multiply(PERCENTUAL_CASHBACK)
                .setScale(2, RoundingMode.HALF_UP);

        // Se o valor for muito pequeno, não gera cashback
        if (valorCashback.compareTo(new BigDecimal("0.01")) < 0) {
            return null;
        }

        // Cria o registro de cashback
        RegistroCashback registro = RegistroCashback.builder()
                .cliente(venda.getCliente())
                .valor(valorCashback)
                .dataGeracao(LocalDate.now())
                .dataExpiracao(LocalDate.now().plusDays(DIAS_EXPIRACAO))
                .utilizado(false)
                .valorUtilizado(BigDecimal.ZERO)
                .vendaOrigem(venda)
                .build();

        registro = registroCashbackRepository.save(registro);

        // Atualiza o saldo de cashback do cliente
        clienteService.adicionarCashback(venda.getCliente().getId(), valorCashback);

        return registro;
    }

    /**
     * Utiliza cashback durante uma venda. Debita do saldo do cliente e marca
     * os registros como parcial ou totalmente utilizados.
     */
    public void utilizarCashback(Cliente cliente, BigDecimal valorUtilizado) {
        if (valorUtilizado == null || valorUtilizado.compareTo(BigDecimal.ZERO) <= 0) {
            return; // Não há cashback a ser utilizado
        }

        // Valida se o cliente tem saldo suficiente
        BigDecimal saldoDisponivel = calcularSaldoDisponivel(cliente);
        if (valorUtilizado.compareTo(saldoDisponivel) > 0) {
            throw new RuntimeException("Saldo de cashback insuficiente. Disponível: R$ " + saldoDisponivel);
        }

        // Busca registros de cashback disponíveis (não utilizados e não expirados)
        List<RegistroCashback> registros = registroCashbackRepository
                .buscarPorClienteNaoUtilizado(cliente.getId(), LocalDate.now());

        // Utiliza os registros mais antigos primeiro (FIFO)
        BigDecimal valorRestante = valorUtilizado;
        for (RegistroCashback registro : registros) {
            if (valorRestante.compareTo(BigDecimal.ZERO) <= 0) {
                break;
            }

            BigDecimal valorDisponivelRegistro = registro.getValorDisponivel();
            BigDecimal valorAUtilizar = valorRestante.min(valorDisponivelRegistro);

            registro.setValorUtilizado(registro.getValorUtilizado().add(valorAUtilizar));

            // Se utilizou todo o valor do registro, marca como utilizado
            if (registro.getValorUtilizado().compareTo(registro.getValor()) >= 0) {
                registro.setUtilizado(true);
            }

            registroCashbackRepository.save(registro);
            valorRestante = valorRestante.subtract(valorAUtilizar);
        }

        // Atualiza o saldo do cliente
        clienteService.subtrairCashback(cliente.getId(), valorUtilizado);
    }

    /**
     * Calcula o saldo disponível de cashback do cliente.
     * Considera apenas registros não expirados e não totalmente utilizados.
     */
    public BigDecimal calcularSaldoDisponivel(Cliente cliente) {
        return registroCashbackRepository.calcularSaldoDisponivel(
                cliente.getId(),
                LocalDate.now()
        );
    }

    /**
     * Lista todos os registros de cashback de um cliente.
     */
    public List<RegistroCashback> listarPorCliente(Long clienteId) {
        return registroCashbackRepository.buscarPorCliente(clienteId);
    }

    /**
     * Busca registros de cashback que expirarão nos próximos 7 dias.
     */
    public List<RegistroCashback> buscarCashbackExpirando() {
        LocalDate hoje = LocalDate.now();
        LocalDate daquiA7Dias = hoje.plusDays(7);
        return registroCashbackRepository.buscarCashbackExpirando(hoje, daquiA7Dias);
    }
}
