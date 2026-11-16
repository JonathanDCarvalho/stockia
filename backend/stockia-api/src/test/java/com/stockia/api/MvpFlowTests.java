package com.stockia.api;

import com.stockia.api.entity.Empresa;
import com.stockia.api.entity.Usuario;
import com.stockia.api.entity.Categoria;
import com.stockia.api.enums.PerfilUsuario;
import com.stockia.api.repository.EmpresaRepository;
import com.stockia.api.repository.UsuarioRepository;
import com.stockia.api.repository.CategoriaRepository;
import jakarta.servlet.Filter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;
import tools.jackson.databind.json.JsonMapper;

import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@Transactional
class MvpFlowTests {
    @Autowired WebApplicationContext context;
    @Autowired @Qualifier("springSecurityFilterChain") Filter securityFilter;
    @Autowired EmpresaRepository empresas;
    @Autowired UsuarioRepository usuarios;
    @Autowired PasswordEncoder passwordEncoder;
    @Autowired CategoriaRepository categorias;

    private MockMvc mvc;
    private final JsonMapper json = new JsonMapper();

    @BeforeEach
    void configureMvc() {
        mvc = MockMvcBuilders.webAppContextSetup(context).addFilters(securityFilter).build();
    }

    @Test
    void loginCadastrosEEstoqueUsamContratosReais() throws Exception {
        Empresa empresa = new Empresa();
        empresa.setNome("Empresa de teste MVP");
        empresas.saveAndFlush(empresa);

        String email = "mvp-" + UUID.randomUUID() + "@example.com";
        Usuario usuario = new Usuario();
        usuario.setEmpresa(empresa);
        usuario.setNome("Teste MVP");
        usuario.setEmail(email);
        usuario.setSenha(passwordEncoder.encode("senha-teste-mvp-2026"));
        usuario.setPerfil(PerfilUsuario.ADMIN);
        usuarios.saveAndFlush(usuario);

        String path = "/api/empresas/" + empresa.getId();
        mvc.perform(get(path + "/dashboard")).andExpect(status().isUnauthorized());
        mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                .content("{\"email\":\"" + email + "\",\"senha\":\"incorreta\"}"))
                .andExpect(status().isUnauthorized());

        String login = mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                .content("{\"email\":\"" + email + "\",\"senha\":\"senha-teste-mvp-2026\"}"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.empresaId").value(empresa.getId()))
                .andReturn().getResponse().getContentAsString();
        String authorization = "Bearer " + json.readTree(login).get("token").asText();

        mvc.perform(get("/api/usuarios/me").header("Authorization", authorization))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(usuario.getId()))
                .andExpect(jsonPath("$.empresaId").value(empresa.getId()))
                .andExpect(jsonPath("$.perfil").value("ADMIN"))
                .andExpect(jsonPath("$.senha").doesNotExist());

        mvc.perform(get(path + "/dashboard").header("Authorization", authorization))
                .andExpect(status().isOk()).andExpect(jsonPath("$.unidadesEmEstoque").value(0));

        String fornecedor = mvc.perform(post(path + "/fornecedores").header("Authorization", authorization)
                .contentType(MediaType.APPLICATION_JSON).content("{\"nome\":\"Fornecedor MVP\"}"))
                .andExpect(status().isCreated()).andReturn().getResponse().getContentAsString();
        long fornecedorId = json.readTree(fornecedor).get("id").asLong();

        String categoria = mvc.perform(post(path + "/categorias").header("Authorization", authorization)
                .contentType(MediaType.APPLICATION_JSON).content("{\"nome\":\"Categoria MVP\",\"descricao\":\"Teste\"}"))
                .andExpect(status().isCreated()).andReturn().getResponse().getContentAsString();
        long categoriaId = json.readTree(categoria).get("id").asLong();

        String produtoRequest = "{\"nome\":\"Produto MVP\",\"sku\":\"MVP-001\",\"precoCusto\":10.50,\"precoVenda\":20,\"estoqueMinimo\":2,\"fornecedorId\":" + fornecedorId + "}";
        String produto = mvc.perform(post(path + "/produtos").header("Authorization", authorization)
                .contentType(MediaType.APPLICATION_JSON).content(produtoRequest))
                .andExpect(status().isCreated()).andExpect(jsonPath("$.quantidadeEstoque").value(0))
                .andReturn().getResponse().getContentAsString();
        long produtoId = json.readTree(produto).get("id").asLong();

