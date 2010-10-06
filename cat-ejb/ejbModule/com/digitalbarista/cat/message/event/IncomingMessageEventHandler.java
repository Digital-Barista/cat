package com.digitalbarista.cat.message.event;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

import javax.ejb.SessionContext;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.Query;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.criterion.Restrictions;

import com.digitalbarista.cat.audit.IncomingMessageEntryDO;
import com.digitalbarista.cat.audit.IncomingMessageEntryDO.KeywordMatchType;
import com.digitalbarista.cat.business.Campaign;
import com.digitalbarista.cat.business.Connector;
import com.digitalbarista.cat.business.Contact;
import com.digitalbarista.cat.business.EntryData;
import com.digitalbarista.cat.business.EntryNode;
import com.digitalbarista.cat.business.Node;
import com.digitalbarista.cat.business.ResponseConnector;
import com.digitalbarista.cat.data.CampaignDO;
import com.digitalbarista.cat.data.CampaignEntryPointDO;
import com.digitalbarista.cat.data.CampaignSubscriberLinkDO;
import com.digitalbarista.cat.data.ConnectorType;
import com.digitalbarista.cat.data.ContactDO;
import com.digitalbarista.cat.data.EntryPointType;
import com.digitalbarista.cat.data.GlobalUnsubscribeKeywords;
import com.digitalbarista.cat.data.NodeType;
import com.digitalbarista.cat.data.SubscriberBlacklistDO;
import com.digitalbarista.cat.data.SubscriberDO;
import com.digitalbarista.cat.ejb.session.CampaignManager;
import com.digitalbarista.cat.ejb.session.ContactManager;
import com.digitalbarista.cat.ejb.session.EventManager;
import com.digitalbarista.cat.ejb.session.EventTimerManager;

public class IncomingMessageEventHandler extends CATEventHandler {

	Logger log = LogManager.getLogger(getClass());
	
	IncomingMessageEventHandler(EntityManager em, 
			SessionContext ctx, 
			EventManager emi, 
			CampaignManager cmi,
			ContactManager contactMan,
			EventTimerManager timer)
	{
		super(em,ctx,emi,cmi,contactMan,timer);
	}
	
