package com.bagatelle.fidelidade.repository;

import com.bagatelle.fidelidade.model.Cliente;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ClienteRepository extends JpaRepository<Cliente, Long> {

    Optional<Cliente> findByCpf(String cpf);

    boolean existsByCpf(String cpf);

    @Query("SELECT c FROM Cliente c WHERE " +
           "LOWER(c.nomeCompleto) LIKE LOWER(CONCAT('%', :termo, '%')) OR " +
           "c.cpf LIKE CONCAT('%', :termo, '%')")
    List<Cliente> buscarPorCpfOuNome(@Param("termo") String termo);

    @Query("SELECT c FROM Cliente c ORDER BY c.nomeCompleto")
    List<Cliente> findAllOrdenados();
}
