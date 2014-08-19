package com.digitalbarista.cat.message.event.connectorfire;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import javax.persistence.NoResultException;

import com.digitalbarista.cat.business.Connector;
import com.digitalbarista.cat.business.ContactTag;
import com.digitalbarista.cat.business.Node;
import com.digitalbarista.cat.business.TaggingNode;
import com.digitalbarista.cat.data.CampaignDO;
import com.digitalbarista.cat.data.CampaignSubscriberLinkDO;
import com.digitalbarista.cat.data.ContactDO;
import com.digitalbarista.cat.data.ContactTagDO;
import com.digitalbarista.cat.data.ContactTagLinkDO;
import com.digitalbarista.cat.data.EntryPointType;
import com.digitalbarista.cat.data.NodeDO;
import com.digitalbarista.cat.data.SubscriberDO;
import com.digitalbarista.cat.ejb.session.CampaignManager;
import com.digitalbarista.cat.ejb.session.EventManager;
import com.digitalbarista.cat.message.event.CATEvent;
import org.hibernate.LockOptions;
import org.hibernate.Query;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Component
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class TaggingNodeFireHandler implements ConnectorFireHandler {

  @Autowired
  private CampaignManager cMan;
  
  @Autowired
  private EventManager eMan;

  @Autowired
  private SessionFactory sf;
  
	@Override
	public void handle(Connector conn, Node dest, Integer version, SubscriberDO s, CATEvent e) {
		TaggingNode tNode = (TaggingNode)dest;
		NodeDO simpleNode=cMan.getSimpleNode(tNode.getUid());
		List<ContactTagDO> tags = new ArrayList<ContactTagDO>();
		for(ContactTag cTag : tNode.getTags())
			tags.add((ContactTagDO)sf.getCurrentSession().get(ContactTagDO.class, cTag.getContactTagId()));
		ContactDO con;
		CampaignSubscriberLinkDO csl=null;
		for(CampaignDO subCamp : s.getSubscriptions().keySet())
		{
			if(subCamp.getUID().equalsIgnoreCase(simpleNode.getCampaign().getUID()))
			{
				csl=s.getSubscriptions().get(subCamp);
				break;
			}
		}
		sf.getCurrentSession().buildLockRequest(LockOptions.UPGRADE).lock(csl);
		sf.getCurrentSession().refresh(csl);
		EntryPointType ept = csl.getLastHitEntryType();
		Query q = sf.getCurrentSession().getNamedQuery("contact.by.address.and.client");
		q.setParameter("address",s.getAddress());
		q.setParameter("type", ept);
		q.setParameter("clientId", simpleNode.getCampaign().getClient().getPrimaryKey());
		try
		{
			con = (ContactDO)q.uniqueResult();
		}catch(NoResultException nre)
		{
			con=null;
		}
		if(con==null)
		{
			con=new ContactDO();
			con.setAddress(s.getAddress());
			con.setType(ept);
			con.setCreateDate(Calendar.getInstance());
			sf.getCurrentSession().persist(con);
		}
		Date newDate = new Date();
		for(ContactTagDO tag : tags)
		{
			if (con.findLink(tag)==null)
			{
				ContactTagLinkDO ctldo = new ContactTagLinkDO();
				ctldo.setContact(con);
				ctldo.setTag(tag);
				ctldo.setInitialTagDate(newDate);
				con.getContactTags().add(ctldo);
				sf.getCurrentSession().persist(ctldo);
			}
		}
		csl.setLastHitNode(simpleNode);
		eMan.queueEvent(CATEvent.buildNodeOperationCompletedEvent(dest.getUid(), ""+s.getPrimaryKey()));

	}

}
