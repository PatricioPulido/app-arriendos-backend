package com.apparriendos.arriendos_api.mapper;

import com.apparriendos.arriendos_api.model.Departamento;
import com.apparriendos.arriendos_api.model.FiltroDepartamentos;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

@Mapper
public interface DepartamentoMapper {
    List<Departamento> findAll();
    List<Departamento> buscar(@Param("f") FiltroDepartamentos filtro);
    List<Departamento> findByAnfitrion(@Param("idAnfitrion") Long idAnfitrion);
    Departamento findById(@Param("id") Long id);
    void insert(Departamento departamento);
    void update(Departamento departamento);
    void delete(@Param("id") Long id);
}
