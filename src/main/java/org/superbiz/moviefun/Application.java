package org.superbiz.moviefun;

import com.mysql.jdbc.jdbc2.optional.MysqlDataSource;
import com.zaxxer.hikari.HikariDataSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.Database;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import javax.persistence.EntityManagerFactory;
import javax.sql.DataSource;

@SpringBootApplication(exclude = {
        DataSourceAutoConfiguration.class,
        HibernateJpaAutoConfiguration.class
})

public class Application {

    @Autowired
    DatabaseServiceCredentials databaseServiceCredentials;


    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

    @Bean
    public ServletRegistrationBean actionServletRegistration(ActionServlet actionServlet) {
        return new ServletRegistrationBean(actionServlet, "/moviefun/*");
    }

    @Bean
    public static DatabaseServiceCredentials getDatabaseServiceCredentials() {
        DatabaseServiceCredentials databaseServiceCredentials = new DatabaseServiceCredentials(new String (System.getenv("VCAP_SERVICES")));
        return databaseServiceCredentials;
    }


    @Bean(name="albums-database")
    public HikariDataSource albumsDataSource(DatabaseServiceCredentials serviceCredentials) {
        MysqlDataSource dataSource = new MysqlDataSource();
        dataSource.setURL(serviceCredentials.jdbcUrl("albums-mysql"));
        HikariDataSource hikariDataSource = new HikariDataSource();
        hikariDataSource.setDataSource(dataSource);
        return hikariDataSource;
    }

    @Bean(name="movies-database")
    public HikariDataSource moviesDataSource(DatabaseServiceCredentials serviceCredentials) {
        MysqlDataSource dataSource = new MysqlDataSource();
        dataSource.setURL(serviceCredentials.jdbcUrl("movies-mysql"));
        HikariDataSource hikariDataSource = new HikariDataSource();
        hikariDataSource.setDataSource(dataSource);
        return hikariDataSource;
    }

    @Bean
    public HibernateJpaVendorAdapter hibernateJpaVendorAdapter() {
        HibernateJpaVendorAdapter hibernateJpaVendorAdapter =  new HibernateJpaVendorAdapter();
        hibernateJpaVendorAdapter.setDatabase(Database.MYSQL);
        hibernateJpaVendorAdapter.setDatabasePlatform("org.hibernate.dialect.MySQL5Dialect");
        hibernateJpaVendorAdapter.setGenerateDdl(true);
        return hibernateJpaVendorAdapter;
        }

    @Bean
    @Primary
    public LocalContainerEntityManagerFactoryBean moviesEntity(@Qualifier("movies-database") DataSource dataSource, HibernateJpaVendorAdapter hibernateJpaVendorAdapter) {
        LocalContainerEntityManagerFactoryBean localContainerEntityManagerFactoryBean = new LocalContainerEntityManagerFactoryBean();
        localContainerEntityManagerFactoryBean.setDataSource(dataSource);
        localContainerEntityManagerFactoryBean.setJpaVendorAdapter(hibernateJpaVendorAdapter);
        localContainerEntityManagerFactoryBean.setPackagesToScan("org.superbiz.moviefun");
        localContainerEntityManagerFactoryBean.setPersistenceUnitName("movies");
        return localContainerEntityManagerFactoryBean;
    }

    @Bean
    public LocalContainerEntityManagerFactoryBean albumsEntity(@Qualifier("albums-database") DataSource dataSource, HibernateJpaVendorAdapter hibernateJpaVendorAdapter) {
        LocalContainerEntityManagerFactoryBean localContainerEntityManagerFactoryBean = new LocalContainerEntityManagerFactoryBean();
        localContainerEntityManagerFactoryBean.setDataSource(dataSource);
        localContainerEntityManagerFactoryBean.setJpaVendorAdapter(hibernateJpaVendorAdapter);
        localContainerEntityManagerFactoryBean.setPackagesToScan("org.superbiz.moviefun");
        localContainerEntityManagerFactoryBean.setPersistenceUnitName("albums");
        return localContainerEntityManagerFactoryBean;
    }

    @Bean
    public PlatformTransactionManager getMovieTransactionManager(EntityManagerFactory moviesEntity) {
        return new JpaTransactionManager(moviesEntity);
    }

    @Bean
    public PlatformTransactionManager getAlbumTransactionManager(EntityManagerFactory albumsEntity) {
        return new JpaTransactionManager(albumsEntity);
    }

    @Bean
    public TransactionTemplate transactionTemplateAlbum(PlatformTransactionManager getAlbumTransactionManager){
        return new TransactionTemplate(getAlbumTransactionManager);
    }

    @Bean
    public TransactionTemplate transactionTemplateMovie(PlatformTransactionManager getMovieTransactionManager){
        return new TransactionTemplate(getMovieTransactionManager);
    }
}
