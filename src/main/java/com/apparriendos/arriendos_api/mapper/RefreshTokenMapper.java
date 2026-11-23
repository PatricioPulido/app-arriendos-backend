package com.apparriendos.arriendos_api.mapper;

import com.apparriendos.arriendos_api.model.RefreshToken;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface RefreshTokenMapper {
    RefreshToken findByHash(@Param("tokenHash") String tokenHash);
    void insert(RefreshToken token);
    void revoke(@Param("id") Long id);
    void revokeAllForUser(@Param("idUsuario") Long idUsuario);
}
