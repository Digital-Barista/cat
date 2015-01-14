package com.digitalbarista.cat.message.event.connectorfire;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

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
import com.digitalbarista.cat.message.event.CATEventHandlerFactory;
import com.digitalbarista.cat.util.SequentialBitShuffler;
import org.hibernate.LockOptions;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class CouponNodeFireHandler implements ConnectorFireHandler {

  @Autowired
  private MessageManager mMan;
  
  @Autowired 
  private SubscriptionManager sMan;
  
  @Autowired
  private CampaignManager cMan;
  
  @Autowired
  private EventManager eMan;
  
  @Autowired
  private SessionFactory sf;
  
  @Autowired
  private CATEventHandlerFactory eventHandlerFactory;
  
	@Override
        @Transactional
	public void handle(Connector conn, Node dest, Integer version, SubscriberDO s, CATEvent e) 
	{
		CouponNode cNode = (CouponNode)dest;
		CATEvent sendMessageEvent=null;
		NodeDO simpleNode=cMan.getSimpleNode(cNode.getUid());

		Date now = new Date();
		String actualMessage;

		CouponOfferDO offer = (CouponOfferDO)sf.getCurrentSession().get(CouponOfferDO.class, cNode.getCouponId());
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
		sf.getCurrentSession().buildLockRequest(LockOptions.UPGRADE).lock(csl);
		sf.getCurrentSession().refresh(csl);
		String fromAddress = csl.getLastHitEntryPoint();
		EntryPointType fromType = csl.getLastHitEntryType();

		if(sMan.isSubscriberBlacklisted(s.getAddress(), fromType, fromAddress, csl.getCampaign().getClient().getPrimaryKey()))
			return;
		
		if((cNode.getUnavailableDate()==null || now.before(cNode.getUnavailableDate())) && (offer.getMaxCoupons()<0 || offer.getIssuedCouponCount()<offer.getMaxCoupons()))
		{
			String couponCode=null;
			
                        int COUPON_CODE_LENGTH=6;
			if(cNode.getCouponCode()!=null)
			{
				couponCode=cNode.getCouponCode();
			} else {
				//Get the counter for 6-digit coupon codes.  This may need to change in the future.
				CouponCounterDO counter = (CouponCounterDO)sf.getCurrentSession().get(CouponCounterDO.class,COUPON_CODE_LENGTH, LockOptions.UPGRADE);
				if(counter==null)
				{
					counter = new CouponCounterDO();
					counter.setCouponCodeLength(COUPON_CODE_LENGTH);
					counter.setNextNumber(1l);
					counter.setBitScramble(SequentialBitShuffler.generateBitShuffle(COUPON_CODE_LENGTH));
					sf.getCurrentSession().persist(counter);
					counter = (CouponCounterDO)sf.getCurrentSession().get(CouponCounterDO.class,COUPON_CODE_LENGTH, LockOptions.UPGRADE);
				}
				SequentialBitShuffler shuffler = new SequentialBitShuffler(counter.getBitScramble(),COUPON_CODE_LENGTH);
				couponCode = shuffler.generateCode(counter.getNextNumber());
				counter.setNextNumber(counter.getNextNumber()+1);							
			}
			actualMessage = cNode.getAvailableMessageForType(fromType);
			//This is for coupon code, and really should check it, but doesn't.
			actualMessage = actualMessage.replaceAll("\\{"+COUPON_CODE_LENGTH+"_CHAR_CODE\\}", couponCode);
			//Same here for expiration date.
			if(cNode.getExpireDays()!=null && cNode.getExpireDays()>0)
			{
				Calendar expireDate = GregorianCalendar.getInstance();
				expireDate.setTime(now);
				expireDate.add(Calendar.DAY_OF_MONTH, cNode.getExpireDays());
				actualMessage = actualMessage.replaceAll("\\[EXPIRE_DATE\\]",new SimpleDateFormat("MM/dd/yyyy").format(expireDate.getTime()));
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
		
		sf.getCurrentSession().persist(response);
		
		CampaignMessagePart messagePart = mMan.getMessagePart(cMan.getLastPublishedCampaign(cNode.getCampaignUID()), fromType, actualMessage);

		for(String splitMessage : messagePart.getMessages())
		{
			sendMessageEvent = CATEvent.buildSendMessageRequestedEvent(fromAddress, fromType, s.getAddress(), splitMessage, cNode.getName(),cNode.getUid(),version);
			if(fromType==EntryPointType.Facebook)
                        {
                            eventHandlerFactory.processEvent(sendMessageEvent);
                        } else {
                            eMan.queueEvent(sendMessageEvent);
                        }
		}
			
		csl.setLastHitNode(simpleNode);
		eMan.queueEvent(CATEvent.buildNodeOperationCompletedEvent(dest.getUid(), ""+s.getPrimaryKey()));
	}
}
