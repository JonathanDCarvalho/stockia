
package com.stockia.api.repository;

import com.stockia.api.entity.Usuario;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

        @Query("""
                        SELECT u
                        FROM Usuario u
                        JOIN FETCH u.empresa
                        WHERE u.id = :id
                        """)
        Optional<Usuario> buscarPorIdComEmpresa(
                        @Param("id") Long id);

        @Query("""
                        SELECT u
                        FROM Usuario u
                        JOIN FETCH u.empresa
                        WHERE LOWER(u.email) = LOWER(:email)
                        """)
        Optional<Usuario> buscarPorEmail(
                        @Param("email") String email);

        @Query("""
                        SELECT CASE WHEN COUNT(u) > 0 THEN true ELSE false END
                        FROM Usuario u
                        WHERE LOWER(u.email) = LOWER(:email)
                        """)
        boolean existeEmail(
                        @Param("email") String email);

        @Query("""
                        SELECT COUNT(u)
                        FROM Usuario u
                        WHERE u.empresa.id = :empresaId
                          AND u.perfil = com.stockia.api.enums.PerfilUsuario.ADMIN
                          AND u.ativo = true
                        """)
        long contarAdministradoresAtivos(
                        @Param("empresaId") Long empresaId);

        List<Usuario> findAllByEmpresaIdAndAtivoTrue(
                        Long empresaId);

        Optional<Usuario> findByIdAndEmpresaIdAndAtivoTrue(
                        Long id,
                        Long empresaId);
}
