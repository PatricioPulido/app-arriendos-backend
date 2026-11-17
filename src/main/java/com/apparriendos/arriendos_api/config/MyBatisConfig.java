// com.apparriendos.arriendos_api.config.MyBatisConfig
package com.apparriendos.arriendos_api.config;

import javax.sql.DataSource;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.type.JdbcType;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.mybatis.spring.SqlSessionTemplate;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;

@Configuration
@MapperScan("com.apparriendos.arriendos_api.mapper")
public class MyBatisConfig {

  @Bean
  public SqlSessionFactory sqlSessionFactory(DataSource dataSource) throws Exception {
    SqlSessionFactoryBean fb = new SqlSessionFactoryBean();
    fb.setDataSource(dataSource);

    // Como este bean reemplaza al de Spring Boot, las propiedades mybatis.* no se aplican solas.
    org.apache.ibatis.session.Configuration config = new org.apache.ibatis.session.Configuration();
    config.setMapUnderscoreToCamelCase(true);
    // Oracle rechaza parametros null sin tipo (ORA-17004 "Invalid column type: 1111")
    config.setJdbcTypeForNull(JdbcType.NULL);
    fb.setConfiguration(config);
    fb.setMapperLocations(
        new PathMatchingResourcePatternResolver().getResources("classpath*:mapper/*.xml")
    );
    return fb.getObject();
  }

  @Bean
  public SqlSessionTemplate sqlSessionTemplate(SqlSessionFactory sqlSessionFactory) {
    return new SqlSessionTemplate(sqlSessionFactory);
  }
}
