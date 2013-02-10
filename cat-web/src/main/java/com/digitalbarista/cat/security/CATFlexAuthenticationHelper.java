/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.digitalbarista.cat.security;

import flex.messaging.FlexContext;
import flex.messaging.security.LoginCommand;
import java.security.Principal;
import java.util.Collection;
import java.util.List;
import javax.servlet.ServletConfig;
import org.apache.log4j.Logger;
import org.springframework.context.ApplicationContext;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.context.support.WebApplicationContextUtils;

/**
 *
 * @author Falken
 */
public class CATFlexAuthenticationHelper implements LoginCommand {

  public void start(ServletConfig config) {
  }

  public void stop() {
  }

  public Principal doAuthentication(String username, Object credentials) {
    ApplicationContext appContext =WebApplicationContextUtils.getWebApplicationContext(FlexContext.getServletConfig().getServletContext());
    AuthenticationManager authManager = (AuthenticationManager) appContext.getBean("authenticationManager");
    UsernamePasswordAuthenticationToken usernamePasswordAuthenticationToken = new UsernamePasswordAuthenticationToken(username,credentials);
    Authentication authentication=null;
    try
    {
      authentication = authManager.authenticate(usernamePasswordAuthenticationToken);
    }catch(Exception ex)
    {
      Logger.getLogger(getClass()).debug("Authentication failed: ",ex);
      return null;
    }
    SecurityContextHolder.getContextHolderStrategy().getContext().setAuthentication(authentication);
    final String principalName=authentication.getName();
    return new Principal(){
      public String getName() {
        return principalName;
      }
    };
  }

  public boolean doAuthorization(Principal principal, List roles) {
    List<String> roleNames = (List<String>)roles;
    if(SecurityContextHolder.getContext()==null || SecurityContextHolder.getContext().getAuthentication()==null)
      return false;
    Collection<GrantedAuthority> authorities = (Collection<GrantedAuthority>)SecurityContextHolder.getContextHolderStrategy().getContext().getAuthentication().getAuthorities();
    for(String roleName : roleNames)
    {
      for(GrantedAuthority authority : authorities)
      {
        if(("ROLE_"+roleName).equals(authority.getAuthority()) || roleName.equals(authority.getAuthority()))
        {
          return true;
        }
      }
    }
    return false;
  }

  public boolean logout(Principal principal) {
    SecurityContextHolder.getContext().setAuthentication(null);
    return true;
  }
  
}
