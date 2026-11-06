
package com.stockia.api.controller;

import com.stockia.api.dto.request.EmpresaRequest;
import com.stockia.api.dto.response.EmpresaResponse;
import com.stockia.api.entity.Empresa;
import com.stockia.api.service.EmpresaService;

import jakarta.validation.Valid;

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
                        @Valid @RequestBody EmpresaRequest request) {

                Empresa empresa = new Empresa();

                empresa.setNome(request.nome());
                empresa.setCnpj(request.cnpj());

                Empresa empresaSalva = empresaService.cadastrar(empresa);

                return ResponseEntity
                                .status(HttpStatus.CREATED)
                                .body(toResponse(empresaSalva));
        }

        @GetMapping
        public ResponseEntity<List<EmpresaResponse>> listarTodos() {

                List<EmpresaResponse> empresas = empresaService.listarTodos()
                                .stream()
                                .map(this::toResponse)
                                .toList();

                return ResponseEntity.ok(empresas);
        }

        @GetMapping("/{id}")
        public ResponseEntity<EmpresaResponse> buscarPorId(
                        @PathVariable Long id) {

                Empresa empresa = empresaService.buscarPorId(id);

                return ResponseEntity.ok(toResponse(empresa));
        }

        @PutMapping("/{id}")
        public ResponseEntity<EmpresaResponse> atualizar(
                        @PathVariable Long id,
                        @RequestBody EmpresaRequest request) {

                Empresa dadosAtualizados = new Empresa();
                dadosAtualizados.setNome(request.nome());

                Empresa empresaAtualizada = empresaService.atualizar(id, dadosAtualizados);

                return ResponseEntity.ok(toResponse(empresaAtualizada));
        }

        @DeleteMapping("/{id}")
        public ResponseEntity<Void> desativar(@PathVariable Long id) {

                empresaService.desativar(id);

                return ResponseEntity.noContent().build();
        }

        private EmpresaResponse toResponse(Empresa empresa) {

                return new EmpresaResponse(
                                empresa.getId(),
                                empresa.getNome(),
                                empresa.getCnpj(),
                                empresa.getAtivo(),
                                empresa.getCreatedAt());
        }
}
