package com.digitalbarista.cat.message.event.connectorfire;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

import javax.ejb.SessionContext;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.persistence.EntityManager;
import javax.persistence.LockModeType;

import com.digitalbarista.cat.business.CampaignMessagePart;
import com.digitalbarista.cat.business.Connector;
import com.digitalbarista.cat.business.CouponNode;
import com.digitalbarista.cat.business.Node;
import com.digitalbarista.cat.data.CouponCounterDO;
import com.digitalbarista.cat.data.CouponOfferDO;
import com.digitalbarista.cat.data.CouponResponseDO;
import com.digitalbarista.cat.data.EntryPointType;
import com.digitalbarista.cat.data.NodeDO;
import com.digitalbarista.cat.data.SubscriberDO;
import com.digitalbarista.cat.data.CouponResponseDO.Type;
import com.digitalbarista.cat.ejb.session.CampaignManager;
import com.digitalbarista.cat.ejb.session.EventManager;
import com.digitalbarista.cat.ejb.session.MessageManager;
import com.digitalbarista.cat.ejb.session.SubscriptionManager;
import com.digitalbarista.cat.message.event.CATEvent;
import com.digitalbarista.cat.util.SequentialBitShuffler;

public class CouponNodeFireHandler extends ConnectorFireHandler {

	@Override
	public void handle(EntityManager em, SessionContext ctx, Connector conn, Node dest, Integer version, SubscriberDO s, CATEvent e) 
	{
		MessageManager mMan = (MessageManager)ctx.lookup("ejb/cat/MessageManager");
		SubscriptionManager sMan = (SubscriptionManager)ctx.lookup("ejb/cat/SubscriptionManager");
		CampaignManager cMan = (CampaignManager)ctx.lookup("ejb/cat/CampaignManager");
		EventManager eMan = (EventManager)ctx.lookup("ejb/cat/EventManager");

		CouponNode cNode = (CouponNode)dest;
		CATEvent sendMessageEvent=null;
		NodeDO simpleNode=cMan.getSimpleNode(cNode.getUid());

		Date now = new Date();
		String actualMessage;

		CouponOfferDO offer = em.find(CouponOfferDO.class, cNode.getCouponId());
		CouponResponseDO response;
						
		String fromAddress = s.getSubscriptions().get(simpleNode.getCampaign()).getLastHitEntryPoint();
		EntryPointType fromType = s.getSubscriptions().get(simpleNode.getCampaign()).getLastHitEntryType();

		if(sMan.isSubscriberBlacklisted(s.getPrimaryKey(), fromAddress, fromType))
			return;
		
		if((cNode.getUnavailableDate()==null || now.before(cNode.getUnavailableDate())) && (offer.getMaxCoupons()<0 || offer.getIssuedCouponCount()<offer.getMaxCoupons()))
		{
			String couponCode=null;
			
			if(cNode.getCouponCode()!=null)
			{
				couponCode=cNode.getCouponCode();
			} else {
				//Get the counter for 6-digit coupon codes.  This may need to change in the future.
				int COUPON_CODE_LENGTH=6;
				CouponCounterDO counter = em.find(CouponCounterDO.class, COUPON_CODE_LENGTH);
				if(counter==null)
				{
					counter = new CouponCounterDO();
					counter.setCouponCodeLength(COUPON_CODE_LENGTH);
					counter.setNextNumber(1l);
					counter.setBitScramble(SequentialBitShuffler.generateBitShuffle(COUPON_CODE_LENGTH));
					em.persist(counter);
					counter = em.find(CouponCounterDO.class, COUPON_CODE_LENGTH);
				}
				em.lock(counter, LockModeType.READ);
				SequentialBitShuffler shuffler = new SequentialBitShuffler(counter.getBitScramble(),COUPON_CODE_LENGTH);
				couponCode = shuffler.generateCode(counter.getNextNumber());
				counter.setNextNumber(counter.getNextNumber()+1);							
			}
			actualMessage = cNode.getAvailableMessage();
			//This is for coupon code, and really should check it, but doesn't.
			int startPos = actualMessage.indexOf('{');
			int endPos = actualMessage.indexOf('}',-1)+1;
			if(startPos==-1 || endPos==-1 || endPos<=startPos)
				throw new IllegalArgumentException("Cannot insert coupon code, since braces are not inserted properly.");
			actualMessage = actualMessage.substring(0,startPos) + couponCode + ((endPos<actualMessage.length())?actualMessage.substring(endPos):"");
			//Same here for expiration date.
			if(cNode.getExpireDays()>0)
			{
				Calendar expireDate = GregorianCalendar.getInstance();
				expireDate.setTime(now);
				expireDate.add(Calendar.DAY_OF_MONTH, cNode.getExpireDays());
				startPos = actualMessage.indexOf('[');
				endPos = actualMessage.indexOf(']',-1)+1;
				if(startPos==-1 || endPos==-1 || endPos<=startPos)
					throw new IllegalArgumentException("Cannot insert coupon code, since braces are not inserted properly.");
				actualMessage = actualMessage.substring(0,startPos) + new SimpleDateFormat("MM/dd/yyyy").format(expireDate.getTime()) + ((endPos<actualMessage.length())?actualMessage.substring(endPos):"");
			}
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
		
		em.persist(response);
		
		List<CampaignMessagePart> messageParts = mMan.getMessageParts(cMan.getDetailedCampaign(cNode.getCampaignUID()), actualMessage);

		for(CampaignMessagePart messagePart : messageParts)
		{
			if(messagePart.getEntryType()!=fromType)
				continue;
			for(String splitMessage : messagePart.getMessages())
			{
				switch(fromType)
				{
					
					case Email:
						sendMessageEvent = CATEvent.buildSendMessageRequestedEvent(fromAddress, fromType, s.getEmail(), splitMessage, cNode.getName(),cNode.getUid(),version);
						break;
					
					case SMS:
						sendMessageEvent = CATEvent.buildSendMessageRequestedEvent(fromAddress, fromType, s.getPhoneNumber(), splitMessage, cNode.getName(),cNode.getUid(),version);
						break;
						
					case Twitter:
						sendMessageEvent = CATEvent.buildSendMessageRequestedEvent(fromAddress, fromType, s.getTwitterID(), splitMessage, cNode.getName(),cNode.getUid(),version);
						break;
						
					case Facebook:
						sendMessageEvent = CATEvent.buildSendMessageRequestedEvent(fromAddress, fromType, s.getFacebookID(), splitMessage, cNode.getName(),cNode.getUid(),version);
						break;
						
					default:
						throw new IllegalStateException("NodeDO must be either Email or SMS . . . mixed or other types are not supported.");
				}
				eMan.queueEvent(sendMessageEvent);
			}
		}		
		s.getSubscriptions().get(simpleNode.getCampaign()).setLastHitNode(simpleNode);
		eMan.queueEvent(CATEvent.buildNodeOperationCompletedEvent(dest.getUid(), ""+s.getPrimaryKey()));
	}
}
