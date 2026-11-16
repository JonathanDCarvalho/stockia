
package com.stockia.api.repository;

import com.stockia.api.entity.Categoria;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface CategoriaRepository extends JpaRepository<Categoria, Long> {

        long countByEmpresaIdAndAtivoTrue(Long empresaId);

        List<Categoria> findAllByEmpresaIdAndAtivoTrue(Long empresaId);

        Optional<Categoria> findByIdAndEmpresaIdAndAtivoTrue(
                        Long id, Long empresaId);

        Optional<Categoria> findByIdAndEmpresaId(Long id, Long empresaId);

        boolean existsByEmpresaIdAndNome(Long empresaId, String nome);

        boolean existsByEmpresaIdAndNomeAndIdNot(
                        Long empresaId, String nome, Long id);
}
