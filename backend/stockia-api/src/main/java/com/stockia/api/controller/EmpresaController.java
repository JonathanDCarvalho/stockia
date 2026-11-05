
package com.stockia.api.controller;

import com.stockia.api.dto.request.EmpresaRequest;
import com.stockia.api.dto.response.EmpresaResponse;
import com.stockia.api.entity.Empresa;
import com.stockia.api.service.EmpresaService;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/empresas")
public class EmpresaController {

    private final EmpresaService empresaService;

    public EmpresaController(EmpresaService empresaService) {
        this.empresaService = empresaService;
    }

    @PostMapping
    public ResponseEntity<EmpresaResponse> cadastrar(
            @RequestBody EmpresaRequest request) {

        Empresa empresa = new Empresa();

        empresa.setNome(request.nome());
        empresa.setCnpj(request.cnpj());

        Empresa empresaSalva = empresaService.cadastrar(empresa);

        EmpresaResponse response = new EmpresaResponse(
                empresaSalva.getId(),
                empresaSalva.getNome(),
                empresaSalva.getCnpj(),
                empresaSalva.getAtivo(),
                empresaSalva.getCreatedAt());

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    @GetMapping
    public ResponseEntity<List<EmpresaResponse>> listarTodos() {

        List<EmpresaResponse> empresas = empresaService.listarTodos()
                .stream()
                .map(empresa -> new EmpresaResponse(
                        empresa.getId(),
                        empresa.getNome(),
                        empresa.getCnpj(),
                        empresa.getAtivo(),
                        empresa.getCreatedAt()))
                .toList();

        return ResponseEntity.ok(empresas);
    }
}
