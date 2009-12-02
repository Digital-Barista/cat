package com.digitalbarista.cat.message.event;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import javax.ejb.SessionContext;
import javax.persistence.EntityManager;
import javax.persistence.LockModeType;
import javax.persistence.Query;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.criterion.Restrictions;
import org.jboss.util.NotImplementedException;

import com.digitalbarista.cat.business.Connector;
import com.digitalbarista.cat.business.ContactTag;
import com.digitalbarista.cat.business.CouponNode;
import com.digitalbarista.cat.business.MessageNode;
import com.digitalbarista.cat.business.Node;
import com.digitalbarista.cat.business.TaggingNode;
import com.digitalbarista.cat.data.CampaignDO;
import com.digitalbarista.cat.data.ContactDO;
import com.digitalbarista.cat.data.ContactTagDO;
import com.digitalbarista.cat.data.CouponCounterDO;
import com.digitalbarista.cat.data.CouponOfferDO;
import com.digitalbarista.cat.data.CouponResponseDO;
import com.digitalbarista.cat.data.EntryPointType;
import com.digitalbarista.cat.data.NodeDO;
import com.digitalbarista.cat.data.SubscriberDO;
import com.digitalbarista.cat.data.CouponResponseDO.Type;
import com.digitalbarista.cat.ejb.session.CampaignManager;
import com.digitalbarista.cat.ejb.session.ContactManager;
import com.digitalbarista.cat.ejb.session.EventManager;
import com.digitalbarista.cat.ejb.session.EventTimerManager;
import com.digitalbarista.cat.util.SequentialBitShuffler;

public class ConnectorFiredEventHandler extends CATEventHandler {

	private Logger log = LogManager.getLogger(ConnectorFiredEventHandler.class);
	
	public ConnectorFiredEventHandler(EntityManager newEM,
			SessionContext newSC, 
			EventManager newEventManager,
			CampaignManager newCampaignManager,
			ContactManager newContactManager,
			EventTimerManager timer) {
		super(newEM, newSC, newEventManager, newCampaignManager, newContactManager, timer);
	}

