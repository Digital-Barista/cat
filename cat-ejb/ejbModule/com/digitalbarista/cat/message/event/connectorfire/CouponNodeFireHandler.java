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

import org.hibernate.LockMode;
import org.hibernate.Session;

import com.digitalbarista.cat.business.CampaignMessagePart;
import com.digitalbarista.cat.business.Connector;
import com.digitalbarista.cat.business.CouponNode;
import com.digitalbarista.cat.business.Node;
import com.digitalbarista.cat.data.CampaignDO;
import com.digitalbarista.cat.data.CampaignSubscriberLinkDO;
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
						
		CampaignSubscriberLinkDO csl=null;
		for(CampaignDO subCamp : s.getSubscriptions().keySet())
		{
			if(subCamp.getUID().equalsIgnoreCase(simpleNode.getCampaign().getUID()))
			{
				csl=s.getSubscriptions().get(subCamp);
				break;
			}
		}
		String fromAddress = csl.getLastHitEntryPoint();
		EntryPointType fromType = csl.getLastHitEntryType();

		if(sMan.isSubscriberBlacklisted(s.getAddress(), fromType, fromAddress, csl.getCampaign().getClient().getPrimaryKey()))
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
				CouponCounterDO counter = (CouponCounterDO)((Session)em.getDelegate()).get(CouponCounterDO.class,COUPON_CODE_LENGTH, LockMode.UPGRADE);
				if(counter==null)
				{
					counter = new CouponCounterDO();
					counter.setCouponCodeLength(COUPON_CODE_LENGTH);
					counter.setNextNumber(1l);
					counter.setBitScramble(SequentialBitShuffler.generateBitShuffle(COUPON_CODE_LENGTH));
					em.persist(counter);
					counter = (CouponCounterDO)((Session)em.getDelegate()).get(CouponCounterDO.class,COUPON_CODE_LENGTH, LockMode.UPGRADE);
				}
				SequentialBitShuffler shuffler = new SequentialBitShuffler(counter.getBitScramble(),COUPON_CODE_LENGTH);
				couponCode = shuffler.generateCode(counter.getNextNumber());
				counter.setNextNumber(counter.getNextNumber()+1);							
			}
			actualMessage = cNode.getAvailableMessageForType(fromType);
			//This is for coupon code, and really should check it, but doesn't.
			int startPos = actualMessage.indexOf('{');
			int endPos = actualMessage.indexOf('}',-1)+1;
			if(startPos==-1 || endPos==-1 || endPos<=startPos)
				throw new IllegalArgumentException("Cannot insert coupon code, since braces are not inserted properly.");
			actualMessage = actualMessage.substring(0,startPos) + couponCode + ((endPos<actualMessage.length())?actualMessage.substring(endPos):"");
			//Same here for expiration date.
			if(cNode.getExpireDays()!=null && cNode.getExpireDays()>0)
			{
				Calendar expireDate = GregorianCalendar.getInstance();
				expireDate.setTime(now);
				expireDate.add(Calendar.DAY_OF_MONTH, cNode.getExpireDays());
				startPos = actualMessage.indexOf('[');
				endPos = actualMessage.indexOf(']',-1)+1;
				if(startPos==-1 || endPos==-1 || endPos<=startPos)
					throw new IllegalArgumentException("Cannot insert expiration date, since braces are not inserted properly.");
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
			actualMessage = cNode.getUnavailableMessageForType(fromType);
			response = new CouponResponseDO();
			response.setCouponOffer(offer);
			response.setResponseDate(now);
			response.setResponseType(offer.getIssuedCouponCount()<offer.getMaxCoupons()?Type.Expired:Type.OverMax);
			response.setSubscriber(s);
		}
		response.setActualMessage(actualMessage);
		
		em.persist(response);
		
		CampaignMessagePart messagePart = mMan.getMessagePart(cMan.getDetailedCampaign(cNode.getCampaignUID()), fromType, actualMessage);

		for(String splitMessage : messagePart.getMessages())
		{
			sendMessageEvent = CATEvent.buildSendMessageRequestedEvent(fromAddress, fromType, s.getAddress(), splitMessage, cNode.getName(),cNode.getUid(),version);
			eMan.queueEvent(sendMessageEvent);
		}
			
		csl.setLastHitNode(simpleNode);
		eMan.queueEvent(CATEvent.buildNodeOperationCompletedEvent(dest.getUid(), ""+s.getPrimaryKey()));
	}
}
