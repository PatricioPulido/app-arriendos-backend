package com.apparriendos.arriendos_api.mapper;

import com.apparriendos.arriendos_api.model.PasswordResetToken;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;

@Mapper
public interface PasswordResetMapper {
    void insert(PasswordResetToken token);
    PasswordResetToken findByHash(@Param("tokenHash") String tokenHash);
    /** Cuantos enlaces pidio este usuario desde esa fecha (para no llenarle la bandeja). */
    int countDesde(@Param("idUsuario") Long idUsuario, @Param("desde") LocalDateTime desde);
    /** Devuelve 1 si lo marco ahora, 0 si ya estaba usado (otra peticion gano). */
    int marcarUsado(@Param("id") Long id, @Param("ahora") LocalDateTime ahora);
    /** Anula los demas enlaces pendientes del usuario tras cambiar la clave. */
    void anularPendientes(@Param("idUsuario") Long idUsuario, @Param("ahora") LocalDateTime ahora);
}
