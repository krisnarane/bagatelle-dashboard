package com.bagatelle.fidelidade.service;

import com.bagatelle.fidelidade.model.Produto;
import com.bagatelle.fidelidade.repository.ProdutoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class ProdutoService {

    private final ProdutoRepository produtoRepository;

    public List<Produto> listarTodos() {
        return produtoRepository.findAllOrdenados();
    }

    public Produto buscarPorId(Long id) {
        return produtoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Produto não encontrado com ID: " + id));
    }

    public List<Produto> buscarPorNomeOuMarca(String termo) {
        return produtoRepository.buscarPorNomeOuMarca(termo);
    }

    public List<Produto> buscarPorMarca(String marca) {
        return produtoRepository.findByMarcaIgnoreCase(marca);
    }

    public Produto salvar(Produto produto) {
        validarProduto(produto);
        return produtoRepository.save(produto);
    }

    public Produto atualizar(Long id, Produto produtoAtualizado) {
        Produto produtoExistente = buscarPorId(id);

        produtoExistente.setNomePerfume(produtoAtualizado.getNomePerfume());
        produtoExistente.setMarca(produtoAtualizado.getMarca());
        produtoExistente.setVolume(produtoAtualizado.getVolume());
        produtoExistente.setPrecoVenda(produtoAtualizado.getPrecoVenda());

        return produtoRepository.save(produtoExistente);
    }

    public void deletar(Long id) {
        Produto produto = buscarPorId(id);
        produtoRepository.delete(produto);
    }

    private void validarProduto(Produto produto) {
        if (produto.getPrecoVenda() == null || produto.getPrecoVenda().compareTo(java.math.BigDecimal.ZERO) <= 0) {
            throw new RuntimeException("Preço de venda deve ser maior que zero");
        }
    }
}
