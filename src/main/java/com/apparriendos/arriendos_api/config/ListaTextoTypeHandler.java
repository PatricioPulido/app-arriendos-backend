package com.apparriendos.arriendos_api.config;

import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.MappedJdbcTypes;
import org.apache.ibatis.type.MappedTypes;

import java.sql.Array;
import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;

/**
 * Convierte una columna text[] de Postgres en List&lt;String&gt; de Java y viceversa.
 * Se usa para comodidades y destacados: ['wifi','piscina'] &lt;-&gt; List.of("wifi","piscina").
 * Una columna NULL se lee como lista vacia, asi el front nunca recibe null.
 */
@MappedTypes(List.class)
@MappedJdbcTypes(JdbcType.ARRAY)
public class ListaTextoTypeHandler extends BaseTypeHandler<List<String>> {

    @Override
    public void setNonNullParameter(PreparedStatement ps, int i, List<String> lista, JdbcType jdbcType)
            throws SQLException {
        ps.setArray(i, ps.getConnection().createArrayOf("text", lista.toArray()));
    }

    @Override
    public void setParameter(PreparedStatement ps, int i, List<String> lista, JdbcType jdbcType)
            throws SQLException {
        // null -> arreglo vacio, porque las columnas son NOT NULL DEFAULT '{}'
        setNonNullParameter(ps, i, lista == null ? List.of() : lista, jdbcType);
    }

    @Override
    public List<String> getNullableResult(ResultSet rs, String columna) throws SQLException {
        return aLista(rs.getArray(columna));
    }

    @Override
    public List<String> getNullableResult(ResultSet rs, int indice) throws SQLException {
        return aLista(rs.getArray(indice));
    }

    @Override
    public List<String> getNullableResult(CallableStatement cs, int indice) throws SQLException {
        return aLista(cs.getArray(indice));
    }

    private static List<String> aLista(Array arreglo) throws SQLException {
        if (arreglo == null) return List.of();
        Object[] valores = (Object[]) arreglo.getArray();
        return Arrays.stream(valores).map(String::valueOf).toList();
    }
}
