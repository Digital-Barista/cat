package com.digitalbarista.cat.ejb.session;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.annotation.Resource;
import javax.annotation.security.RolesAllowed;
import javax.annotation.security.RunAs;
import javax.ejb.EJB;
import javax.ejb.SessionContext;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.interceptor.Interceptors;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.apache.log4j.LogManager;
import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.criterion.Restrictions;
import org.hsqldb.lib.ArrayUtil;
import org.jboss.annotation.ejb.LocalBinding;
import org.jboss.annotation.security.RunAsPrincipal;

import com.digitalbarista.cat.audit.AuditInterceptor;
import com.digitalbarista.cat.business.Coupon;
import com.digitalbarista.cat.data.CouponOfferDO;
import com.digitalbarista.cat.data.CouponRedemptionDO;
import com.digitalbarista.cat.data.CouponResponseDO;
import com.digitalbarista.cat.util.CodedMessage;
import com.digitalbarista.cat.util.SecurityUtil;

/**
 * Session Bean implementation class ClientDO
 */
@Stateless
@LocalBinding(jndiBinding = "ejb/cat/CouponManager")
@RunAsPrincipal("admin")
@RunAs("admin")
@Interceptors(AuditInterceptor.class)
@TransactionAttribute(TransactionAttributeType.REQUIRED)
public class CouponManagerImpl implements CouponManager {

	private static final int SUCCESS=0;
	
	private static final int OVER_MAX_CODE=1;
	private static final int EXPIRED_CODE=2;
	private static final int NOT_FOUND_CODE=3;
	private static final int NOT_ALLOWED_CODE=4;
	
	private static final int UNKNOWN_ERROR=5;
	
	
	@Resource
	private SessionContext ctx; //Used to flag rollbacks.
	
	@PersistenceContext(unitName="cat-data")
	private EntityManager em;

	@PersistenceContext(unitName="cat-data")
	private Session session;

	@EJB(name="ejb/cat/UserManager")
	UserManager userManager;

    @SuppressWarnings("unchecked")
    @RolesAllowed({"client","admin"})
	public CodedMessage redeemCoupon(String couponCode) {
		if(couponCode!=null)
			couponCode=couponCode.trim();
		
		Criteria crit = session.createCriteria(CouponResponseDO.class);
		crit.add(Restrictions.eq("responseDetail", couponCode));
		crit.add(Restrictions.eq("responseType", CouponResponseDO.Type.Issued));
		List<CouponResponseDO> cRespList = (List<CouponResponseDO>)crit.list();
		
		if(cRespList==null || cRespList.size() ==0)
		{
			return new CodedMessage(NOT_FOUND_CODE, "This coupon could not be found");
		}

		CouponResponseDO cResp = null;
		for(CouponResponseDO cRespTemp : cRespList)
		{
			if(!ctx.isCallerInRole("admin") && !userManager.isUserAllowedForClientId(ctx.getCallerPrincipal().getName(), cRespTemp.getCouponOffer().getCampaign().getClient().getPrimaryKey()))
				continue;
			if(cResp==null)
			{
				cResp = cRespTemp;
			}
			else
			{
				LogManager.getLogger(getClass()).error("Coupon code :"+couponCode+" has been DUPLICATED!!");
				return new CodedMessage(UNKNOWN_ERROR, "An unknown internal error occurred.  Please contact the administrator if this continues.");
			}
		}

		// If the coupon doesn't match return an appropriate error
		if (cResp == null)
		{
			return new CodedMessage(NOT_FOUND_CODE, "This coupon could not be found");
		}
		
		//Also . . . should we check to see if the campagin is active?
		
		if(cResp.getCouponOffer().getMaxRedemptions()>0)
		{
			if(cResp.getRedemptionCount()>=cResp.getCouponOffer().getMaxRedemptions())
			return new CodedMessage(OVER_MAX_CODE,"This coupon has already been redeemed "+cResp.getRedemptionCount()+" times.");
		}

		cResp.setRedemptionCount(cResp.getRedemptionCount()+1);
		
		CouponRedemptionDO cRed = new CouponRedemptionDO();
		cRed.setCouponResponse(cResp);
		cRed.setRedemptionDate(new Date());
		cRed.setRedeemedByUsername(ctx.getCallerPrincipal().getName());
		
		em.persist(cRed);
	
		return new CodedMessage(SUCCESS,"Coupon successfully redeemed.",cResp.getActualMessage(),cResp.getCouponOffer().getOfferCode());
	}

	@Override
    @RolesAllowed({"client","admin","account.manager"})
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

	public List<Coupon> getCouponSummaries(List<Long> clientIDs)
	{
		List<Coupon> ret = new ArrayList<Coupon>();
		List<Long> allowedClientIds = userManager.getAllowedClientIDs(clientIDs);
		
		if (allowedClientIds.size() > 0)
		{
			Criteria crit = session.createCriteria(CouponOfferDO.class);
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
	
	@Override
	public CodedMessage queryCoupon(String couponCode) {
		if(couponCode!=null)
			couponCode=couponCode.trim();

		Criteria crit = session.createCriteria(CouponResponseDO.class);
		crit.add(Restrictions.eq("responseDetail", couponCode));
		crit.add(Restrictions.eq("responseType", CouponResponseDO.Type.Issued));
		List<CouponResponseDO> cRespList = (List<CouponResponseDO>)crit.list();
		
		if(cRespList==null || cRespList.size() ==0)
		{
			return new CodedMessage(NOT_FOUND_CODE, "This coupon could not be found");
		}

		CouponResponseDO cResp = null;
		for(CouponResponseDO cRespTemp : cRespList)
		{
			if(!ctx.isCallerInRole("admin") && !userManager.isUserAllowedForClientId(ctx.getCallerPrincipal().getName(), cRespTemp.getCouponOffer().getCampaign().getClient().getPrimaryKey()))
				continue;
			if(cResp==null)
			{
				cResp = cRespTemp;
			}
			else
			{
				LogManager.getLogger(getClass()).error("Coupon code :"+couponCode+" has been DUPLICATED!!");
				return new CodedMessage(UNKNOWN_ERROR, "An unknown internal error occurred.  Please contact the administrator if this continues.");
			}
		}

		// If the coupon doesn't match return an appropriate error
		if (cResp == null)
		{
			return new CodedMessage(NOT_FOUND_CODE, "This coupon could not be found");
		}
		
		//Also . . . should we check to see if the campagin is active?
		
		if(cResp.getCouponOffer().getMaxRedemptions()>0)
		{
			if(cResp.getRedemptionCount()>=cResp.getCouponOffer().getMaxRedemptions())
			return new CodedMessage(OVER_MAX_CODE,"This coupon has already been redeemed "+cResp.getRedemptionCount()+" times.");
		}
		return new CodedMessage(SUCCESS,"Coupon is valid.",cResp.getActualMessage(),cResp.getCouponOffer().getOfferCode());
	}

}
