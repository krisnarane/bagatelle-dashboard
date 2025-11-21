package com.bagatelle.fidelidade.repository;

import com.bagatelle.fidelidade.model.Produto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProdutoRepository extends JpaRepository<Produto, Long> {

    @Query("SELECT p FROM Produto p WHERE " +
           "LOWER(p.nomePerfume) LIKE LOWER(CONCAT('%', :termo, '%')) OR " +
           "LOWER(p.marca) LIKE LOWER(CONCAT('%', :termo, '%'))")
    List<Produto> buscarPorNomeOuMarca(@Param("termo") String termo);

    @Query("SELECT p FROM Produto p ORDER BY p.nomePerfume")
    List<Produto> findAllOrdenados();

    List<Produto> findByMarcaIgnoreCase(String marca);
}
