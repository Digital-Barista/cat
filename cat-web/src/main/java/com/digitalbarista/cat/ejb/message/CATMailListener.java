package com.digitalbarista.cat.ejb.message;

import javax.annotation.security.RunAs;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.ObjectMessage;


import com.digitalbarista.cat.ejb.session.EventManager;
import com.digitalbarista.cat.mail.MessageSummary;
import com.digitalbarista.cat.message.event.CATEvent;
import java.util.ArrayList;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * Message-Driven Bean implementation class for: MailEcho
 *
 */
@Component("CATMailListener")
@RunAs("admin")
public class CATMailListener implements MessageListener{

  @Autowired
  private EventManager eventManager;
	
  @Transactional(propagation=Propagation.REQUIRED)
  public void onMessage(Message message) {
    try
    {
      List<GrantedAuthority> grantedAuths = new ArrayList<GrantedAuthority>();
      grantedAuths.add(new SimpleGrantedAuthority("ROLE_admin"));      

      TestingAuthenticationToken token = new TestingAuthenticationToken("JMS Listener","pw",grantedAuths);
      token.setAuthenticated(true);
      SecurityContextHolder.getContext().setAuthentication(token);

      ObjectMessage om = (ObjectMessage)message;
      MessageSummary ms = (MessageSummary)om.getObject();
      CATEvent e = CATEvent.buildIncomingEmailEvent(
          ms.getReplyTo()[0].toString(),
          ms.getRecipients()[0].toString(), 
          ms.getSubject());
      eventManager.queueEvent(e);
    }
    catch (Exception ex)
    {
      throw new RuntimeException(ex);
    }
    finally
    {
      SecurityContextHolder.getContext().setAuthentication(null);
    }
  }
}
