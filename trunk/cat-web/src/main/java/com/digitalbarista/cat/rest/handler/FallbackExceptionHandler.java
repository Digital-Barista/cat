/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.digitalbarista.cat.rest.handler;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.log4j.LogManager;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerExceptionResolver;
import org.springframework.web.servlet.ModelAndView;

/**
 *
 * @author Falken
 */
@Component
public class FallbackExceptionHandler implements HandlerExceptionResolver {

//  @ExceptionHandler
//  public Response toResponse(Throwable exception) {
//    ErrorMessage err;
//    if((exception instanceof SecurityException) ||
//       (exception instanceof EJBAccessException))
//    {
//      LogManager.getLogger(getClass()).error(exception);
//      err = new ErrorMessage(null,"You are not allowed to perform the requested action.",exception.getMessage());
//      return Response.status(Response.Status.FORBIDDEN).entity(err).build();
//    }
//    err = new ErrorMessage(null,"Something unexpected went wrong.",exception.getMessage());
//    return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(err).build();
//  }

  public ModelAndView resolveException(HttpServletRequest hsr, HttpServletResponse hsr1, Object o, Exception excptn) {
    LogManager.getLogger(getClass()).error(excptn);
    return null;
  }
  
}
