/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.digitalbarista.cat.security;

import java.io.Serializable;
import org.springframework.security.access.PermissionEvaluator;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

/**
 *
 * @author Falken
 */
@Component("CATPermissionEvaluator")
public class CATPermissionEvaluator implements PermissionEvaluator {

  public boolean hasPermission(Authentication authentication, Object targetDomainObject, Object permission) {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  public boolean hasPermission(Authentication authentication, Serializable targetId, String targetType, Object permission) {
    throw new UnsupportedOperationException("Not supported yet.");
  }
  
}
