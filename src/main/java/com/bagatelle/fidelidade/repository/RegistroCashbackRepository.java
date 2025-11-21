package com.bagatelle.fidelidade.repository;

import com.bagatelle.fidelidade.model.RegistroCashback;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface RegistroCashbackRepository extends JpaRepository<RegistroCashback, Long> {

    @Query("SELECT r FROM RegistroCashback r " +
           "JOIN FETCH r.cliente " +
           "WHERE r.dataExpiracao BETWEEN :hoje AND :dataLimite " +
           "AND r.utilizado = false " +
           "AND r.dataExpiracao > :hoje " +
           "ORDER BY r.dataExpiracao ASC")
    List<RegistroCashback> buscarCashbackExpirando(@Param("hoje") LocalDate hoje,
                                                    @Param("dataLimite") LocalDate dataLimite);

    @Query("SELECT r FROM RegistroCashback r " +
           "WHERE r.cliente.id = :clienteId " +
           "AND r.utilizado = false " +
           "AND r.dataExpiracao > :hoje " +
           "ORDER BY r.dataExpiracao ASC")
    List<RegistroCashback> buscarPorClienteNaoUtilizado(@Param("clienteId") Long clienteId,
                                                         @Param("hoje") LocalDate hoje);

    @Query("SELECT r FROM RegistroCashback r " +
           "WHERE r.cliente.id = :clienteId " +
           "ORDER BY r.dataGeracao DESC")
    List<RegistroCashback> buscarPorCliente(@Param("clienteId") Long clienteId);

    @Query("SELECT COALESCE(SUM(r.valor - r.valorUtilizado), 0) " +
           "FROM RegistroCashback r " +
           "WHERE r.cliente.id = :clienteId " +
           "AND r.utilizado = false " +
           "AND r.dataExpiracao > :hoje")
    java.math.BigDecimal calcularSaldoDisponivel(@Param("clienteId") Long clienteId,
                                                  @Param("hoje") LocalDate hoje);
}
