package net.geant.nmaas.portal;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.access.expression.method.DefaultMethodSecurityExpressionHandler;
import org.springframework.security.access.expression.method.MethodSecurityExpressionHandler;

import net.geant.nmaas.portal.api.security.ApiPermissionEvaluator;

@Configuration
public class ApiSecurityConfig {

	@Bean
	MethodSecurityExpressionHandler defaultMethodSecurityExpressionHandler() {
		DefaultMethodSecurityExpressionHandler dmseh = new DefaultMethodSecurityExpressionHandler();
		dmseh.setPermissionEvaluator(new ApiPermissionEvaluator());
		return dmseh;
	}
	
}
