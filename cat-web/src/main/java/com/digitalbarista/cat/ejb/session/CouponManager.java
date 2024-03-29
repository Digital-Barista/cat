package com.digitalbarista.cat.ejb.session;

import com.digitalbarista.cat.business.Contact;
import com.digitalbarista.cat.business.Coupon;
import com.digitalbarista.cat.business.ServiceMetadata;
import com.digitalbarista.cat.business.ServiceResponse;
import com.digitalbarista.cat.data.CampaignInfoDO;
import com.digitalbarista.cat.data.CampaignStatus;
import com.digitalbarista.cat.data.CouponOfferDO;
import com.digitalbarista.cat.data.CouponRedemptionDO;
import com.digitalbarista.cat.data.CouponResponseDO;
import com.digitalbarista.cat.data.EntryPointType;
import com.digitalbarista.cat.util.CouponRedemptionMessage;
import com.digitalbarista.cat.util.CriteriaUtil;
import com.digitalbarista.cat.util.SecurityUtil;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import org.apache.log4j.LogManager;
import org.hibernate.Criteria;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.Restrictions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * Session Bean implementation class ClientDO
 */
@Component("CouponManager")
@Lazy
@Transactional(propagation=Propagation.REQUIRED)
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
  private CampaignManager campaignManager;

  @Autowired
  private SecurityUtil securityUtil;

  @PreAuthorize("hasRole('ROLE_client')")
  public List<String> findLuckyNumberList(Long clientID)
  {
    Criteria crit;
    crit = sf.getCurrentSession().createCriteria(CampaignInfoDO.class);
    crit.add(Restrictions.eq("name", CampaignInfoDO.KEY_AUTO_START_NODE_UID));
    crit.createAlias("campaign", "c");
    crit.add(Restrictions.eq("c.status", CampaignStatus.Active));
    crit.add(Restrictions.eq("c.client.id", clientID));
    CampaignInfoDO cInfo = (CampaignInfoDO) crit.uniqueResult();
    
    crit = sf.getCurrentSession().createCriteria(CouponResponseDO.class);
    crit.createAlias("couponOffer", "co");
    crit.add(Restrictions.eq("co.campaign.id",cInfo.getCampaign().getPrimaryKey()));
    List<CouponResponseDO> couponsSentList = crit.list();
    
    List<String> ret = new ArrayList<String>();
    for(CouponResponseDO resp : couponsSentList)
        ret.add(resp.getResponseDetail());
    
    return ret;
}
  
  @SuppressWarnings("unchecked")
  @PreAuthorize("hasRole('ROLE_admin') or hasRole('ROLE_client')")
	public CouponRedemptionMessage redeemCoupon(String couponCode) {
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
	
		return new CouponRedemptionMessage(SUCCESS,"Coupon successfully redeemed.",cResp.getActualMessage(),cResp.getCouponOffer().getOfferCode(),c);
	}

  @PreAuthorize("hasRole('ROLE_admin') or hasRole('ROLE_client') or hasRole('ROLE_account.manager')")
  public List<Coupon> couponSummaryByClient(Long clientID) 
  {
		List<Long> clientIds = null;
		
		if (clientID != null)
		{
			clientIds = new ArrayList<Long>();
			clientIds.add(clientID);
		}
		return getCouponSummaries(clientIds);
	}
    public ServiceResponse getCouponSummaries(List<Long> clientIDs, ServiceMetadata metadata)
    {
        ServiceMetadata meta = metadata != null ? metadata : new ServiceMetadata();
        List<Coupon> coupons = new ArrayList<Coupon>();
        List<Long> allowedClientIds = securityUtil.getAllowedClientIDs(sf.getCurrentSession(), clientIDs);

        if (allowedClientIds.size() > 0)
        {
            Criteria crit = sf.getCurrentSession().createCriteria(CouponOfferDO.class);
            crit.createAlias("campaign", "campaign");
            crit.add(Restrictions.in("campaign.client.id", allowedClientIds));

            meta.setTotal(CriteriaUtil.getTotalResultCount(crit));
            CriteriaUtil.applyPagingInfo(crit, meta);

            Coupon temp;
            for(CouponOfferDO offer : (List<CouponOfferDO>)crit.list())
            {
                temp = new Coupon();
                temp.copyFrom(offer);
                coupons.add(temp);
            }
        }

        ServiceResponse ret = new ServiceResponse();
        ret.setResult(coupons);
        ret.setMetadata(meta);

        return ret;
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
	
  public CouponRedemptionMessage queryCoupon(String couponCode) {
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
