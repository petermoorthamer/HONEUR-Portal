package com.jnj.honeur.portal;

import com.jnj.honeur.shiro.ShiroCasLogoutHandler;
import io.buji.pac4j.filter.CallbackFilter;
import io.buji.pac4j.filter.SecurityFilter;
import io.buji.pac4j.realm.Pac4jRealm;
import org.apache.shiro.authz.AuthorizationException;
import org.apache.shiro.cache.CacheManager;
import org.apache.shiro.cache.MemoryConstrainedCacheManager;
import org.apache.shiro.mgt.SecurityManager;
import org.apache.shiro.realm.Realm;
import org.apache.shiro.spring.config.web.autoconfigure.ShiroWebAutoConfiguration;
import org.apache.shiro.spring.web.ShiroFilterFactoryBean;
import org.apache.shiro.spring.web.config.AbstractShiroWebConfiguration;
import org.apache.shiro.spring.web.config.DefaultShiroFilterChainDefinition;
import org.apache.shiro.spring.web.config.ShiroFilterChainDefinition;
import org.apache.shiro.web.servlet.ShiroFilter;
import org.pac4j.cas.client.CasClient;
import org.pac4j.cas.config.CasConfiguration;
import org.pac4j.core.config.Config;
import org.pac4j.core.context.WebContext;
import org.pac4j.core.credentials.TokenCredentials;
import org.pac4j.core.credentials.authenticator.Authenticator;
import org.pac4j.core.exception.CredentialsException;
import org.pac4j.core.exception.HttpAction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;

import javax.servlet.Filter;
import java.security.Security;
import java.util.HashMap;
import java.util.Map;

//@Configuration
public class ShiroCasConfigurationNew extends ShiroWebAutoConfiguration {

    private static Logger log = LoggerFactory.getLogger(ShiroCasConfigurationNew.class);

    @ExceptionHandler(AuthorizationException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public String handleException(AuthorizationException e, Model model) {

        // you could return a 404 here instead (this is how github handles 403, so the user does NOT know there is a
        // resource at that location)
        log.warn("AuthorizationException was thrown", e);

        Map<String, Object> map = new HashMap<String, Object>();
        map.put("status", HttpStatus.FORBIDDEN.value());
        map.put("message", "No message available");
        model.addAttribute("errors", map);

        return "error";
    }


    @Bean
    public Realm realm() {
        /*return new AuthenticatingRealm() {
            @Override
            protected AuthenticationInfo doGetAuthenticationInfo(AuthenticationToken token) throws AuthenticationException {
                return new SimpleAuthenticationInfo(token.getPrincipal(), token.getCredentials(), "CasRealm");
            }
        };*/
        return new Pac4jRealm();
    }

    @Bean
    public ShiroFilterFactoryBean shiroFilterFactoryBean(SecurityManager securityManager) {
        ShiroFilterFactoryBean shiroFilter = new ShiroFilterFactoryBean();
        shiroFilter.setSecurityManager(securityManager);

        shiroFilter.getFilters().put("callbackFilter", callbackFilter());

        return shiroFilter;
    }

    @Bean
    public ShiroFilterChainDefinition shiroFilterChainDefinition() {
        DefaultShiroFilterChainDefinition chainDefinition = new DefaultShiroFilterChainDefinition();
        chainDefinition.addPathDefinition("/login.html", "authc"); // need to accept POSTs from the login form
        chainDefinition.addPathDefinition("/logout", "logout");
        chainDefinition.addPathDefinition("/callback?*", "callbackFilter");
        chainDefinition.addPathDefinition("/protected/**", "authc");
        chainDefinition.addPathDefinition("/public/**", "anon");
        chainDefinition.addPathDefinition("/**", "anon");


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
        casConfig.setRenew(false);
        return casConfig;
    }

    @Bean
    public CasClient casClient() {
        CasClient casClient = new CasClient();
        casClient.setName("CasClient");
        casClient.setIncludeClientNameInCallbackUrl(true);
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
        securityFilter.setClients("CasClient");
        return securityFilter;
    }

    @Bean
    public CallbackFilter callbackFilter() {
        CallbackFilter callbackFilter = new CallbackFilter();
        callbackFilter.setConfig(pack4jConfig());
        //callbackFilter.setMultiProfile(true);
        //callbackFilter.setDefaultUrl("/callback");
        return callbackFilter;
    }

    @Bean
    protected CacheManager cacheManager() {
        return new MemoryConstrainedCacheManager();
    }
}
