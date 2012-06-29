/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.digitalbarista.cat.message.event;

import com.digitalbarista.cat.data.EntryPointType;
import javax.ejb.SessionContext;
import javax.persistence.EntityManager;

/**
 *
 * @author Falken
 */
public class UserSubscribedEventHandler extends CATEventHandler {

	public UserSubscribedEventHandler(EntityManager newEM,
			SessionContext newSC) {
		super(newEM, newSC);
	}

  @Override
  public void processEvent(CATEvent e) {
    switch(e.getSourceType())
    {
      case EmailEndpoint:
        getSubscriptionManager().subscribeToEntryPoint(e.getSource(), e.getTarget(), EntryPointType.Email);
        break;
      case SMSEndpoint:
        getSubscriptionManager().subscribeToEntryPoint(e.getSource(), e.getTarget(), EntryPointType.SMS);
        break;
      case TwitterEndpoint:
        getSubscriptionManager().subscribeToEntryPoint(e.getSource(), e.getTarget(), EntryPointType.Twitter);
        break;
      case FacebookEndpoint:
        getSubscriptionManager().subscribeToEntryPoint(e.getSource(), e.getTarget(), EntryPointType.Facebook);
        break;
      default:
        throw new IllegalArgumentException("Only valid entry types may subscribe users. type="+e.getSourceType()+" is not valid.");
    }
  }  
}
