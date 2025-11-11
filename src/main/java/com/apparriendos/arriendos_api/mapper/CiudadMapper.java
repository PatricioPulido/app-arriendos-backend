package com.apparriendos.arriendos_api.mapper;

import com.apparriendos.arriendos_api.model.Ciudad;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface CiudadMapper {
    List<Ciudad> findAll();
    Ciudad findById(@Param("id") Long id);
    List<Ciudad> searchByNombre(@Param("nombre") String nombre);
    void insert(Ciudad ciudad);
    void update(Ciudad ciudad);
    void delete(@Param("id") Long id);
}