	@Override
	public void processEvent(CATEvent e) {
		Integer version = getCampaignManager().getSimpleConnector(e.getSource()).getCampaign().getCurrentVersion()-1;
		Connector conn = getCampaignManager().getSpecificConnectorVersion(e.getSource(), version);
		if(conn.getDestinationUID()==null)
			throw new IllegalStateException("ConnectorDO has no destination node. connector uid="+conn.getUid());
		Node dest = getCampaignManager().getSpecificNodeVersion(conn.getDestinationUID(), version);
		
		//validate fired connection
		if(e.getTargetType().equals(CATTargetType.SpecificSubscriber))
		{
			SubscriberDO s = getEntityManager().find(SubscriberDO.class, new Long(e.getTarget()));
			CampaignDO camp = getCampaignManager().getSimpleCampaign(conn.getCampaignUID());
			if(s==null || !s.getSubscriptions().containsKey(camp))
			{
				log.warn("subscriber pk="+e.getTarget()+" was not subscribed to campaign UID="+conn.getCampaignUID()+".  ConnectorDO "+conn.getUid()+" will not be fired.");
				return;
			}
			Node source = getCampaignManager().getSpecificNodeVersion(conn.getSourceNodeUID(), version);
			String subscriberCurrent=s.getSubscriptions().get(camp).getLastHitNode().getUID();
			if(source==null || !source.getUid().equals(subscriberCurrent))
			{
				log.warn("subscriber pk="+e.getTarget()+" is not on node uid="+source.getUid()+".  ConnectorDO "+conn.getUid()+" will not be fired.");
				return;
			}
		}
		
		//At this point, fired connection should be valid, so do it.
		switch(dest.getType())
		{
			case Entry:
			case OutgoingEntry:
				throw new IllegalStateException("ConnectorDO cannot have a entry node for a destination.");

			case Termination:
				throw new NotImplementedException("Haven't built this type of node yet.");
			
			case Tagging:
			{
				TaggingNode tNode = (TaggingNode)dest;
				NodeDO simpleNode=getCampaignManager().getSimpleNode(tNode.getUid());
				List<ContactTagDO> tags = new ArrayList<ContactTagDO>();
				for(ContactTag cTag : tNode.getTags())
					tags.add(getEntityManager().find(ContactTagDO.class, cTag.getContactTagId()));
				if(e.getTargetType().equals(CATTargetType.SpecificSubscriber))
				{
					SubscriberDO s = getEntityManager().find(SubscriberDO.class, new Long(e.getTarget()));
					ContactDO con;
					Criteria crit = ((Session)this.getEntityManager().getDelegate()).createCriteria(ContactDO.class);
					EntryPointType ept = s.getSubscriptions().get(simpleNode.getCampaign()).getLastHitEntryType();
					String address=null;
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
							break;
					}
					crit.add(Restrictions.eq("address",address));
					crit.add(Restrictions.eq("type", ept));
					crit.add(Restrictions.eq("client.id", simpleNode.getCampaign().getClient().getPrimaryKey()));
					con = (ContactDO)crit.uniqueResult();
					if(con==null)
					{
						con=new ContactDO();
						con.setAddress(address);
						con.setType(ept);
						con.setCreateDate(Calendar.getInstance());
						getEntityManager().persist(con);
					}
					con.getContactTags().addAll(tags);
					s.getSubscriptions().get(simpleNode.getCampaign()).setLastHitNode(simpleNode);
					getEventManager().queueEvent(CATEvent.buildNodeOperationCompletedEvent(dest.getUid(), ""+s.getPrimaryKey()));
				} else if(e.getTargetType().equals(CATTargetType.AllAppliedSubscribers)){
					Query q = getEntityManager().createNamedQuery("all.subscribers.on.node");
					q.setParameter("nodeUID", conn.getSourceNodeUID());
					List<SubscriberDO> subs = (List<SubscriberDO>)q.getResultList();
					for(SubscriberDO s : subs)
					{
						ContactDO con;
						Criteria crit = ((Session)this.getEntityManager().getDelegate()).createCriteria(ContactDO.class);
						EntryPointType ept = s.getSubscriptions().get(simpleNode.getCampaign()).getLastHitEntryType();
						String address=null;
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
								break;
						}
						crit.add(Restrictions.eq("address",address));
						crit.add(Restrictions.eq("entryPointType", ept));
						crit.add(Restrictions.eq("client.id", simpleNode.getCampaign().getClient().getPrimaryKey()));
						con = (ContactDO)crit.uniqueResult();
						if(con==null)
						{
							con=new ContactDO();
							con.setAddress(address);
							con.setType(ept);
							con.setCreateDate(Calendar.getInstance());
							getEntityManager().persist(con);
						}
						con.getContactTags().addAll(tags);
						s.getSubscriptions().get(simpleNode.getCampaign()).setLastHitNode(simpleNode);
						getEventManager().queueEvent(CATEvent.buildNodeOperationCompletedEvent(dest.getUid(), ""+s.getPrimaryKey()));
					}
				}
			}
			break; //End Tagging node type.
				
