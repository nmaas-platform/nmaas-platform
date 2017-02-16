package net.geant.nmaas.portal;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jdbc.DataSourceBuilder;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@Configuration
@EnableTransactionManagement
@EnableJpaRepositories(basePackages = {"net.geant.nmaas.portal.persistent.repositories"})
@PropertySource("classpath:db.properties")
@ComponentScan("net.geant.nmaas.portal.persistent.repositories")
public class PersistentConfig {
//	public final static String DRIVER="db.driver";
//	public final static String URL="db.url";
//	public final static String USERNAME="db.username";
//	public final static String PASSWORD="db.password";

	@Autowired
	private Environment env;
	
	@Bean
	@ConfigurationProperties("db")
	public DataSource dataSource() {
		return DataSourceBuilder.create().build();
	}
	
}
