package com.digitalbarista.cat.message.event.connectorfire;

import java.util.Date;

import javax.persistence.EntityManager;
import javax.persistence.LockModeType;

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
import com.digitalbarista.cat.message.event.CATEvent;
import com.digitalbarista.cat.util.SequentialBitShuffler;

public class CouponNodeFireHandler extends ConnectorFireHandler {

	@Override
	public void handle(EntityManager em, CampaignManager cMan, EventManager eMan, Connector conn, Node dest, Integer version, SubscriberDO s, CATEvent e) 
	{
		CouponNode cNode = (CouponNode)dest;
		CATEvent sendMessageEvent=null;
		NodeDO simpleNode=cMan.getSimpleNode(cNode.getUid());

		Date now = new Date();
		String actualMessage;

		CouponOfferDO offer = em.find(CouponOfferDO.class, cNode.getCouponId());
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
		
		em.persist(response);
		
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
		eMan.queueEvent(sendMessageEvent);
		eMan.queueEvent(CATEvent.buildNodeOperationCompletedEvent(dest.getUid(), ""+s.getPrimaryKey()));
	}

}