        mvc.perform(put(path + "/produtos/" + produtoId).header("Authorization", authorization)
                .contentType(MediaType.APPLICATION_JSON).content(produtoRequest))
                .andExpect(status().isOk());
        mvc.perform(get(path + "/produtos").header("Authorization", authorization))
                .andExpect(status().isOk()).andExpect(jsonPath("$[0].fornecedorId").value(fornecedorId));

        String movimentacoes = path + "/produtos/" + produtoId + "/movimentacoes";
        mvc.perform(post(movimentacoes).header("Authorization", authorization).contentType(MediaType.APPLICATION_JSON)
                .content("{\"tipo\":\"ENTRADA\",\"quantidade\":10,\"observacao\":\"Teste MVP\"}"))
                .andExpect(status().isCreated()).andExpect(jsonPath("$.estoquePosterior").value(10));
        mvc.perform(post(movimentacoes).header("Authorization", authorization).contentType(MediaType.APPLICATION_JSON)
                .content("{\"tipo\":\"SAIDA\",\"quantidade\":3}"))
                .andExpect(status().isCreated()).andExpect(jsonPath("$.estoquePosterior").value(7));
        mvc.perform(post(movimentacoes).header("Authorization", authorization).contentType(MediaType.APPLICATION_JSON)
                .content("{\"tipo\":\"SAIDA\",\"quantidade\":8}"))
                .andExpect(status().isBadRequest());
        mvc.perform(post(movimentacoes).header("Authorization", authorization).contentType(MediaType.APPLICATION_JSON)
                .content("{\"tipo\":\"AJUSTE\",\"quantidade\":0}"))
                .andExpect(status().isCreated()).andExpect(jsonPath("$.estoqueAnterior").value(7))
                .andExpect(jsonPath("$.estoquePosterior").value(0));
        mvc.perform(get(path + "/movimentacoes").header("Authorization", authorization))
                .andExpect(status().isOk()).andExpect(jsonPath("$.length()").value(3));
        mvc.perform(get(path + "/dashboard").header("Authorization", authorization))
                .andExpect(status().isOk()).andExpect(jsonPath("$.produtosCadastrados").value(1))
                .andExpect(jsonPath("$.estoqueBaixo").value(1)).andExpect(jsonPath("$.unidadesEmEstoque").value(0));
        mvc.perform(get("/api/empresas/" + (empresa.getId() + 1) + "/dashboard").header("Authorization", authorization))
                .andExpect(status().isForbidden());

