package com.digitalbarista.cat.ejb.session;

import com.digitalbarista.cat.business.Contact;
import com.digitalbarista.cat.business.Coupon;
import com.digitalbarista.cat.data.CouponOfferDO;
import com.digitalbarista.cat.data.CouponRedemptionDO;
import com.digitalbarista.cat.data.CouponResponseDO;
import com.digitalbarista.cat.util.CouponRedemptionMessage;
import com.digitalbarista.cat.util.SecurityUtil;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import org.apache.log4j.LogManager;
import org.hibernate.Criteria;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.Restrictions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * Session Bean implementation class ClientDO
 */
@Controller("CouponManager")
@Transactional(propagation=Propagation.REQUIRED)
@RequestMapping(value="/coupons",
                produces={"application/xml","application/json"},
                consumes={"application/xml","application/json"})
public class CouponManager{

	private static final int SUCCESS=0;
	
	private static final int OVER_MAX_CODE=1;
	private static final int EXPIRED_CODE=2;
	private static final int NOT_FOUND_CODE=3;
	private static final int NOT_ALLOWED_CODE=4;
	
	private static final int UNKNOWN_ERROR=5;
	
        @Autowired
        private SessionFactory sf;
        
        @Autowired
        private UserManager userManager;
	
        @Autowired
        private ContactManager contactManager;

        @Autowired
        private SecurityUtil securityUtil;
        
        @SuppressWarnings("unchecked")
        @PreAuthorize("hasRole(admin) || hasRole(client)")
        @RequestMapping(method=RequestMethod.POST,value="/{code}")
	public CouponRedemptionMessage redeemCoupon(@PathVariable("cond") String couponCode) {
		if(couponCode!=null)
			couponCode=couponCode.trim();
		
		Criteria crit = sf.getCurrentSession().createCriteria(CouponResponseDO.class);
		crit.add(Restrictions.eq("responseDetail", couponCode));
		crit.add(Restrictions.eq("responseType", CouponResponseDO.Type.Issued));
		List<CouponResponseDO> cRespList = (List<CouponResponseDO>)crit.list();
		
		if(cRespList==null || cRespList.size() ==0)
		{
			return new CouponRedemptionMessage(NOT_FOUND_CODE, "This coupon could not be found");
		}

		CouponResponseDO cResp = null;
		for(CouponResponseDO cRespTemp : cRespList)
		{
			if(!securityUtil.isAdmin() && !userManager.isUserAllowedForClientId(securityUtil.getPrincipalName(), cRespTemp.getCouponOffer().getCampaign().getClient().getPrimaryKey()))
				continue;
			if(cResp==null)
			{
				cResp = cRespTemp;
			}
			else
			{
				LogManager.getLogger(getClass()).error("Coupon code :"+couponCode+" has been DUPLICATED!!");
				return new CouponRedemptionMessage(UNKNOWN_ERROR, "An unknown internal error occurred.  Please contact the administrator if this continues.");
			}
		}

		// If the coupon doesn't match return an appropriate error
		if (cResp == null)
		{
			return new CouponRedemptionMessage(NOT_FOUND_CODE, "This coupon could not be found");
		}
		
		//Also . . . should we check to see if the campagin is active?
		
		if(cResp.getCouponOffer().getMaxRedemptions()>0)
		{
			if(cResp.getRedemptionCount()>=cResp.getCouponOffer().getMaxRedemptions())
			return new CouponRedemptionMessage(OVER_MAX_CODE,"This coupon has already been redeemed "+cResp.getRedemptionCount()+" times.");
		}

		Contact c = contactManager.getContactForSubscription(cResp.getSubscriber(),cResp.getCouponOffer().getCampaign());
		if(c==null)
		{
			LogManager.getLogger(getClass()).error("Coupon code :"+couponCode+" belongs to more than 1 contact!!");
			return new CouponRedemptionMessage(UNKNOWN_ERROR, "An unknown internal error occurred.  Please contact the administrator if this continues.");
		}
		
		cResp.setRedemptionCount(cResp.getRedemptionCount()+1);
		
		CouponRedemptionDO cRed = new CouponRedemptionDO();
		cRed.setCouponResponse(cResp);
		cRed.setRedemptionDate(new Date());
		cRed.setRedeemedByUsername(securityUtil.getPrincipalName());
		
		sf.getCurrentSession().persist(cRed);
	
		return new CouponRedemptionMessage(SUCCESS,"Coupon successfully redeemed.",cResp.getActualMessage(),cResp.getCouponOffer().getOfferCode(),c.getUID());
	}

