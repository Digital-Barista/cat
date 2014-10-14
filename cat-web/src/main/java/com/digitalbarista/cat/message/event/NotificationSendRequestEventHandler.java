package com.digitalbarista.cat.message.event;

import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.jms.ConnectionFactory;
import javax.jms.Destination;
import javax.jms.MapMessage;
import javax.jms.MessageProducer;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.naming.InitialContext;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import com.digitalbarista.cat.audit.OutgoingMessageEntryDO;
import com.digitalbarista.cat.business.Connector;
import com.digitalbarista.cat.business.EntryData;
import com.digitalbarista.cat.business.Node;
import com.digitalbarista.cat.business.ResponseConnector;
import com.digitalbarista.cat.data.ConnectorType;
import com.digitalbarista.cat.data.EntryPointType;
import com.digitalbarista.cat.data.FacebookAppDO;
import com.digitalbarista.cat.data.FacebookMessageDO;
import com.digitalbarista.cat.ejb.session.CampaignManager;
import com.digitalbarista.cat.ejb.session.FacebookManager;
import javax.mail.Message;
import javax.mail.Session;
import javax.mail.Transport;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class NotificationSendRequestEventHandler implements CATEventHandler {
	
	private Logger log = LogManager.getLogger(NotificationSendRequestEventHandler.class);
        private String dateFormat = "MM/dd/yyyy";

  @Autowired
  private SessionFactory sf;
  
  @Autowired
  private FacebookManager fbMan;
  
	@Override
        @Transactional
	public void processEvent(CATEvent e) {
            try
            {
                    FacebookAppDO applicationInfo = (FacebookAppDO)sf.getCurrentSession().get(FacebookAppDO.class, e.getSource());
                    if(!applicationInfo.isSendNotifications()) return;
                    List<String> fbuids = new ArrayList<String>();
                    fbuids.add(e.getTarget());
                    fbMan.sendAppRequest(fbuids, applicationInfo.getAppName(), "A new message arrived - "+new SimpleDateFormat(dateFormat).format(new Date()));
                    fbMan.sendNotification(e.getTarget(), applicationInfo.getAppName(), "A new message has arrived!");
            }catch(Exception ex)
            {
                    throw new RuntimeException("Could not deliver the requested message!",ex);
            }
	}

}
