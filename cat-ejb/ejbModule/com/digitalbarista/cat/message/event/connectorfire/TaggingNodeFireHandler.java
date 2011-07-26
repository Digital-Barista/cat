package com.digitalbarista.cat.message.event.connectorfire;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import javax.ejb.SessionContext;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.Query;

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

public class TaggingNodeFireHandler extends ConnectorFireHandler {

	@Override
	public void handle(EntityManager em, SessionContext ctx, Connector conn, Node dest, Integer version, SubscriberDO s, CATEvent e) {

		CampaignManager cMan = (CampaignManager)ctx.lookup("ejb/cat/CampaignManager");
		EventManager eMan = (EventManager)ctx.lookup("ejb/cat/EventManager");

		TaggingNode tNode = (TaggingNode)dest;
		NodeDO simpleNode=cMan.getSimpleNode(tNode.getUid());
		List<ContactTagDO> tags = new ArrayList<ContactTagDO>();
		for(ContactTag cTag : tNode.getTags())
			tags.add(em.find(ContactTagDO.class, cTag.getContactTagId()));
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
		EntryPointType ept = csl.getLastHitEntryType();
		Query q = em.createNamedQuery("contact.by.address.and.client");
		q.setParameter("address",s.getAddress());
		q.setParameter("type", ept);
		q.setParameter("clientId", simpleNode.getCampaign().getClient().getPrimaryKey());
		try
		{
			con = (ContactDO)q.getSingleResult();
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
			em.persist(con);
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
				em.persist(ctldo);
			}
		}
		csl.setLastHitNode(simpleNode);
		eMan.queueEvent(CATEvent.buildNodeOperationCompletedEvent(dest.getUid(), ""+s.getPrimaryKey()));

	}

}
