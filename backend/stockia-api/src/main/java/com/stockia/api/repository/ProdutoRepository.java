package com.stockia.api.repository;

import com.stockia.api.entity.Produto;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface ProdutoRepository extends JpaRepository<Produto, Long> {
    List<Produto> findByEmpresaId(Long empresaId);

    Optional<Produto> findByEmpresaIdAndSku(Long empresaId, String sku);

    boolean existsByEmpresaIdAndSku(Long empresaId, String sku);

    List<Produto> findAllByEmpresaIdAndAtivoTrue(Long empresaId);

    Optional<Produto> findByIdAndEmpresaIdAndAtivoTrue(
            Long id,
            Long empresaId);
}
