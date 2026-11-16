package com.stockia.api.repository;

import com.stockia.api.entity.Produto;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProdutoRepository extends JpaRepository<Produto, Long> {

        List<Produto> findByEmpresaId(Long empresaId);

        @EntityGraph(attributePaths = { "empresa", "fornecedor" })
        List<Produto> findAllByEmpresaIdAndAtivoTrue(Long empresaId);

        @EntityGraph(attributePaths = { "empresa", "fornecedor" })
        Optional<Produto> findByIdAndEmpresaIdAndAtivoTrue(
                        Long id, Long empresaId);

        Optional<Produto> findByEmpresaIdAndSku(
                        Long empresaId, String sku);

        boolean existsByEmpresaIdAndSku(
                        Long empresaId, String sku);

        boolean existsByEmpresaIdAndSkuAndIdNot(
                        Long empresaId, String sku, Long id);

        @Lock(LockModeType.PESSIMISTIC_WRITE)
        @Query("""
                            SELECT p FROM Produto p
                            WHERE p.id = :produtoId
                            AND p.empresa.id = :empresaId
                            AND p.ativo = true
                        """)
        Optional<Produto> buscarParaMovimentacao(
                        @Param("produtoId") Long produtoId,
                        @Param("empresaId") Long empresaId);

        @Query("""
                        SELECT SUM(p.precoCusto * p.quantidadeEstoque)
                        FROM Produto p
                        WHERE p.empresa.id = :empresaId
                          AND p.ativo = true
                        """)
        BigDecimal calcularValorTotalEstoque(
                        @Param("empresaId") Long empresaId);

        @Query("""
                        SELECT COUNT(p)
                        FROM Produto p
                        WHERE p.empresa.id = :empresaId
                          AND p.ativo = true
                        """)
        Long contarProdutosAtivos(
                        @Param("empresaId") Long empresaId);

        @Query("""
                        SELECT SUM(p.quantidadeEstoque)
                        FROM Produto p
                        WHERE p.empresa.id = :empresaId
                          AND p.ativo = true
                        """)
        Long somarUnidadesEmEstoque(
                        @Param("empresaId") Long empresaId);

        @Query("""
                        SELECT COUNT(p)
                        FROM Produto p
                        WHERE p.empresa.id = :empresaId
                          AND p.ativo = true
                          AND p.quantidadeEstoque <= p.estoqueMinimo
                        """)
        Long contarProdutosEstoqueBaixo(
                        @Param("empresaId") Long empresaId);

}