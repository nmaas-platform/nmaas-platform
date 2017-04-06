package net.geant.nmaas.portal;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.access.expression.method.DefaultMethodSecurityExpressionHandler;
import org.springframework.security.access.expression.method.MethodSecurityExpressionHandler;

import net.geant.nmaas.portal.api.security.ApiPermissionEvaluator;

@Configuration
public class ApiSecurityConfig {

	@Autowired(required=true)
	ApiPermissionEvaluator ApiPermissionEvaluator;
	
	@Bean
	public MethodSecurityExpressionHandler defaultMethodSecurityExpressionHandler() {
		DefaultMethodSecurityExpressionHandler dmseh = new DefaultMethodSecurityExpressionHandler();
		dmseh.setPermissionEvaluator(this.ApiPermissionEvaluator);
		return dmseh;
	}
	
}
