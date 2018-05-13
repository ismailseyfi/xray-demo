package edu.jhu;

import com.amazonaws.xray.javax.servlet.AWSXRayServletFilter;
import com.zaxxer.hikari.HikariDataSource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.aws.jdbc.config.annotation.RdsInstanceConfigurer;
import org.springframework.cloud.aws.jdbc.datasource.TomcatJdbcDataSourceFactory;
import org.springframework.context.annotation.Bean;

import javax.servlet.Filter;

/**
 * Spring Boot main application configuration
 */
@SpringBootApplication
public class XrayDemoApplication {
	public static void main(String[] args) {
		SpringApplication.run(XrayDemoApplication.class, args);
	}

    /**
     * RdsInstanceConfigurer used by Spring Cloud AWS JDBC configuration.
     * @return
     */
    @Bean
    public RdsInstanceConfigurer instanceConfigurer() {
        return () -> {
            TomcatJdbcDataSourceFactory dataSourceFactory
                    = new TomcatJdbcDataSourceFactory();
            dataSourceFactory.setInitialSize(10);
            dataSourceFactory.setValidationQuery("SELECT 1");
            return dataSourceFactory;
        };
    }

    /**
     * Filter configuration. This allows AWS X-Ray to instrument incoming HTTP requests.
     *
     * @return
     */
    @Bean
    public Filter TracingFilter() {
        return new AWSXRayServletFilter(System.getenv("APP_NAME"));
    }

    /**
     * Custom Datasource configuration. Hikari DataSource does not support instrumentation. I ended up internally using
     * Tomcat JDBC datasource to instrument.
     *
     * Internally uses com.amazonaws.xray.sql.postgres.TracingInterceptor from AWS X-Ray.
     * @param url
     * @param username
     * @param driverClassName
     * @param postgresPassword
     * @return
     */
    @Bean
    public HikariDataSource geoExposureAppDataSource(
            @Value("${spring.datasource.url}") String url,
            @Value("${spring.datasource.username}") String username,
            @Value("${spring.database.driverClassName}") String driverClassName,
            @Value("${spring.datasource.password}") String postgresPassword) {

        org.apache.tomcat.jdbc.pool.DataSource dataSource = new org.apache.tomcat.jdbc.pool.DataSource();
        dataSource.setDriverClassName(driverClassName);
        dataSource.setUsername(username);
        dataSource.setUrl(url);
        dataSource.setJdbcInterceptors("com.amazonaws.xray.sql.postgres.TracingInterceptor");


        dataSource.setPassword(postgresPassword);

        HikariDataSource hikariDataSource = new HikariDataSource();
        hikariDataSource.setDataSource(dataSource);

        return hikariDataSource;
    }
}
