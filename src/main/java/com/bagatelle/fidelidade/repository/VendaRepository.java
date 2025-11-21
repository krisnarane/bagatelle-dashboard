package com.bagatelle.fidelidade.repository;

import com.bagatelle.fidelidade.model.Venda;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface VendaRepository extends JpaRepository<Venda, Long> {

    @Query("SELECT v FROM Venda v " +
           "JOIN FETCH v.cliente " +
           "JOIN FETCH v.itens i " +
           "JOIN FETCH i.produto " +
           "WHERE v.dataVenda BETWEEN :inicio AND :fim " +
           "ORDER BY v.dataVenda DESC")
    List<Venda> buscarVendasEntreDatas(@Param("inicio") LocalDateTime inicio,
                                        @Param("fim") LocalDateTime fim);

    @Query("SELECT v FROM Venda v " +
           "JOIN FETCH v.cliente c " +
           "JOIN FETCH v.itens i " +
           "JOIN FETCH i.produto p " +
           "WHERE v.dataVenda >= :dataInicio " +
           "AND v.dataVenda < :dataFim " +
           "ORDER BY v.dataVenda DESC")
    List<Venda> buscarVendasDe5MesesAtras(@Param("dataInicio") LocalDateTime dataInicio,
                                           @Param("dataFim") LocalDateTime dataFim);

    @Query("SELECT v FROM Venda v " +
           "JOIN FETCH v.cliente " +
           "LEFT JOIN FETCH v.itens " +
           "WHERE v.cliente.id = :clienteId " +
           "ORDER BY v.dataVenda DESC")
    List<Venda> buscarPorCliente(@Param("clienteId") Long clienteId);

    @Query("SELECT v FROM Venda v " +
           "LEFT JOIN FETCH v.cliente " +
           "LEFT JOIN FETCH v.itens " +
           "ORDER BY v.dataVenda DESC")
    List<Venda> findAllOrdenadas();
}
