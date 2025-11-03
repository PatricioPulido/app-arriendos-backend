package com.apparriendos.arriendos_api.mapper;

import com.apparriendos.arriendos_api.model.Zona;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface ZonaMapper {
    List<Zona> findAll();
    List<Zona> findByCiudad(@Param("ciudadId") Long ciudadId);
    Zona findById(@Param("id") Long id);
    void insert(Zona zona);
    void update(Zona zona);
    void delete(@Param("id") Long id);
}
