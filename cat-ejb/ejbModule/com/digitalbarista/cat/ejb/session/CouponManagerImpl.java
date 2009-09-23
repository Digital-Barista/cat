package com.digitalbarista.cat.ejb.session;

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

import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.criterion.Restrictions;
import org.jboss.annotation.ejb.LocalBinding;
import org.jboss.annotation.security.RunAsPrincipal;

import com.digitalbarista.cat.audit.AuditEvent;
import com.digitalbarista.cat.audit.AuditInterceptor;
import com.digitalbarista.cat.audit.AuditType;
import com.digitalbarista.cat.business.Coupon;
import com.digitalbarista.cat.data.CouponOfferDO;
import com.digitalbarista.cat.data.CouponRedemptionDO;
import com.digitalbarista.cat.data.CouponResponseDO;
import com.digitalbarista.cat.util.CodedMessage;

/**
 * Session Bean implementation class ClientDO
 */
@Stateless
@LocalBinding(jndiBinding = "ejb/cat/CouponManager")
@RunAsPrincipal("admin")
@RunAs("admin")
@Interceptors(AuditInterceptor.class)
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

	@TransactionAttribute(TransactionAttributeType.REQUIRED)
    @SuppressWarnings("unchecked")
    @RolesAllowed({"client","admin"})
	public CodedMessage redeemCoupon(String couponCode) {
		Criteria crit = session.createCriteria(CouponResponseDO.class);
		crit.add(Restrictions.eq("responseDetail", couponCode));
		crit.add(Restrictions.eq("responseType", CouponResponseDO.Type.Issued));
		CouponResponseDO cResp = (CouponResponseDO)crit.uniqueResult();
		
		// If the coupon doesn't match return an appropriate error
		if (cResp == null)
		{
			return new CodedMessage(NOT_FOUND_CODE, "This coupon could not be found");
		}
		
		//First do security check!
		
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
	
		return new CodedMessage(SUCCESS,"Coupon successfully redeemed.",cResp.getActualMessage());
	}

	@Override
	public List<Coupon> couponSummaryByClient(Long clientID) {
		Criteria crit = session.createCriteria(CouponOfferDO.class);
		if(clientID!=null)
		{
			crit.createAlias("campaign", "campaign");
			crit.add(Restrictions.eq("campaign.client.id", clientID));
		}
		List<Coupon> ret = new ArrayList<Coupon>();
		Coupon temp;
		for(CouponOfferDO offer : (List<CouponOfferDO>)crit.list())
		{
			temp = new Coupon();
			temp.copyFrom(offer);
			ret.add(temp);
		}
		return ret;
	}

}
