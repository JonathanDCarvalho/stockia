package com.stockia.api.repository;

import com.stockia.api.entity.Auditoria;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AuditoriaRepository extends JpaRepository<Auditoria, Long> {

    List<Auditoria> findAllByEmpresaIdOrderByCreatedAtDescIdDesc(
            Long empresaId);

    List<Auditoria> findAllByEmpresaIdAndEntidadeAndRegistroIdOrderByCreatedAtDescIdDesc(
            Long empresaId,
            String entidade,
            Long registroId);
}