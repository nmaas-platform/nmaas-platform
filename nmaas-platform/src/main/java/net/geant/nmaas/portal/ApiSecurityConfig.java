package net.geant.nmaas.portal;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.access.expression.method.DefaultMethodSecurityExpressionHandler;
import org.springframework.security.access.expression.method.MethodSecurityExpressionHandler;

import net.geant.nmaas.portal.api.security.ApiPermissionEvaluator;
import net.geant.nmaas.portal.service.impl.security.AclService;
import net.geant.nmaas.portal.service.impl.security.AppInstancePermissionCheck;
import net.geant.nmaas.portal.service.impl.security.AppTemplatePermissionCheck;
import net.geant.nmaas.portal.service.impl.security.CommentPermissionCheck;
import net.geant.nmaas.portal.service.impl.security.DomainObjectPermissionCheck;
import net.geant.nmaas.portal.service.impl.security.GenericPermissionCheck;

@Configuration
@ComponentScan(basePackages= {"net.geant.nmaas.portal.service.impl.security"})
public class ApiSecurityConfig {

	@Autowired(required=true)
	ApiPermissionEvaluator ApiPermissionEvaluator;
	
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
	public MethodSecurityExpressionHandler defaultMethodSecurityExpressionHandler() {
		DefaultMethodSecurityExpressionHandler dmseh = new DefaultMethodSecurityExpressionHandler();
		dmseh.setPermissionEvaluator(this.ApiPermissionEvaluator);
		return dmseh;
	}
	
}
