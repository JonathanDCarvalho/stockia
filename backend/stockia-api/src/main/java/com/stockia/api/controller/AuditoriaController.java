package com.stockia.api.controller;

import com.stockia.api.dto.response.AuditoriaResponse;
import com.stockia.api.entity.Auditoria;
import com.stockia.api.service.AuditoriaService;

import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/empresas/{empresaId}/auditoria")
public class AuditoriaController {

    private final AuditoriaService auditoriaService;

    public AuditoriaController(AuditoriaService auditoriaService) {
        this.auditoriaService = auditoriaService;
    }

    @GetMapping
    public List<AuditoriaResponse> listar(
            @PathVariable Long empresaId) {

        return auditoriaService.listarPorEmpresa(empresaId)
                .stream()
                .map(this::converterParaResponse)
                .toList();
    }

    @GetMapping("/registros/{entidade}/{registroId}")
    public List<AuditoriaResponse> listarPorRegistro(
            @PathVariable Long empresaId,
            @PathVariable String entidade,
            @PathVariable Long registroId) {

        return auditoriaService.listarPorRegistro(
                empresaId,
                entidade.toUpperCase(java.util.Locale.ROOT),
                registroId)
                .stream()
                .map(this::converterParaResponse)
                .toList();
    }

    private AuditoriaResponse converterParaResponse(Auditoria auditoria) {

        return new AuditoriaResponse(
                auditoria.getId(),
                auditoria.getEmpresa().getId(),
                auditoria.getUsuarioId(),
                auditoria.getEntidade(),
                auditoria.getRegistroId(),
                auditoria.getAcao(),
                auditoria.getDescricao(),
                auditoria.getCreatedAt());
    }
}