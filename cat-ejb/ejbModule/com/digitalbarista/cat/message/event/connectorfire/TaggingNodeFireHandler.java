package com.digitalbarista.cat.message.event.connectorfire;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import javax.persistence.EntityManager;

import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.criterion.Restrictions;

import com.digitalbarista.cat.business.Connector;
import com.digitalbarista.cat.business.ContactTag;
import com.digitalbarista.cat.business.Node;
import com.digitalbarista.cat.business.TaggingNode;
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
	public void handle(EntityManager em, CampaignManager cMan, EventManager eMan, Connector conn, Node dest, Integer version, SubscriberDO s, CATEvent e) {
		TaggingNode tNode = (TaggingNode)dest;
		NodeDO simpleNode=cMan.getSimpleNode(tNode.getUid());
		List<ContactTagDO> tags = new ArrayList<ContactTagDO>();
		for(ContactTag cTag : tNode.getTags())
			tags.add(em.find(ContactTagDO.class, cTag.getContactTagId()));
		ContactDO con;
		Criteria crit = ((Session)em.getDelegate()).createCriteria(ContactDO.class);
		EntryPointType ept = s.getSubscriptions().get(simpleNode.getCampaign()).getLastHitEntryType();
		String address=null;
		String altID=null;
		switch(ept)
		{
			case Email:
				address=s.getEmail();
				break;
			case SMS:
				address=s.getPhoneNumber();
				break;
			case Twitter:
				address=s.getTwitterUsername();
				altID=s.getTwitterID();
				break;
			case Facebook:
				address=s.getFacebookID();
				break;
		}
		if(address!=null)
			crit.add(Restrictions.eq("address",address));
		else
			crit.add(Restrictions.eq("alternateId", altID));
		crit.add(Restrictions.eq("type", ept));
		crit.add(Restrictions.eq("client.id", simpleNode.getCampaign().getClient().getPrimaryKey()));
		con = (ContactDO)crit.uniqueResult();
		if(con==null)
		{
			con=new ContactDO();
			con.setAddress(address);
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
		s.getSubscriptions().get(simpleNode.getCampaign()).setLastHitNode(simpleNode);
		eMan.queueEvent(CATEvent.buildNodeOperationCompletedEvent(dest.getUid(), ""+s.getPrimaryKey()));

	}

}
