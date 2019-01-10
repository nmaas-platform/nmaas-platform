package net.geant.nmaas.portal;

import net.geant.nmaas.portal.api.security.ApiPermissionEvaluator;
import net.geant.nmaas.portal.service.impl.security.AclService;
import net.geant.nmaas.portal.service.impl.security.AppInstancePermissionCheck;
import net.geant.nmaas.portal.service.impl.security.AppTemplatePermissionCheck;
import net.geant.nmaas.portal.service.impl.security.CommentPermissionCheck;
import net.geant.nmaas.portal.service.impl.security.DomainObjectPermissionCheck;
import net.geant.nmaas.portal.service.impl.security.GenericPermissionCheck;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.access.expression.method.DefaultMethodSecurityExpressionHandler;
import org.springframework.security.access.expression.method.MethodSecurityExpressionHandler;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
@ComponentScan(basePackages= {"net.geant.nmaas.portal.service.impl.security"})
public class ApiSecurityConfig {

	@Bean
	public MethodSecurityExpressionHandler defaultMethodSecurityExpressionHandler(ApiPermissionEvaluator apiPermissionEvaluator) {
		DefaultMethodSecurityExpressionHandler dmseh = new DefaultMethodSecurityExpressionHandler();
		dmseh.setPermissionEvaluator(apiPermissionEvaluator);
		return dmseh;
	}

	@Bean
	public PasswordEncoder passwordEncoder(){
		return new BCryptPasswordEncoder();
	}
	
}