    @PreAuthorize("hasRole(admin) || hasRole(client) || hasRole(account.manager)")
    @RequestMapping(method=RequestMethod.GET)
    public List<Coupon> couponSummaryByClient(@RequestParam("clientid") Long clientID) 
    {
		List<Long> clientIds = null;
		
		if (clientID != null)
		{
			clientIds = new ArrayList<Long>();
			clientIds.add(clientID);
		}
		return getCouponSummaries(clientIds);
	}

	public List<Coupon> getCouponSummaries(List<Long> clientIDs)
	{
		List<Coupon> ret = new ArrayList<Coupon>();
		List<Long> allowedClientIds = securityUtil.getAllowedClientIDs(sf.getCurrentSession(), clientIDs);
		
		if (allowedClientIds.size() > 0)
		{
			Criteria crit = sf.getCurrentSession().createCriteria(CouponOfferDO.class);
			crit.createAlias("campaign", "campaign");
			crit.add(Restrictions.in("campaign.client.id", allowedClientIds));
			
			Coupon temp;
			for(CouponOfferDO offer : (List<CouponOfferDO>)crit.list())
			{
				temp = new Coupon();
				temp.copyFrom(offer);
				ret.add(temp);
			}
		}
		return ret;
	}
	
        @RequestMapping(method= RequestMethod.GET,value="/{code}")
        public CouponRedemptionMessage queryCoupon(@PathVariable("code") String couponCode) {
		if(couponCode!=null)
			couponCode=couponCode.trim();

		Criteria crit = sf.getCurrentSession().createCriteria(CouponResponseDO.class);
		crit.add(Restrictions.eq("responseDetail", couponCode));
		crit.add(Restrictions.eq("responseType", CouponResponseDO.Type.Issued));
		List<CouponResponseDO> cRespList = (List<CouponResponseDO>)crit.list();
		
		if(cRespList==null || cRespList.size() ==0)
		{
			return new CouponRedemptionMessage(NOT_FOUND_CODE, "This coupon could not be found");
		}

		CouponResponseDO cResp = null;
		for(CouponResponseDO cRespTemp : cRespList)
		{
			if(!securityUtil.isAdmin() && !userManager.isUserAllowedForClientId(securityUtil.getPrincipalName(), cRespTemp.getCouponOffer().getCampaign().getClient().getPrimaryKey()))
				continue;
			if(cResp==null)
			{
				cResp = cRespTemp;
			}
			else
			{
				LogManager.getLogger(getClass()).error("Coupon code :"+couponCode+" has been DUPLICATED!!");
				return new CouponRedemptionMessage(UNKNOWN_ERROR, "An unknown internal error occurred.  Please contact the administrator if this continues.");
			}
		}

		// If the coupon doesn't match return an appropriate error
		if (cResp == null)
		{
			return new CouponRedemptionMessage(NOT_FOUND_CODE, "This coupon could not be found");
		}
		
		//Also . . . should we check to see if the campagin is active?
		
		if(cResp.getCouponOffer().getMaxRedemptions()>0)
		{
			if(cResp.getRedemptionCount()>=cResp.getCouponOffer().getMaxRedemptions())
			return new CouponRedemptionMessage(OVER_MAX_CODE,"This coupon has already been redeemed "+cResp.getRedemptionCount()+" times.");
		}
		return new CouponRedemptionMessage(SUCCESS,"Coupon is valid.",cResp.getActualMessage(),cResp.getCouponOffer().getOfferCode(),null);
	}

}
