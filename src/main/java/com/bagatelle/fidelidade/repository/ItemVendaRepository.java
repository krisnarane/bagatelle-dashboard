package com.bagatelle.fidelidade.repository;

import com.bagatelle.fidelidade.model.ItemVenda;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ItemVendaRepository extends JpaRepository<ItemVenda, Long> {

    @Query("SELECT i FROM ItemVenda i WHERE i.venda.id = :vendaId")
    List<ItemVenda> findByVendaId(@Param("vendaId") Long vendaId);

    @Query("SELECT i FROM ItemVenda i WHERE i.produto.id = :produtoId")
    List<ItemVenda> findByProdutoId(@Param("produtoId") Long produtoId);
}