			case Message:
			{
				MessageNode mNode = (MessageNode)dest;
				CATEvent sendMessageEvent=null;
				NodeDO simpleNode=getCampaignManager().getSimpleNode(mNode.getUid());
				if(e.getTargetType().equals(CATTargetType.SpecificSubscriber))
				{
					SubscriberDO s = getEntityManager().find(SubscriberDO.class, new Long(e.getTarget()));
					String actualMessage = mNode.getMessage();
					String campaignAddIn = simpleNode.getCampaign().getAddInMessage();
					String clientAddIn = simpleNode.getCampaign().getClient().getUserAddInMessage();
					String adminAddIn = simpleNode.getCampaign().getClient().getAdminAddInMessage();
					
					if(campaignAddIn!=null && campaignAddIn.trim().length()>0)
						actualMessage+=campaignAddIn;
					else if(clientAddIn!=null && clientAddIn.trim().length()>0)
						actualMessage+=clientAddIn;
					
					if(adminAddIn!=null && adminAddIn.trim().length()>0)
						actualMessage+=adminAddIn;
					
					String fromAddress = s.getSubscriptions().get(simpleNode.getCampaign()).getLastHitEntryPoint();
					EntryPointType fromType = s.getSubscriptions().get(simpleNode.getCampaign()).getLastHitEntryType();
					switch(fromType)
					{
						
						case Email:
							sendMessageEvent = CATEvent.buildSendMessageRequestedEvent(fromAddress, fromType, s.getEmail(), actualMessage, mNode.getName(),mNode.getUid(),version);
							break;
						
						case SMS:
							sendMessageEvent = CATEvent.buildSendMessageRequestedEvent(fromAddress, fromType, s.getPhoneNumber(), actualMessage, mNode.getName(),mNode.getUid(),version);
							break;
							
						case Twitter:
							sendMessageEvent = CATEvent.buildSendMessageRequestedEvent(fromAddress, fromType, s.getTwitterUsername(), actualMessage, mNode.getName(),mNode.getUid(),version);
							break;
							
						default:
							throw new IllegalStateException("NodeDO must be either Email or SMS . . . mixed or other types are not supported.");
					}
					s.getSubscriptions().get(simpleNode.getCampaign()).setLastHitNode(simpleNode);
					getEventManager().queueEvent(sendMessageEvent);
					getEventManager().queueEvent(CATEvent.buildNodeOperationCompletedEvent(dest.getUid(), ""+s.getPrimaryKey()));
				} else if(e.getTargetType().equals(CATTargetType.AllAppliedSubscribers)){
					Query q = getEntityManager().createNamedQuery("all.subscribers.on.node");
					q.setParameter("nodeUID", conn.getSourceNodeUID());
					List<SubscriberDO> subs = (List<SubscriberDO>)q.getResultList();
					for(SubscriberDO s : subs)
					{
						String actualMessage = mNode.getMessage();
						String campaignAddIn = simpleNode.getCampaign().getAddInMessage();
						String clientAddIn = simpleNode.getCampaign().getClient().getUserAddInMessage();
						String adminAddIn = simpleNode.getCampaign().getClient().getAdminAddInMessage();
						
						if(campaignAddIn!=null && campaignAddIn.trim().length()>0)
							actualMessage+=campaignAddIn;
						else if(clientAddIn!=null && clientAddIn.trim().length()>0)
							actualMessage+=clientAddIn;
						
						if(adminAddIn!=null && adminAddIn.trim().length()>0)
							actualMessage+=adminAddIn;
						
						String fromAddress = s.getSubscriptions().get(simpleNode.getCampaign()).getLastHitEntryPoint();
						EntryPointType fromType = s.getSubscriptions().get(simpleNode.getCampaign()).getLastHitEntryType();
						switch(fromType)
						{
							case Email:
								sendMessageEvent = CATEvent.buildSendMessageRequestedEvent(fromAddress, fromType, s.getEmail(), actualMessage, mNode.getName(), mNode.getUid(), version);
								break;
							
							case SMS:
								sendMessageEvent = CATEvent.buildSendMessageRequestedEvent(fromAddress, fromType, s.getPhoneNumber(), actualMessage, mNode.getName(), mNode.getUid(), version);
						
							case Twitter:
								sendMessageEvent = CATEvent.buildSendMessageRequestedEvent(fromAddress, fromType, s.getTwitterUsername(), actualMessage, mNode.getName(), mNode.getUid(), version);
						
							default:
								throw new IllegalStateException("NodeDO must be either Email or SMS . . . mixed or other types are not supported.");
						}
						s.getSubscriptions().get(simpleNode.getCampaign()).setLastHitNode(simpleNode);
						getEntityManager().persist(simpleNode);
						getEventManager().queueEvent(sendMessageEvent);
						getEventManager().queueEvent(CATEvent.buildNodeOperationCompletedEvent(dest.getUid(), ""+s.getPrimaryKey()));
					}
				}
			}
			break; //End Message node type
			
