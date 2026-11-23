package com.apparriendos.arriendos_api.mapper;

import com.apparriendos.arriendos_api.model.RefreshToken;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface RefreshTokenMapper {
    RefreshToken findByHash(@Param("tokenHash") String tokenHash);
    void insert(RefreshToken token);
    /** Devuelve 1 si lo revoco ahora, 0 si ya estaba revocado (otra peticion gano). */
    int revoke(@Param("id") Long id);
    void revokeAllForUser(@Param("idUsuario") Long idUsuario);
}
