package com.apparriendos.arriendos_api.mapper;

import com.apparriendos.arriendos_api.model.Usuario;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface UsuarioMapper {
    Usuario findByEmail(@Param("email") String email);
    Usuario findById(@Param("id") Long id);
    int countByRol(@Param("rol") String rol);
    void insert(Usuario usuario);
}
