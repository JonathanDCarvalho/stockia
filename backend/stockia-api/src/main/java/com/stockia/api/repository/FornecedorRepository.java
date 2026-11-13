
package com.stockia.api.repository;

import com.stockia.api.entity.Fornecedor;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface FornecedorRepository extends JpaRepository<Fornecedor, Long> {

    List<Fornecedor> findAllByEmpresaIdAndAtivoTrue(Long empresaId);

    java.util.Optional<Fornecedor> findByIdAndEmpresaIdAndAtivoTrue(
            Long id,
            Long empresaId);

    boolean existsByEmpresaIdAndDocumento(
            Long empresaId,
            String documento);

    boolean existsByEmpresaIdAndDocumentoAndIdNot(
            Long empresaId,
            String documento,
            Long id);
}