        mvc.perform(delete(path + "/produtos/" + produtoId).header("Authorization", authorization))
                .andExpect(status().isNoContent());
        mvc.perform(patch(path + "/categorias/" + categoriaId + "/desativar").header("Authorization", authorization))
                .andExpect(status().isNoContent());
        mvc.perform(patch(path + "/fornecedores/" + fornecedorId + "/desativar").header("Authorization", authorization))
                .andExpect(status().isNoContent());
    }

    @Test
    void operadorAcessaOperacaoERecebe403NasRotasAdministrativas() throws Exception {
        Empresa empresa = new Empresa();
        empresa.setNome("Empresa de teste de permissoes");
        empresas.saveAndFlush(empresa);

        String email = "operador-" + UUID.randomUUID() + "@example.com";
        Usuario usuario = new Usuario();
        usuario.setEmpresa(empresa);
        usuario.setNome("Operador MVP");
        usuario.setEmail(email);
        usuario.setSenha(passwordEncoder.encode("senha-teste-mvp-2026"));
        usuario.setPerfil(PerfilUsuario.OPERADOR);
        usuarios.saveAndFlush(usuario);

        String login = mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                .content("{\"email\":\"" + email + "\",\"senha\":\"senha-teste-mvp-2026\"}"))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
        String authorization = "Bearer " + json.readTree(login).get("token").asText();
        String path = "/api/empresas/" + empresa.getId();

        mvc.perform(get("/api/usuarios/me").header("Authorization", authorization))
                .andExpect(status().isOk()).andExpect(jsonPath("$.perfil").value("OPERADOR"));
        mvc.perform(get(path + "/dashboard").header("Authorization", authorization))
                .andExpect(status().isOk());
        mvc.perform(get(path + "/produtos").header("Authorization", authorization))
                .andExpect(status().isOk());
        mvc.perform(get(path + "/usuarios").header("Authorization", authorization))
                .andExpect(status().isForbidden());
        mvc.perform(get(path + "/auditoria").header("Authorization", authorization))
                .andExpect(status().isForbidden());
        mvc.perform(put(path).header("Authorization", authorization)
                .contentType(MediaType.APPLICATION_JSON).content("{\"nome\":\"Alteracao negada\"}"))
                .andExpect(status().isForbidden());
    }

    @Test
    void administradorGerenciaCategoriasUsuariosEmpresaEAuditoria() throws Exception {
        Empresa empresa = new Empresa();
        empresa.setNome("Empresa administrativa MVP");
        empresas.saveAndFlush(empresa);

        String email = "admin-" + UUID.randomUUID() + "@example.com";
        Usuario administrador = new Usuario();
        administrador.setEmpresa(empresa);
        administrador.setNome("Administrador MVP");
        administrador.setEmail(email);
        administrador.setSenha(passwordEncoder.encode("senha-teste-mvp-2026"));
        administrador.setPerfil(PerfilUsuario.ADMIN);
        usuarios.saveAndFlush(administrador);

        String login = mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                .content("{\"email\":\"" + email + "\",\"senha\":\"senha-teste-mvp-2026\"}"))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
        String authorization = "Bearer " + json.readTree(login).get("token").asText();
        String path = "/api/empresas/" + empresa.getId();

        mvc.perform(get(path).header("Authorization", authorization))
                .andExpect(status().isOk()).andExpect(jsonPath("$.nome").value("Empresa administrativa MVP"));
        mvc.perform(get("/api/empresas").header("Authorization", authorization))
                .andExpect(status().isForbidden());
        mvc.perform(post("/api/empresas").header("Authorization", authorization)
                .contentType(MediaType.APPLICATION_JSON).content("{\"nome\":\"Empresa global negada\"}"))
                .andExpect(status().isForbidden());
        mvc.perform(put(path).header("Authorization", authorization)
                .contentType(MediaType.APPLICATION_JSON).content("{\"nome\":\"Empresa renomeada\"}"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.nome").value("Empresa renomeada"));

        String categoria = mvc.perform(post(path + "/categorias").header("Authorization", authorization)
                .contentType(MediaType.APPLICATION_JSON).content("{\"nome\":\"Categoria administrativa\"}"))
                .andExpect(status().isCreated()).andReturn().getResponse().getContentAsString();
        long categoriaId = json.readTree(categoria).get("id").asLong();
        mvc.perform(get(path + "/categorias").header("Authorization", authorization))
                .andExpect(status().isOk()).andExpect(jsonPath("$[0].id").value(categoriaId));
        mvc.perform(post(path + "/categorias").header("Authorization", authorization)
                .contentType(MediaType.APPLICATION_JSON).content("{\"nome\":\"Categoria administrativa\"}"))
                .andExpect(status().isConflict());
        mvc.perform(put(path + "/categorias/" + categoriaId).header("Authorization", authorization)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"nome\":\"Categoria editada\",\"descricao\":\"Descricao editada\"}"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.descricao").value("Descricao editada"));
        mvc.perform(patch(path + "/categorias/" + categoriaId + "/desativar")
                .header("Authorization", authorization)).andExpect(status().isNoContent());
        mvc.perform(get(path + "/categorias").header("Authorization", authorization))
                .andExpect(status().isOk()).andExpect(jsonPath("$.length()").value(0));

        String novoEmail = "novo-" + UUID.randomUUID() + "@example.com";
        String novoUsuarioRequest = "{\"nome\":\"Novo operador\",\"email\":\"" + novoEmail
                + "\",\"senha\":\"senha-operador-teste-2026\",\"perfil\":\"OPERADOR\"}";
        String novoUsuario = mvc.perform(post(path + "/usuarios").header("Authorization", authorization)
                .contentType(MediaType.APPLICATION_JSON).content(novoUsuarioRequest))
                .andExpect(status().isCreated()).andExpect(jsonPath("$.senha").doesNotExist())
                .andReturn().getResponse().getContentAsString();
        long novoUsuarioId = json.readTree(novoUsuario).get("id").asLong();
        mvc.perform(get(path + "/usuarios").header("Authorization", authorization))
                .andExpect(status().isOk()).andExpect(jsonPath("$.length()").value(2));
        mvc.perform(post(path + "/usuarios").header("Authorization", authorization)
                .contentType(MediaType.APPLICATION_JSON).content(novoUsuarioRequest))
                .andExpect(status().isConflict());
        mvc.perform(patch(path + "/usuarios/" + administrador.getId() + "/desativar")
                .header("Authorization", authorization)).andExpect(status().isConflict());
        mvc.perform(patch(path + "/usuarios/" + novoUsuarioId + "/desativar")
                .header("Authorization", authorization)).andExpect(status().isNoContent());
        mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                .content("{\"email\":\"" + novoEmail + "\",\"senha\":\"senha-operador-teste-2026\"}"))
                .andExpect(status().isUnauthorized());

        mvc.perform(get(path + "/auditoria").header("Authorization", authorization))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.entidade == 'CATEGORIA')]").isNotEmpty())
                .andExpect(jsonPath("$[?(@.entidade == 'USUARIO')]").isNotEmpty())
                .andExpect(jsonPath("$[0].usuarioId").value(administrador.getId()));
        mvc.perform(get(path + "/auditoria/registros/CATEGORIA/" + categoriaId)
                .header("Authorization", authorization))
                .andExpect(status().isOk()).andExpect(jsonPath("$.length()").value(3));
        mvc.perform(delete(path).header("Authorization", authorization))
                .andExpect(status().isNoContent());
        mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                .content("{\"email\":\"" + email + "\",\"senha\":\"senha-teste-mvp-2026\"}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void produtoAssociaTrocaERemoveCategoriaDaMesmaEmpresa() throws Exception {
        Empresa empresa = new Empresa();
        empresa.setNome("Empresa de categorias de produto");
        empresas.saveAndFlush(empresa);
        Empresa outraEmpresa = new Empresa();
        outraEmpresa.setNome("Outra empresa de categorias");
        empresas.saveAndFlush(outraEmpresa);

        Categoria categoria = new Categoria();
        categoria.setEmpresa(empresa);
        categoria.setNome("Categoria inicial");
        categorias.saveAndFlush(categoria);
        Categoria segundaCategoria = new Categoria();
        segundaCategoria.setEmpresa(empresa);
        segundaCategoria.setNome("Segunda categoria");
        categorias.saveAndFlush(segundaCategoria);
        Categoria categoriaExterna = new Categoria();
        categoriaExterna.setEmpresa(outraEmpresa);
        categoriaExterna.setNome("Categoria externa");
        categorias.saveAndFlush(categoriaExterna);

        String email = "categoria-" + UUID.randomUUID() + "@example.com";
        Usuario administrador = new Usuario();
        administrador.setEmpresa(empresa);
        administrador.setNome("Administrador de categorias");
        administrador.setEmail(email);
        administrador.setSenha(passwordEncoder.encode("senha-teste-mvp-2026"));
        administrador.setPerfil(PerfilUsuario.ADMIN);
        usuarios.saveAndFlush(administrador);
        String login = mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                .content("{\"email\":\"" + email + "\",\"senha\":\"senha-teste-mvp-2026\"}"))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
        String authorization = "Bearer " + json.readTree(login).get("token").asText();
        String path = "/api/empresas/" + empresa.getId();

        String fornecedor = mvc.perform(post(path + "/fornecedores").header("Authorization", authorization)
                .contentType(MediaType.APPLICATION_JSON).content("{\"nome\":\"Fornecedor de produto categorizado\"}"))
                .andExpect(status().isCreated()).andReturn().getResponse().getContentAsString();
        long fornecedorId = json.readTree(fornecedor).get("id").asLong();
        String base = "{\"nome\":\"Produto categorizado\",\"sku\":\"CATEGORIA-001\","
                + "\"precoCusto\":10,\"precoVenda\":20,\"estoqueMinimo\":2,\"fornecedorId\":" + fornecedorId;
        String produto = mvc.perform(post(path + "/produtos").header("Authorization", authorization)
                .contentType(MediaType.APPLICATION_JSON)
                .content(base + ",\"quantidadeInicial\":10,\"categoriaId\":" + categoria.getId() + "}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.categoriaId").value(categoria.getId()))
                .andExpect(jsonPath("$.categoriaNome").value("Categoria inicial"))
                .andExpect(jsonPath("$.quantidadeEstoque").value(10))
                .andReturn().getResponse().getContentAsString();
        long produtoId = json.readTree(produto).get("id").asLong();
        mvc.perform(get(path + "/produtos").header("Authorization", authorization))
                .andExpect(status().isOk()).andExpect(jsonPath("$[0].categoriaNome").value("Categoria inicial"));
        mvc.perform(get(path + "/produtos/" + produtoId).header("Authorization", authorization))
                .andExpect(status().isOk()).andExpect(jsonPath("$.categoriaId").value(categoria.getId()));

        mvc.perform(put(path + "/produtos/" + produtoId).header("Authorization", authorization)
                .contentType(MediaType.APPLICATION_JSON)
                .content(base + ",\"categoriaId\":" + segundaCategoria.getId() + "}"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.categoriaNome").value("Segunda categoria"))
                .andExpect(jsonPath("$.quantidadeEstoque").value(10));
        mvc.perform(put(path + "/produtos/" + produtoId).header("Authorization", authorization)
                .contentType(MediaType.APPLICATION_JSON)
                .content(base + ",\"categoriaId\":" + categoriaExterna.getId() + "}"))
                .andExpect(status().isNotFound());
        mvc.perform(post(path + "/produtos").header("Authorization", authorization)
                .contentType(MediaType.APPLICATION_JSON)
                .content(base.replace("CATEGORIA-001", "CATEGORIA-EXTERNA")
                        + ",\"categoriaId\":" + categoriaExterna.getId() + "}"))
                .andExpect(status().isNotFound());

        mvc.perform(patch(path + "/categorias/" + segundaCategoria.getId() + "/desativar")
                .header("Authorization", authorization)).andExpect(status().isNoContent());
        mvc.perform(put(path + "/produtos/" + produtoId).header("Authorization", authorization)
                .contentType(MediaType.APPLICATION_JSON)
                .content(base + ",\"categoriaId\":" + segundaCategoria.getId() + "}"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.categoriaId").value(segundaCategoria.getId()));
        mvc.perform(post(path + "/produtos").header("Authorization", authorization)
                .contentType(MediaType.APPLICATION_JSON)
                .content(base.replace("CATEGORIA-001", "CATEGORIA-INATIVA")
                        + ",\"categoriaId\":" + segundaCategoria.getId() + "}"))
                .andExpect(status().isNotFound());

        mvc.perform(put(path + "/produtos/" + produtoId).header("Authorization", authorization)
                .contentType(MediaType.APPLICATION_JSON).content(base + ",\"categoriaId\":null}"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.categoriaId").isEmpty())
                .andExpect(jsonPath("$.categoriaNome").isEmpty())
                .andExpect(jsonPath("$.quantidadeEstoque").value(10));
        mvc.perform(get(path + "/produtos/" + produtoId + "/movimentacoes")
                .header("Authorization", authorization))
                .andExpect(status().isOk()).andExpect(jsonPath("$.length()").value(1));
        mvc.perform(get(path + "/produtos").header("Authorization", authorization))
                .andExpect(status().isOk()).andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    void cadastroRegistraQuantidadeInicialComMovimentacaoEAuditoria() throws Exception {
        Empresa empresa = new Empresa();
        empresa.setNome("Empresa de estoque inicial");
        empresas.saveAndFlush(empresa);

        String email = "estoque-" + UUID.randomUUID() + "@example.com";
        Usuario administrador = new Usuario();
        administrador.setEmpresa(empresa);
        administrador.setNome("Administrador de estoque");
        administrador.setEmail(email);
        administrador.setSenha(passwordEncoder.encode("senha-teste-mvp-2026"));
        administrador.setPerfil(PerfilUsuario.ADMIN);
        usuarios.saveAndFlush(administrador);

        String login = mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                .content("{\"email\":\"" + email + "\",\"senha\":\"senha-teste-mvp-2026\"}"))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
        String authorization = "Bearer " + json.readTree(login).get("token").asText();
        String path = "/api/empresas/" + empresa.getId();

        String fornecedor = mvc.perform(post(path + "/fornecedores").header("Authorization", authorization)
                .contentType(MediaType.APPLICATION_JSON).content("{\"nome\":\"Fornecedor inicial\"}"))
                .andExpect(status().isCreated()).andReturn().getResponse().getContentAsString();
        long fornecedorId = json.readTree(fornecedor).get("id").asLong();

        String request = "{\"nome\":\"Produto com saldo\",\"sku\":\"INICIAL-001\","
                + "\"precoCusto\":10,\"precoVenda\":20,\"estoqueMinimo\":2,"
                + "\"fornecedorId\":" + fornecedorId + ",\"quantidadeInicial\":15}";
        String produto = mvc.perform(post(path + "/produtos").header("Authorization", authorization)
                .contentType(MediaType.APPLICATION_JSON).content(request))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.quantidadeEstoque").value(15))
                .andReturn().getResponse().getContentAsString();
        long produtoId = json.readTree(produto).get("id").asLong();

        mvc.perform(get(path + "/produtos/" + produtoId).header("Authorization", authorization))
                .andExpect(status().isOk()).andExpect(jsonPath("$.quantidadeEstoque").value(15));
        mvc.perform(get(path + "/produtos/" + produtoId + "/movimentacoes")
                .header("Authorization", authorization))
                .andExpect(status().isOk()).andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].tipo").value("ENTRADA"))
                .andExpect(jsonPath("$[0].quantidade").value(15))
                .andExpect(jsonPath("$[0].estoqueAnterior").value(0))
                .andExpect(jsonPath("$[0].estoquePosterior").value(15))
                .andExpect(jsonPath("$[0].observacao").value("Estoque inicial"));
        mvc.perform(get(path + "/dashboard").header("Authorization", authorization))
                .andExpect(status().isOk()).andExpect(jsonPath("$.unidadesEmEstoque").value(15))
                .andExpect(jsonPath("$.valorTotalEstoque").value(150));
        mvc.perform(get(path + "/auditoria").header("Authorization", authorization))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.entidade == 'MOVIMENTACAO_ESTOQUE')]").isNotEmpty());

        mvc.perform(post(path + "/produtos").header("Authorization", authorization)
                .contentType(MediaType.APPLICATION_JSON).content(request))
                .andExpect(status().isConflict());
        mvc.perform(get(path + "/produtos/" + produtoId + "/movimentacoes")
                .header("Authorization", authorization))
                .andExpect(status().isOk()).andExpect(jsonPath("$.length()").value(1));

        mvc.perform(put(path + "/produtos/" + produtoId).header("Authorization", authorization)
                .contentType(MediaType.APPLICATION_JSON).content(request.replace(",\"quantidadeInicial\":15", "")))
                .andExpect(status().isOk()).andExpect(jsonPath("$.quantidadeEstoque").value(15));
        mvc.perform(post(path + "/produtos").header("Authorization", authorization)
                .contentType(MediaType.APPLICATION_JSON)
                .content(request.replace("INICIAL-001", "INICIAL-NEGATIVO")
                        .replace("\"quantidadeInicial\":15", "\"quantidadeInicial\":-1")))
                .andExpect(status().isBadRequest());
        mvc.perform(get(path + "/produtos").header("Authorization", authorization))
                .andExpect(status().isOk()).andExpect(jsonPath("$.length()").value(1));

        String semSaldo = request.replace("INICIAL-001", "INICIAL-ZERO")
                .replace("\"quantidadeInicial\":15", "\"quantidadeInicial\":0");
        String produtoSemSaldo = mvc.perform(post(path + "/produtos").header("Authorization", authorization)
                .contentType(MediaType.APPLICATION_JSON).content(semSaldo))
                .andExpect(status().isCreated()).andExpect(jsonPath("$.quantidadeEstoque").value(0))
                .andReturn().getResponse().getContentAsString();
        long produtoSemSaldoId = json.readTree(produtoSemSaldo).get("id").asLong();
        mvc.perform(get(path + "/produtos/" + produtoSemSaldoId + "/movimentacoes")
                .header("Authorization", authorization))
                .andExpect(status().isOk()).andExpect(jsonPath("$.length()").value(0));
    }
}
