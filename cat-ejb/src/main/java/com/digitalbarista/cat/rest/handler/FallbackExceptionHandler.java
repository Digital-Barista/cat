/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.digitalbarista.cat.rest.handler;

import com.digitalbarista.cat.business.ErrorMessage;
import javax.ejb.EJBAccessException;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import org.apache.log4j.LogManager;

/**
 *
 * @author Falken
 */
public class FallbackExceptionHandler implements ExceptionMapper {

  public Response toResponse(Throwable exception) {
    ErrorMessage err;
    if((exception instanceof SecurityException) ||
       (exception instanceof EJBAccessException))
    {
      LogManager.getLogger(getClass()).error(exception);
      err = new ErrorMessage(null,"You are not allowed to perform the requested action.",exception.getMessage());
      return Response.status(Response.Status.FORBIDDEN).entity(err).build();
    }
    err = new ErrorMessage(null,"Something unexpected went wrong.",exception.getMessage());
    return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(err).build();
  }
  
}
