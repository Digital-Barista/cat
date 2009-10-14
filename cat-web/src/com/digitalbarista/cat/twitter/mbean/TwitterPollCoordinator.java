package com.digitalbarista.cat.twitter.mbean;

import java.io.IOException;

import javax.management.InstanceAlreadyExistsException;
import javax.management.MBeanRegistrationException;
import javax.management.MBeanServer;
import javax.management.MalformedObjectNameException;
import javax.management.NotCompliantMBeanException;
import javax.management.ObjectName;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.UsernamePasswordCredentials;
import org.apache.commons.httpclient.auth.AuthScope;
import org.apache.commons.httpclient.methods.GetMethod;
import org.jboss.mx.util.MBeanServerLocator;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import com.digitalbarista.cat.business.EntryPointDefinition;
import com.digitalbarista.cat.data.EntryPointType;
import com.digitalbarista.cat.ejb.session.ClientManager;
import com.digitalbarista.cat.twitter.bindings.DirectMessage;
import com.digitalbarista.cat.twitter.bindings.DirectMessageCollection;
import com.digitalbarista.cat.twitter.bindings.Tweeter;


public class TwitterPollCoordinator implements TwitterPollCoordinatorMBean, ApplicationContextAware {

	private ClientManager clientManager;
	
	@Override
	public int checkMessages(String account) {
		int messageCount=0;
		try
		{
			InitialContext ic = new InitialContext();
			
//			EntryPointDefinition epd = clientManager.getEntryPointDefinition(EntryPointType.Twitter, account);
//			if(epd==null) return -1;
//
			JAXBContext context = JAXBContext.newInstance(DirectMessageCollection.class,DirectMessage.class,Tweeter.class);
			Unmarshaller decoder = context.createUnmarshaller();
			
			HttpClient client = new HttpClient();
			GetMethod get = new GetMethod("http://www.twitter.com/direct_messages.xml");
			client.getParams().setAuthenticationPreemptive(true);
//			client.getState().setCredentials(AuthScope.ANY, new UsernamePasswordCredentials(account,epd.getCredentials()));
			client.getState().setCredentials(AuthScope.ANY, new UsernamePasswordCredentials("Falken224","Twitter224"));
			
			client.executeMethod(get);
			DirectMessageCollection dmc = (DirectMessageCollection)decoder.unmarshal(get.getResponseBodyAsStream());
			return dmc.getDirectMessages().size();
		}catch(JAXBException e)
		{
			e.printStackTrace();
			return -1;
		} catch (NamingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (HttpException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return messageCount;
	}

	@Override
	public void sendMessage(String account, String to, String message) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void deleteMesage(int id) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setApplicationContext(ApplicationContext ctx)
			throws BeansException {
		try {
			MBeanServer server = MBeanServerLocator.locateJBoss();
			server.registerMBean(this, new ObjectName("dbi.config:service=TwitterPollCoordinator"));
		} catch (InstanceAlreadyExistsException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (MBeanRegistrationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NotCompliantMBeanException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (MalformedObjectNameException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NullPointerException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void setClientManager(ClientManager clientManager) {
		this.clientManager = clientManager;
	}
	
}