			case Coupon:
			{
				CouponNode cNode = (CouponNode)dest;
				CATEvent sendMessageEvent=null;
				NodeDO simpleNode=getCampaignManager().getSimpleNode(cNode.getUid());
				if(e.getTargetType().equals(CATTargetType.SpecificSubscriber))
				{
					SubscriberDO s = getEntityManager().find(SubscriberDO.class, new Long(e.getTarget()));
					Date now = new Date();
					String actualMessage;

					CouponOfferDO offer = getEntityManager().find(CouponOfferDO.class, cNode.getCouponId());
					CouponResponseDO response;
					
					if((cNode.getUnavailableDate()==null || now.before(cNode.getUnavailableDate())) && (offer.getMaxCoupons()<0 || offer.getIssuedCouponCount()<offer.getMaxCoupons()))
					{
						String couponCode=null;
						
						if(cNode.getCouponCode()!=null)
						{
							couponCode=cNode.getCouponCode();
						} else {
							//Get the counter for 6-digit coupon codes.  This may need to change in the future.
							int COUPON_CODE_LENGTH=6;
							CouponCounterDO counter = getEntityManager().find(CouponCounterDO.class, COUPON_CODE_LENGTH);
							if(counter==null)
							{
								counter = new CouponCounterDO();
								counter.setCouponCodeLength(COUPON_CODE_LENGTH);
								counter.setNextNumber(1l);
								counter.setBitScramble(SequentialBitShuffler.generateBitShuffle(COUPON_CODE_LENGTH));
								getEntityManager().persist(counter);
								counter = getEntityManager().find(CouponCounterDO.class, COUPON_CODE_LENGTH);
							}
							getEntityManager().lock(counter, LockModeType.READ);
							SequentialBitShuffler shuffler = new SequentialBitShuffler(counter.getBitScramble(),COUPON_CODE_LENGTH);
							couponCode = shuffler.generateCode(counter.getNextNumber());
							counter.setNextNumber(counter.getNextNumber()+1);							
						}
						actualMessage = cNode.getAvailableMessage();
						int startPos = actualMessage.indexOf('{');
						int endPos = actualMessage.indexOf('}',-1)+1;
						if(startPos==-1 || endPos==-1 || endPos<=startPos)
							throw new IllegalArgumentException("Cannot insert coupon code, since braces are not inserted properly.");
						actualMessage = actualMessage.substring(0,startPos) + couponCode + ((endPos<actualMessage.length())?actualMessage.substring(endPos):"");
						offer.setIssuedCouponCount(offer.getIssuedCouponCount()+1);
						response = new CouponResponseDO();
						response.setCouponOffer(offer);
						response.setResponseDate(now);
						response.setResponseDetail(couponCode);
						response.setSubscriber(s);
						response.setResponseType(Type.Issued);
						response.setRedemptionCount(0);
					} else {
						offer.setRejectedResponseCount(offer.getRejectedResponseCount()+1);
						actualMessage = cNode.getUnavailableMessage();
						response = new CouponResponseDO();
						response.setCouponOffer(offer);
						response.setResponseDate(now);
						response.setResponseType(offer.getIssuedCouponCount()<offer.getMaxCoupons()?Type.Expired:Type.OverMax);
						response.setSubscriber(s);
					}
					response.setActualMessage(actualMessage);
					
					getEntityManager().persist(response);
					
					String fromAddress = s.getSubscriptions().get(simpleNode.getCampaign()).getLastHitEntryPoint();
					EntryPointType fromType = s.getSubscriptions().get(simpleNode.getCampaign()).getLastHitEntryType();
					switch(fromType)
					{
						
						case Email:
							sendMessageEvent = CATEvent.buildSendMessageRequestedEvent(fromAddress, fromType, s.getEmail(), actualMessage, cNode.getName(),cNode.getUid(),version);
							break;
						
						case SMS:
							sendMessageEvent = CATEvent.buildSendMessageRequestedEvent(fromAddress, fromType, s.getPhoneNumber(), actualMessage, cNode.getName(),cNode.getUid(),version);
							break;
							
						case Twitter:
							sendMessageEvent = CATEvent.buildSendMessageRequestedEvent(fromAddress, fromType, s.getTwitterUsername(), actualMessage, cNode.getName(),cNode.getUid(),version);
							break;
							
						default:
							throw new IllegalStateException("NodeDO must be either Email or SMS . . . mixed or other types are not supported.");
					}
					s.getSubscriptions().get(simpleNode.getCampaign()).setLastHitNode(simpleNode);
					getEventManager().queueEvent(sendMessageEvent);
					getEventManager().queueEvent(CATEvent.buildNodeOperationCompletedEvent(dest.getUid(), ""+s.getPrimaryKey()));
				} else if(e.getTargetType().equals(CATTargetType.AllAppliedSubscribers)){
					Query q = getEntityManager().createNamedQuery("all.subscribers.on.node");
					q.setParameter("nodeUID", conn.getSourceNodeUID());
					List<SubscriberDO> subs = (List<SubscriberDO>)q.getResultList();
					for(SubscriberDO s : subs)
					{
						
						Date now = new Date();
						String actualMessage;
						
						CouponOfferDO offer = getEntityManager().find(CouponOfferDO.class, cNode.getCouponId());
						CouponResponseDO response;
						
						if((cNode.getUnavailableDate()==null || now.before(cNode.getUnavailableDate())) && (offer.getMaxCoupons()<0 || offer.getIssuedCouponCount()<offer.getMaxCoupons()))
						{
							String couponCode=null;
							
							if(cNode.getCouponCode()!=null)
							{
								couponCode=cNode.getCouponCode();
							} else {
								//Get the counter for 6-digit coupon codes.  This may need to change in the future.
								int COUPON_CODE_LENGTH=6;
								CouponCounterDO counter = getEntityManager().find(CouponCounterDO.class, COUPON_CODE_LENGTH);
								if(counter==null)
								{
									counter = new CouponCounterDO();
									counter.setCouponCodeLength(COUPON_CODE_LENGTH);
									counter.setNextNumber(1l);
									counter.setBitScramble(SequentialBitShuffler.generateBitShuffle(COUPON_CODE_LENGTH));
									getEntityManager().persist(counter);
								}
								getEntityManager().lock(counter, LockModeType.WRITE);
								SequentialBitShuffler shuffler = new SequentialBitShuffler(counter.getBitScramble(),COUPON_CODE_LENGTH);
								couponCode = shuffler.generateCode(counter.getNextNumber());
								counter.setNextNumber(counter.getNextNumber()+1);							
							}
							actualMessage = cNode.getAvailableMessage();
							int startPos = actualMessage.indexOf('{');
							int endPos = actualMessage.indexOf('}',-1)+1;
							if(startPos==-1 || endPos==-1 || endPos<=startPos)
								throw new IllegalArgumentException("Cannot insert coupon code, since braces are not inserted properly.");
							actualMessage = actualMessage.substring(0,startPos) + couponCode + ((endPos<actualMessage.length())?actualMessage.substring(endPos):"");
							offer.setIssuedCouponCount(offer.getIssuedCouponCount()+1);
							response = new CouponResponseDO();
							response.setCouponOffer(offer);
							response.setResponseDate(now);
							response.setResponseDetail(couponCode);
							response.setResponseType(Type.Issued);
							response.setRedemptionCount(0);
							response.setSubscriber(s);
						} else {
							offer.setRejectedResponseCount(offer.getRejectedResponseCount()+1);
							actualMessage = cNode.getUnavailableMessage();
							response = new CouponResponseDO();
							response.setCouponOffer(offer);
							response.setResponseDate(now);
							response.setResponseType(offer.getIssuedCouponCount()<offer.getMaxCoupons()?Type.Expired:Type.OverMax);
							response.setSubscriber(s);
						}
						response.setActualMessage(actualMessage);
						
						getEntityManager().persist(response);
						
						String fromAddress = s.getSubscriptions().get(simpleNode.getCampaign()).getLastHitEntryPoint();
						EntryPointType fromType = s.getSubscriptions().get(simpleNode.getCampaign()).getLastHitEntryType();
						switch(fromType)
						{
							case Email:
								sendMessageEvent = CATEvent.buildSendMessageRequestedEvent(fromAddress, fromType, s.getEmail(), actualMessage, cNode.getName(), cNode.getUid(), version);
								break;
							
							case SMS:
								sendMessageEvent = CATEvent.buildSendMessageRequestedEvent(fromAddress, fromType, s.getPhoneNumber(), actualMessage, cNode.getName(), cNode.getUid(), version);
						
							case Twitter:
								sendMessageEvent = CATEvent.buildSendMessageRequestedEvent(fromAddress, fromType, s.getTwitterUsername(), actualMessage, cNode.getName(), cNode.getUid(), version);
						
							default:
								throw new IllegalStateException("NodeDO must be either Email or SMS . . . mixed or other types are not supported.");
						}
						s.getSubscriptions().get(simpleNode.getCampaign()).setLastHitNode(simpleNode);
						getEntityManager().persist(simpleNode);
						getEventManager().queueEvent(sendMessageEvent);
						getEventManager().queueEvent(CATEvent.buildNodeOperationCompletedEvent(dest.getUid(), ""+s.getPrimaryKey()));
					}
				}
			}
			break; //End Coupon node type
			
			default:
				throw new IllegalArgumentException("Invalid target type for a ConnectorFired event. --"+e.getTargetType());
		}
	}

}
