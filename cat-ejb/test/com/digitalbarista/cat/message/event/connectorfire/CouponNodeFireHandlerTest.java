package com.digitalbarista.cat.message.event.connectorfire;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashMap;

import javax.ejb.SessionContext;
import javax.persistence.EntityManager;
import javax.persistence.LockModeType;

import org.easymock.EasyMock;
import org.easymock.EasyMockSupport;
import org.easymock.IArgumentMatcher;
import org.testng.Assert;
import org.testng.annotations.Test;

import com.digitalbarista.cat.business.Connector;
import com.digitalbarista.cat.business.CouponNode;
import com.digitalbarista.cat.business.ImmediateConnector;
import com.digitalbarista.cat.data.CampaignDO;
import com.digitalbarista.cat.data.CampaignSubscriberLinkDO;
import com.digitalbarista.cat.data.CouponCounterDO;
import com.digitalbarista.cat.data.CouponOfferDO;
import com.digitalbarista.cat.data.CouponResponseDO;
import com.digitalbarista.cat.data.EntryPointType;
import com.digitalbarista.cat.data.NodeDO;
import com.digitalbarista.cat.data.SubscriberDO;
import com.digitalbarista.cat.ejb.session.CampaignManager;
import com.digitalbarista.cat.ejb.session.EventManager;
import com.digitalbarista.cat.ejb.session.MessageManager;
import com.digitalbarista.cat.ejb.session.SubscriptionManager;
import com.digitalbarista.cat.message.event.CATEvent;

public class CouponNodeFireHandlerTest extends EasyMockSupport{
	
	private static int[] shuffle=new int[30];
	
	static
	{
		for(int loop=0; loop<30; loop++)
			shuffle[loop]=loop;
	}
	
	@Test
	public void testDateReplacement() throws SecurityException, NoSuchMethodException
	{
		String fromAddress="555-1212";
		CouponNodeFireHandler handler = new CouponNodeFireHandler();
		
		EntityManager em = createMock(EntityManager.class);
		SessionContext ctx = createMock(SessionContext.class);
		
		EventManager eMan = createMock(EventManager.class);
		CampaignManager cMan = createMock(CampaignManager.class);
		SubscriptionManager sMan = createMock(SubscriptionManager.class);
		MessageManager mMan = createMock(MessageManager.class);
		
		EasyMock.expect(ctx.lookup("ejb/cat/EventManager")).andStubReturn(eMan);
		EasyMock.expect(ctx.lookup("ejb/cat/CampaignManager")).andStubReturn(cMan);
		EasyMock.expect(ctx.lookup("ejb/cat/SubscriptionManager")).andStubReturn(sMan);
		EasyMock.expect(ctx.lookup("ejb/cat/MessageManager")).andStubReturn(mMan);
		
		Connector conn = new ImmediateConnector();
		conn.setUid("MOCK-UID");
		
		CampaignDO campaignDO = new CampaignDO();
		CampaignSubscriberLinkDO cslDO = new CampaignSubscriberLinkDO();
		cslDO.setCampaign(campaignDO);
		cslDO.setLastHitEntryPoint("555-1212");
		cslDO.setLastHitEntryType(EntryPointType.SMS);
		
		CouponNode dest = createMockBuilder(CouponNode.class).addMockedMethod(CouponNode.class.getDeclaredMethod("getCouponId")).createMock();
		dest.setUid("NODE-UID");
		dest.setAvailableMessage("Code={6_CODE} and EXPDate=[EXPDATE]");
		dest.setExpireDays(5);
		EasyMock.expect(dest.getCouponId()).andStubReturn(new Long(2));		
		NodeDO destDO = new NodeDO();
		destDO.setUID("NODE-UID");
		destDO.setCampaign(campaignDO);
		CouponOfferDO couponDO = new CouponOfferDO();
		couponDO.setMaxCoupons(-1l);
		couponDO.setPrimaryKey(new Long(2));
		
		CouponCounterDO cc = new CouponCounterDO();
		cc.setBitScramble(shuffle);
		cc.setCouponCodeLength(6);
		cc.setNextNumber(1l);
		
		Integer version = 1;
		SubscriberDO s = new SubscriberDO();
		s.setPrimaryKey(1l);
		s.setSubscriptions(new HashMap<CampaignDO,CampaignSubscriberLinkDO>());
		cslDO.setSubscriber(s);
		s.getSubscriptions().put(campaignDO, cslDO);
		
		EasyMock.expect(cMan.getSimpleNode(dest.getUid())).andStubReturn(destDO);
		EasyMock.expect(em.find(EasyMock.eq(CouponOfferDO.class), EasyMock.eq(new Long(2)))).andStubReturn(couponDO);
//		EasyMock.expect(sMan.isSubscriberBlacklisted(1l, "555-1212", EntryPointType.SMS)).andStubReturn(false);
		EasyMock.expect(em.find(CouponCounterDO.class, 6)).andStubReturn(cc);
		em.lock(cc,LockModeType.READ);
		em.persist(matchCoupon());
		
		CATEvent e = CATEvent.buildFireConnectorForSubscriberEvent("MOCK-UID", "1",-1);
		
		replayAll();
		
		handler.handle(em, ctx, conn, dest, version, s, e);
		
		
	}

	private CouponResponseDO matchCoupon()
	{
		EasyMock.reportMatcher(new CouponMatcher());
		return null;
	}
	
	public class CouponMatcher implements IArgumentMatcher
	{

		@Override
		public void appendTo(StringBuffer arg0) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public boolean matches(Object arg0) {
			if(!(arg0 instanceof CouponResponseDO))
				return false;
			CouponResponseDO resp = (CouponResponseDO)arg0;
			Calendar exp = GregorianCalendar.getInstance();
			exp.add(Calendar.DAY_OF_MONTH, 5);
			Assert.assertEquals(resp.getActualMessage(),"Code=000001 and EXPDate="+new SimpleDateFormat("MM/dd/yyyy").format(exp.getTime()), "Messages did not match.");
			return true;
		}
		
	}
}