	@Override
	public void processEvent(CATEvent e) {
		Query q;
		String input=null;
		
		//First set up our audit message
		IncomingMessageEntryDO auditEntry = new IncomingMessageEntryDO();
		auditEntry.setDateReceived(new Date());
		auditEntry.setIncomingAddress(e.getTarget());
		
		//Next, let's extract the correct field for keyword parsing.
		if(e.getSourceType().equals(CATEventSource.EmailEndpoint))
		{
			input = e.getArgs().get("subject");
			auditEntry.setIncomingType(EntryPointType.Email);
		}
		else if (e.getSourceType().equals(CATEventSource.SMSEndpoint))
		{
			input = e.getArgs().get("message");
			auditEntry.setIncomingType(EntryPointType.SMS);
		}
		else if (e.getSourceType().equals(CATEventSource.TwitterEndpoint))
		{
			input = e.getArgs().get("message");
			auditEntry.setIncomingType(EntryPointType.Twitter);
		}
		else if (e.getSourceType().equals(CATEventSource.FacebookEndpoint))
		{
			input = e.getArgs().get("message");
			auditEntry.setIncomingType(EntryPointType.Facebook);
		}
		
		//Finish setting up the audit entry.  And save it.
		//  The only way this WON'T save is if we roll back the transaction.
		//  Even if we simply don't do anything, we should still have
		//  an audit entry.
		auditEntry.setSubjectOrMessage(input);
		auditEntry.setIncomingAddress(e.getSource());
		auditEntry.setReturnAddress(e.getTarget());
		getEntityManager().persist(auditEntry);
				
		//If they don't send a keyword, we can't match it.
		if(input==null || input.trim().length()==0)
		{
			log.warn("No input sent.  Cannot match keywords");
			return;
		}

		//Query our subscriber by the return address that was sent to us.
		switch(e.getSourceType())
		{
			case EmailEndpoint:
				q=getEntityManager().createNamedQuery("subscriber.by.email");
				break;
				
			case SMSEndpoint:
				q=getEntityManager().createNamedQuery("subscriber.by.phone");
				break;
			
			case TwitterEndpoint:
				q=getEntityManager().createNamedQuery("subscriber.by.twitter");
				break;
				
			case FacebookEndpoint:
				q=getEntityManager().createNamedQuery("subscriber.by.facebook");
				break;
				
			default:
				throw new IllegalArgumentException("Invalid endpoint type specified.");
		}
		q.setParameter("endpoint", e.getTarget());
		SubscriberDO sub=null;
		try
		{
			sub = (SubscriberDO)q.getSingleResult();
		}
		catch(NoResultException ex){}

		//If they aren't in the system, we'll have to create them.
		if(sub==null)
		{
			if(e.getSourceType()==CATEventSource.TwitterEndpoint)
			{
				Session session = (Session)getEntityManager().getDelegate();
				Criteria crit = session.createCriteria(SubscriberDO.class);
				crit.add(Restrictions.eq("twitterID", e.getArgs().get("twitterID")));
				sub = (SubscriberDO)crit.uniqueResult();
				if(sub!=null && sub.getTwitterUsername()==null)
					sub.setTwitterUsername(e.getTarget());
				
				crit = session.createCriteria(ContactDO.class);
				crit.add(Restrictions.eq("type", EntryPointType.Twitter));
				crit.add(Restrictions.eq("alternateId", e.getArgs().get("twitterID")));
				List<ContactDO> contactList = (List<ContactDO>)crit.list();
				for(ContactDO contact : contactList)
					contact.setAddress(e.getTarget());
			}
			
			if(e.getSourceType()==CATEventSource.FacebookEndpoint)
			{
				Session session = (Session)getEntityManager().getDelegate();
				Criteria crit = session.createCriteria(SubscriberDO.class);
				crit.add(Restrictions.eq("facebookID", e.getArgs().get("facebookID")));
				sub = (SubscriberDO)crit.uniqueResult();
				if(sub!=null && sub.getFacebookID()==null)
					sub.setFacebookID(e.getTarget());
				
				crit = session.createCriteria(ContactDO.class);
				crit.add(Restrictions.eq("type", EntryPointType.Facebook));
				crit.add(Restrictions.eq("alternateId", e.getArgs().get("facebookID")));
				List<ContactDO> contactList = (List<ContactDO>)crit.list();
				for(ContactDO contact : contactList)
					contact.setAddress(e.getTarget());
			}
		}
		
		if(sub==null)
		{
			sub = new SubscriberDO();
			switch(e.getSourceType())
			{
				case EmailEndpoint:
					sub.setEmail(e.getTarget());
					break;
					
				case SMSEndpoint:
					sub.setPhoneNumber(e.getTarget());
					break;
					
				case TwitterEndpoint:
					sub.setTwitterUsername(e.getTarget());
					sub.setTwitterID(e.getArgs().get("twitterID"));
					break;
					
				case FacebookEndpoint:
					sub.setFacebookID(e.getArgs().get("facebookID"));
					break;
			}
			getEntityManager().persist(sub);
		}

		//Now let's figure out what campaign they're trying to send to.
		//  Note that 'campaign.entry.points' includes 'keyword' connectors,
		//  as well as campaign start points.  Note that we're finding ALL 
		//  possible keywords for the given incoming address.
		q=getEntityManager().createNamedQuery("campaign.entry.points");
		switch(e.getSourceType())
		{
			case EmailEndpoint:
				q.setParameter("type", EntryPointType.Email);
				break;
				
			case SMSEndpoint:
				q.setParameter("type", EntryPointType.SMS);
				break;
				
			case TwitterEndpoint:
				q.setParameter("type", EntryPointType.Twitter);
				break;
				
			case FacebookEndpoint:
				q.setParameter("type", EntryPointType.Facebook);
				break;
		}
		q.setParameter("entryPoint", e.getSource());
		List<CampaignEntryPointDO> entries = (List<CampaignEntryPointDO>)q.getResultList();
		
		//First, we need to grab the keyword.  Note that we already know
		//  that there's at least one non-whitespace letter, because of
		//  the previous .trim() length check.
		String[] splitInput = input.split("\\s");
		String possibleGlobalStop=splitInput[0];
		
		//First, double-check that the keyword isn't an unsubscribe request.
		try
		{
			//If this isn't an unsubscribe keyword, we'll exception out and move on.
			GlobalUnsubscribeKeywords.valueOf(possibleGlobalStop.toUpperCase());
			
			//But, since it is, we'll unsubscribe from EVERY campaign that
			// has this incoming address.
			/*for(CampaignEntryPointDO entry : entries)
				getEntityManager().remove(sub.getSubscriptions().get(entry.getCampaign()));*/
			
			//And we'll create a blacklist entry.
			SubscriberBlacklistDO newBL = new SubscriberBlacklistDO();
			newBL.setIncomingAddress(e.getSource());
			switch(e.getSourceType())
			{
				case EmailEndpoint:
					newBL.setType(EntryPointType.Email);
					break;
					
				case SMSEndpoint:
					newBL.setType(EntryPointType.SMS);
					break;
					
				case TwitterEndpoint:
					newBL.setType(EntryPointType.Twitter);
					break;
					
				case FacebookEndpoint:
					newBL.setType(EntryPointType.Facebook);
					break;
			}
			newBL.setSubscriber(sub);
			getEntityManager().persist(newBL);
			
			//And do nothing else.
			return;
		}catch(IllegalArgumentException ex){}
		
		//Since we're not blacklisting or unsubscribing, check what the keyword does.
		// Evidently there aren't any valid keywords for this incoming address.  A fairly
		// rare situation, but one that should be addressed.
		if(entries==null || entries.size()==0)
		{
			log.info("Invalid entry point sent. type="+e.getSourceType()+" entryPoint="+e.getSource());
			return;
		}
		
		//Since we're matching potentially keyPHRASES, we'll have to put the phrase back together.
		String keyphrase="";
		for(String word : splitInput)
		{
			if(word==null)
				continue;
			if(keyphrase.length()>0)
				keyphrase+=" "+word;
			else
				keyphrase+=word;
		}
		keyphrase = keyphrase.toUpperCase();
		
		//Now we go through the entry points 'til we find the right keyword.
		CampaignEntryPointDO mostLikelyEntry=null;
		int longestMatch=0;
		for(CampaignEntryPointDO entry : entries)
		{
			if(keyphrase.startsWith(entry.getKeyword().toUpperCase()))
			{
				if(longestMatch<entry.getKeyword().length())
				{
					mostLikelyEntry = entry;
					longestMatch=entry.getKeyword().length();
				}
			}
		}

		//If we didn't find it, they sent us a bad keyword.
		if(mostLikelyEntry==null)
			return;
		
		boolean isSubscribed=false;
		CampaignSubscriberLinkDO csl=null;
		for(CampaignDO subCamp : sub.getSubscriptions().keySet())
		{
			if(subCamp.getUID().equalsIgnoreCase(mostLikelyEntry.getCampaign().getUID()))
			{
				isSubscribed=true;
				csl=sub.getSubscriptions().get(subCamp);
				break;
			}
		}
		
		//If they are not subscribed, we check for an entry point.
		if(!isSubscribed)
		{
			EntryNode en=null;
			Campaign camp = getCampaignManager().getLastPublishedCampaign(mostLikelyEntry.getCampaign().getUID());
			if(camp==null)
				return;
			
			//We know this is the right campaign, we just need to find the correct node.
			for(Node node : camp.getNodes())
			{
				if(node.getType().equals(NodeType.Entry))
				{
					for(EntryData data : ((EntryNode)node).getEntryData())
					{
						if(!data.getKeyword().equals(mostLikelyEntry.getKeyword())) continue;
						if(!data.getEntryPoint().equals(mostLikelyEntry.getEntryPoint())) continue;
						if(!data.getEntryType().equals(mostLikelyEntry.getType())) continue;
						en=(EntryNode)node;
					}
					if(en!=null) break;
				}
			}

			//This isn't a campaign start point keyword.  It's going to be a
			//  response connector, but since they aren't subscribed, we ignore it.
			if(en==null)
				return;
			
			//But otherwise . . . we found it, and here we go.
			//First . . . audit the match.
			auditEntry.setMatchedType(KeywordMatchType.Node);
			auditEntry.setMatchedUID(en.getUid());
			auditEntry.setMatchedVersion(camp.getCurrentVersion()-1);
			
			//Second . . . clear the blacklist.
			q = getEntityManager().createNamedQuery("blacklist.entry");
			q.setParameter("subID", sub.getPrimaryKey());
			q.setParameter("address", mostLikelyEntry.getEntryPoint());
			q.setParameter("type", mostLikelyEntry.getType());
			try
			{
				getEntityManager().remove(q.getSingleResult());
			}catch(NoResultException ex){}
			
			//Now . . . get that guy subscribed.
			csl = new CampaignSubscriberLinkDO();
			csl.setCampaign(mostLikelyEntry.getCampaign());
			csl.setSubscriber(sub);
			csl.setLastHitNode(getCampaignManager().getSimpleNode(en.getUid()));
			csl.setLastHitEntryType(mostLikelyEntry.getType());
			csl.setLastHitEntryPoint(mostLikelyEntry.getEntryPoint());
			getEntityManager().persist(csl);
			
			if(!getContactManager().contactExists(e.getTarget(), mostLikelyEntry.getType(), mostLikelyEntry.getCampaign().getClient().getPrimaryKey()))
			{
				Contact con = new Contact();
				con.setAddress(e.getTarget());
				con.setClientId(mostLikelyEntry.getCampaign().getClient().getPrimaryKey());
				con.setCreateDate(Calendar.getInstance());
				con.setType(mostLikelyEntry.getType());
				getContactManager().save(con);
			}
			
			CATEvent fireImmediateConnectorsEvent = CATEvent.buildNodeOperationCompletedEvent(en.getUid(), sub.getPrimaryKey().toString());
			getEventManager().queueEvent(fireImmediateConnectorsEvent);
		} else {
			//since they're subscribed, we're going to assume we're triggering a response
			// connector.  Otherwise we ignore it.
			csl.setLastHitEntryPoint(mostLikelyEntry.getEntryPoint());
			csl.setLastHitEntryType(mostLikelyEntry.getType());
			Integer publishedVersion = csl.getCampaign().getCurrentVersion()-1;
			Node node = getCampaignManager().getSpecificNodeVersion(csl.getLastHitNode().getUID(), publishedVersion);
			Connector conn;
			for(String connUID : node.getDownstreamConnections())
			{
				conn=getCampaignManager().getSpecificConnectorVersion(connUID, publishedVersion);
				
				if(conn.getType().equals(ConnectorType.Response))
				{
					ResponseConnector rConn = (ResponseConnector)conn;
					for(EntryData data : rConn.getEntryData())
					{
						if(!keyphrase.equalsIgnoreCase(data.getKeyword()))
							continue;
						switch(e.getSourceType())
						{
							case EmailEndpoint:
								if(!data.getEntryType().equals(EntryPointType.Email)) continue;
								break;
							case SMSEndpoint:
								if(!data.getEntryType().equals(EntryPointType.SMS)) continue;
								break;
							case TwitterEndpoint:
								if(!data.getEntryType().equals(EntryPointType.Twitter)) continue;
								break;
							case FacebookEndpoint:
								if(!data.getEntryType().equals(EntryPointType.Facebook)) continue;
								break;
							default:
								continue;
						}
						//Don't forget to audit this hit.
						auditEntry.setMatchedType(KeywordMatchType.Connector);
						auditEntry.setMatchedUID(rConn.getUid());
						auditEntry.setMatchedVersion(publishedVersion);
						
						CATEvent fireConnectorEvent = CATEvent.buildFireConnectorForSubscriberEvent(rConn.getUid(), sub.getPrimaryKey().toString());
						getEventManager().queueEvent(fireConnectorEvent);
						return;
					}
				}
			}
			//If we simply drop out of this loop, it didn't match.  And we thus ignore it.
		}
	}

}
