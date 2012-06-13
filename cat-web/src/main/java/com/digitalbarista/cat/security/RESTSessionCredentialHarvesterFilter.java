/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.digitalbarista.cat.security;

import java.io.IOException;
import javax.security.auth.callback.*;
import javax.security.auth.login.LoginContext;
import javax.security.auth.login.LoginException;
import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import org.jboss.security.SecurityAssociation;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.web.filter.GenericFilterBean;
import sun.security.acl.PrincipalImpl;

/**
 *
 * @author Falken
 */
public class RESTSessionCredentialHarvesterFilter extends GenericFilterBean {

  public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
    HttpServletRequest req = (HttpServletRequest)request;
    String callURI = req.getRequestURI();
    callURI=callURI.replace("/cat/rs/", "/rest/");
    
    RequestDispatcher rd = req.getRequestDispatcher(callURI);
    final SecurityContext ctx = SecurityContextHolder.getContext();
    if(ctx.getAuthentication()==null || ctx.getAuthentication().isAuthenticated()==false || ctx.getAuthentication().getPrincipal()==null)
    {
      rd.forward(request, response);
      return;
    }
    try {
      LoginContext lc = new LoginContext("campaign-admin",new CallbackHandler(){

        public void handle(Callback[] callbacks) throws IOException, UnsupportedCallbackException {
          for(Callback cb : callbacks)
          {
            if(cb instanceof NameCallback)
            {
              ((NameCallback)cb).setName(((User)ctx.getAuthentication().getPrincipal()).getUsername());
            }
            if(cb instanceof PasswordCallback)
            {
              ((PasswordCallback)cb).setPassword(((User)ctx.getAuthentication().getPrincipal()).getPassword().toCharArray());
            }
          }
        }
        
      });
      lc.login();
      SecurityAssociation.pushSubjectContext(lc.getSubject(),new PrincipalImpl(((User)ctx.getAuthentication().getPrincipal()).getUsername()), ((User)ctx.getAuthentication().getPrincipal()).getPassword());
      rd = req.getRequestDispatcher(callURI);
      rd.forward(req, response);
      lc.logout();
    } catch (LoginException ex) {
      ex.printStackTrace();
    }
  }
  
}
