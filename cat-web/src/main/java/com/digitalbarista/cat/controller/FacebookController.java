/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.digitalbarista.cat.controller;

import com.digitalbarista.cat.business.FacebookMessage;
import com.digitalbarista.cat.ejb.session.FacebookManager;
import com.digitalbarista.cat.exception.FacebookManagerException;
import java.util.List;
import javax.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

/**
 *
 * @author Falken
 */
@Controller
@RequestMapping(value="/unsecure/rest/facebook",
                produces={"application/xml","application/json"},
                consumes={"application/x-www-form-urlencoded"})
public class FacebookController {
  
  @Autowired
  private FacebookManager facebookManager;
  
  @ExceptionHandler
  public Model handleException(Throwable t)
  {
    return null;
  }
  
  @RequestMapping(method={RequestMethod.POST},
                  value="/messages/list/{appName}")
  public List<FacebookMessage> getMessages(@PathVariable("appName") String appName,
                                           @RequestBody MultiValueMap<String,String> parameterMap,
                                           HttpServletRequest request) throws FacebookManagerException
  {
    return facebookManager.getMessages(appName, parameterMap.getFirst("uid"), parameterMap.getFirst("signedRequest"), request);
  }
  
  @RequestMapping(method=RequestMethod.POST,
                  value="/messages/{facebookMessageId}/{response}")
  public FacebookMessage respond(@PathVariable("facebookMessageId") Integer facebookMessageId, 
                                 @PathVariable("response") String response, 
                                 @RequestBody MultiValueMap<String,String> parameterMap,
                                 HttpServletRequest request) throws FacebookManagerException
  {
    return facebookManager.respond(facebookMessageId, response, parameterMap.getFirst("uid"), parameterMap.getFirst("signedRequest"), request);
  }
  
  @RequestMapping(method=RequestMethod.POST,
                  value="/messages/{facebookMessageId}")
  public void delete(@PathVariable("facebookMessageId") Integer facebookMessageId,
                     @RequestBody MultiValueMap<String,String> parameterMap,
                     HttpServletRequest request) throws FacebookManagerException
  {
    facebookManager.delete(facebookMessageId, parameterMap.getFirst("signedRequest"), request);
  }
  
  @RequestMapping(method=RequestMethod.PUT,
                  value="/messages/authorize/{appName}/{uid}")
  public void userAuthorizeApp(@PathVariable("appName") String appName, 
                               @PathVariable("uid") String uid)
  {
    facebookManager.userAuthorizeApp(appName, uid);
  }
  
  @RequestMapping(method=RequestMethod.POST,
                  value="/deauthorize/{appName}")
  public void userDeauthorizeApp(@PathVariable("appName") String appName, 
                                 @RequestParam("fb_sig_user") String uid)
  {
    facebookManager.userDeauthorizeApp(appName, uid);
  }
}
