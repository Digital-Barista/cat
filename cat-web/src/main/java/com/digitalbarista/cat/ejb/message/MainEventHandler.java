package com.digitalbarista.cat.ejb.message;


import javax.annotation.security.RunAs;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.ObjectMessage;


import com.digitalbarista.cat.message.event.CATEvent;
import com.digitalbarista.cat.message.event.CATEventHandlerFactory;
import java.util.ArrayList;
import java.util.List;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * Message-Driven Bean implementation class for: MainEventHandler
 *
 */
@Component("MainEventHandler")
@RunAs("admin")
public class MainEventHandler implements MessageListener {

  @Autowired
  private SessionFactory sf;
  
  @Autowired
  private CATEventHandlerFactory handlerFactory;
	
  @Transactional(propagation=Propagation.REQUIRED)
  public void onMessage(Message message) {
    ObjectMessage om = (ObjectMessage)message;
    try {
      List<GrantedAuthority> grantedAuths = new ArrayList<GrantedAuthority>();
      grantedAuths.add(new SimpleGrantedAuthority("ROLE_admin"));      

      TestingAuthenticationToken token = new TestingAuthenticationToken("JMS Listener","pw",grantedAuths);
      token.setAuthenticated(true);
      SecurityContextHolder.getContext().setAuthentication(token);
      
      CATEvent e = (CATEvent)om.getObject();
      if(om.propertyExists("JMS_JBOSS_SCHEDULED_DELIVERY"))
      {
        e.getArgs().put("scheduledDate", ""+om.getLongProperty("JMS_JBOSS_SCHEDULED_DELIVERY"));
      }
      handlerFactory.processEvent(e);
    } catch (Exception ex) {
      throw new RuntimeException(ex);
    }
    finally
    {
      SecurityContextHolder.getContext().setAuthentication(null);
    }
  }

}
