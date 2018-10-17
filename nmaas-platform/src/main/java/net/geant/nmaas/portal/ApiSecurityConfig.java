package net.geant.nmaas.portal;

import net.geant.nmaas.portal.api.security.ApiPermissionEvaluator;
import net.geant.nmaas.portal.service.impl.security.AclService;
import net.geant.nmaas.portal.service.impl.security.AppInstancePermissionCheck;
import net.geant.nmaas.portal.service.impl.security.AppTemplatePermissionCheck;
import net.geant.nmaas.portal.service.impl.security.CommentPermissionCheck;
import net.geant.nmaas.portal.service.impl.security.DomainObjectPermissionCheck;
import net.geant.nmaas.portal.service.impl.security.GenericPermissionCheck;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.access.expression.method.DefaultMethodSecurityExpressionHandler;
import org.springframework.security.access.expression.method.MethodSecurityExpressionHandler;

@Configuration
@ComponentScan(basePackages= {"net.geant.nmaas.portal.service.impl.security"})
public class ApiSecurityConfig {

	@Bean
	public AclService aclService() {
		AclService aclService = new AclService();
		
		aclService.add(domainObjectPermissionCheck());
		aclService.add(commentPermissionCheck());
		aclService.add(appInstancePermissionCheck());
		aclService.add(appTemplatePermissionCheck());
		
		aclService.setDefaultPermissionCheck(new GenericPermissionCheck());
		
		return aclService;
	}
	
	@Bean
	DomainObjectPermissionCheck domainObjectPermissionCheck() {
		return new DomainObjectPermissionCheck();
	}
	
	@Bean
	CommentPermissionCheck commentPermissionCheck() {
		return new CommentPermissionCheck();
	}
	
	@Bean
	AppInstancePermissionCheck appInstancePermissionCheck() {
		return new AppInstancePermissionCheck();
	}
	
	@Bean
	AppTemplatePermissionCheck appTemplatePermissionCheck() {
		return new AppTemplatePermissionCheck();
	}
	
	@Bean
	public MethodSecurityExpressionHandler defaultMethodSecurityExpressionHandler(ApiPermissionEvaluator apiPermissionEvaluator) {
		DefaultMethodSecurityExpressionHandler dmseh = new DefaultMethodSecurityExpressionHandler();
		dmseh.setPermissionEvaluator(apiPermissionEvaluator);
		return dmseh;
	}
	
}
