package com.stockia.api;

import com.stockia.api.entity.Empresa;
import com.stockia.api.entity.Usuario;
import com.stockia.api.enums.PerfilUsuario;
import com.stockia.api.repository.EmpresaRepository;
import com.stockia.api.repository.UsuarioRepository;
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
}
