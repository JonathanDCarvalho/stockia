package com.stockia.api.repository;

import com.stockia.api.entity.Empresa;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface EmpresaRepository extends JpaRepository<Empresa, Long> {
    boolean existsByCnpj(String cnpj);

    Optional<Empresa> findByIdAndAtivoTrue(Long id);

    List<Empresa> findAllByAtivoTrue();
}
