package com.apparriendos.arriendos_api.mapper;

import com.apparriendos.arriendos_api.model.Arrendatario;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface ArrendatarioMapper {
    List<Arrendatario> findAll();
    Arrendatario findById(@Param("id") Long id);
    void insert(Arrendatario arrendatario);
    void update(Arrendatario arrendatario);
    void delete(@Param("id") Long id);
}
