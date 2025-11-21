package com.bagatelle.fidelidade.service;

import com.bagatelle.fidelidade.model.Cliente;
import com.bagatelle.fidelidade.repository.ClienteRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class ClienteService {

    private final ClienteRepository clienteRepository;

    public List<Cliente> listarTodos() {
        return clienteRepository.findAllOrdenados();
    }

    public Cliente buscarPorId(Long id) {
        return clienteRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Cliente não encontrado com ID: " + id));
    }

    public Cliente buscarPorCpf(String cpf) {
        return clienteRepository.findByCpf(cpf)
                .orElseThrow(() -> new RuntimeException("Cliente não encontrado com CPF: " + cpf));
    }

    public List<Cliente> buscarPorCpfOuNome(String termo) {
        return clienteRepository.buscarPorCpfOuNome(termo);
    }

    public Cliente salvar(Cliente cliente) {
        validarCliente(cliente);
        return clienteRepository.save(cliente);
    }

    public Cliente atualizar(Long id, Cliente clienteAtualizado) {
        Cliente clienteExistente = buscarPorId(id);

        // Verifica se o CPF foi alterado e se já existe outro cliente com esse CPF
        if (!clienteExistente.getCpf().equals(clienteAtualizado.getCpf())) {
            if (clienteRepository.existsByCpf(clienteAtualizado.getCpf())) {
                throw new RuntimeException("Já existe um cliente cadastrado com este CPF");
            }
        }

        clienteExistente.setNomeCompleto(clienteAtualizado.getNomeCompleto());
        clienteExistente.setCpf(clienteAtualizado.getCpf());
        clienteExistente.setTelefone(clienteAtualizado.getTelefone());
        clienteExistente.setEmail(clienteAtualizado.getEmail());

        return clienteRepository.save(clienteExistente);
    }

    public void deletar(Long id) {
        Cliente cliente = buscarPorId(id);
        clienteRepository.delete(cliente);
    }

    public void adicionarCashback(Long clienteId, BigDecimal valor) {
        Cliente cliente = buscarPorId(clienteId);
        cliente.setSaldoCashback(cliente.getSaldoCashback().add(valor));
        clienteRepository.save(cliente);
    }

    public void subtrairCashback(Long clienteId, BigDecimal valor) {
        Cliente cliente = buscarPorId(clienteId);
        BigDecimal novoSaldo = cliente.getSaldoCashback().subtract(valor);

        if (novoSaldo.compareTo(BigDecimal.ZERO) < 0) {
            throw new RuntimeException("Saldo de cashback insuficiente");
        }

        cliente.setSaldoCashback(novoSaldo);
        clienteRepository.save(cliente);
    }

    private void validarCliente(Cliente cliente) {
        if (clienteRepository.existsByCpf(cliente.getCpf())) {
            throw new RuntimeException("Já existe um cliente cadastrado com este CPF");
        }
    }

    public boolean existePorCpf(String cpf) {
        return clienteRepository.existsByCpf(cpf);
    }
}
