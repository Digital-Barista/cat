/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.digitalbarista.cat.message.event;

import com.digitalbarista.cat.data.EntryPointType;
import com.digitalbarista.cat.ejb.session.SubscriptionManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/**
 *
 * @author Falken
 */
@Component
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class UserSubscribedEventHandler implements CATEventHandler {

  @Autowired
  private SubscriptionManager sMan;
  
  @Override
  public void processEvent(CATEvent e) {
    switch(e.getSourceType())
    {
      case EmailEndpoint:
        sMan.subscribeToEntryPoint(e.getSource(), e.getTarget(), EntryPointType.Email);
        break;
      case SMSEndpoint:
        sMan.subscribeToEntryPoint(e.getSource(), e.getTarget(), EntryPointType.SMS);
        break;
      case TwitterEndpoint:
        sMan.subscribeToEntryPoint(e.getSource(), e.getTarget(), EntryPointType.Twitter);
        break;
      case FacebookEndpoint:
        sMan.subscribeToEntryPoint(e.getSource(), e.getTarget(), EntryPointType.Facebook);
        break;
      default:
        throw new IllegalArgumentException("Only valid entry types may subscribe users. type="+e.getSourceType()+" is not valid.");
    }
  }  
}
