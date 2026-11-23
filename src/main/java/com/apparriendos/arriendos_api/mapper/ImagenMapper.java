package com.apparriendos.arriendos_api.mapper;

import com.apparriendos.arriendos_api.model.Imagen;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface ImagenMapper {
    List<Imagen> findByDepartamento(@Param("idDepartamento") Long idDepartamento);
    Imagen findById(@Param("id") Long id);
    int maxOrden(@Param("idDepartamento") Long idDepartamento);
    void insert(Imagen imagen);
    void updateOrden(@Param("idDepartamento") Long idDepartamento, @Param("idImagen") Long idImagen,
                     @Param("orden") int orden);
    void delete(@Param("id") Long id);
}
