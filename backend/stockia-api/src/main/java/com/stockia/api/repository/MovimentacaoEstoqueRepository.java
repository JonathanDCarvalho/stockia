
package com.stockia.api.repository;

import com.stockia.api.entity.MovimentacaoEstoque;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MovimentacaoEstoqueRepository
        extends JpaRepository<MovimentacaoEstoque, Long> {

    @EntityGraph(attributePaths = { "empresa", "produto" })
    List<MovimentacaoEstoque> findAllByEmpresaIdOrderByCreatedAtDesc(
            Long empresaId);

    @EntityGraph(attributePaths = { "empresa", "produto" })
    List<MovimentacaoEstoque> findAllByEmpresaIdAndProdutoIdOrderByCreatedAtDesc(
            Long empresaId, Long produtoId);
}
