package com.jnj.honeur.portal;

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authz.AuthorizationException;
import org.apache.shiro.subject.Subject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.support.SpringBootServletInitializer;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.util.HashMap;
import java.util.Map;

@Configuration
@ControllerAdvice
@SpringBootApplication
public class PortalApplication extends SpringBootServletInitializer {

	private static Logger log = LoggerFactory.getLogger(PortalApplication.class);

	@Override
	protected SpringApplicationBuilder configure(SpringApplicationBuilder application) {
		return application.sources(PortalApplication.class);
	}

    @ModelAttribute(name = "subject")
    public Subject subject() {
        return SecurityUtils.getSubject();
    }

    @ExceptionHandler(AuthorizationException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public String handleException(AuthorizationException e, Model model) {

        // you could return a 404 here instead (this is how github handles 403, so the user does NOT know there is a
        // resource at that location)
        log.debug("AuthorizationException was thrown", e);

        Map<String, Object> map = new HashMap<String, Object>();
        map.put("status", HttpStatus.FORBIDDEN.value());
        map.put("message", "No message available");
        model.addAttribute("errors", map);

        return "error";
    }

    /*

    @Bean
    public Pac4jRealm realm() {
        Pac4jRealm realm = new Pac4jRealm();
        realm.setCachingEnabled(true);
        return realm;
//        realm.setUserDefinitions("joe.coder=password,user\n" +
//                "jill.coder=password,admin");
//
//        realm.setRoleDefinitions("admin=read,write\n" +
//                "user=read");
//        realm.setCachingEnabled(true);
//        return realm;
    }



    @ExceptionHandler(AuthorizationException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public String handleException(AuthorizationException e, Model model) {

        // you could return a 404 here instead (this is how github handles 403, so the user does NOT know there is a
        // resource at that location)
        log.debug("AuthorizationException was thrown", e);

        Map<String, Object> map = new HashMap<String, Object>();
        map.put("status", HttpStatus.FORBIDDEN.value());
        map.put("message", "No message available");
        model.addAttribute("errors", map);

        return "error";
    }

    @Bean
    public ShiroFilterChainDefinition shiroFilterChainDefinition() {
        DefaultShiroFilterChainDefinition chainDefinition = new DefaultShiroFilterChainDefinition();
        chainDefinition.addPathDefinition("/login.html", "authc"); // need to accept POSTs from the login form
        chainDefinition.addPathDefinition("/logout", "logout");
        chainDefinition.addPathDefinition("/protected/**", "authc");
		chainDefinition.addPathDefinition("/public/**", "anon");


        chainDefinition.addPathDefinition("/public/**", "anon");
        chainDefinition.addPathDefinition("/static/**", "anon");
		//chainDefinition.addPathDefinition("/**", "anon");
        return chainDefinition;
	}

	@Bean
	public ShiroCasLogoutHandler casLogoutHandler() {
		return new ShiroCasLogoutHandler();
	}

	@Bean
	public CasConfiguration casConfiguration() {
		CasConfiguration casConfig = new CasConfiguration();
		casConfig.setLoginUrl("https://localhost:8443/cas/login");
		casConfig.setLogoutHandler(casLogoutHandler());
		return casConfig;
	}

	@Bean
	public CasClient casClient() {
		CasClient casClient = new CasClient();
		casClient.setConfiguration(casConfiguration());
		return casClient;
	}

	@Bean
	public String callBackUrl() {
		return "http://localhost:8081/callback";
	}

	@Bean
	public Config pack4jConfig() {
		return new Config(callBackUrl(), casClient());
	}

	@Bean
	public SecurityFilter casSecurityFilter() {
		SecurityFilter securityFilter = new SecurityFilter();
		securityFilter.setConfig(pack4jConfig());
		securityFilter.setClients("HoneurPortal");
		securityFilter.setAuthorizers("isAuthenticated");
		return securityFilter;
	}

	@Bean
	public SecurityManager securityManager() {
		DefaultSecurityManager securityManager = new DefaultSecurityManager(realm());
		return securityManager;
	}

	@Bean
	public RoleAdminAuthGenerator authGenerator() {
		return new RoleAdminAuthGenerator();
	}

	@Bean
	public Authenticator authenticator() {
    	return new Authenticator() {
			@Override
			public AuthenticationInfo authenticate(AuthenticationToken authenticationToken) throws AuthenticationException {
				return new AuthenticationInfo() {
					@Override
					public PrincipalCollection getPrincipals() {
						return new SimplePrincipalCollection(authenticationToken.getPrincipal(), realm().getName());
					}

					@Override
					public Object getCredentials() {
						return authenticationToken.getCredentials();
					}
				};
			}
		};
	}

	@Bean
	public Authorizer authorizer() {
    	return realm();
	}


	@Bean
    protected CacheManager cacheManager() {
        return new MemoryConstrainedCacheManager();
    }*/

	public static void main(String[] args) {
		SpringApplication.run(PortalApplication.class, args);
	}
}
