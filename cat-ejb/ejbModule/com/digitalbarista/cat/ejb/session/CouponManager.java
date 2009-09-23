package com.digitalbarista.cat.ejb.session;
import java.util.List;

import javax.ejb.Local;

import com.digitalbarista.cat.business.Coupon;
import com.digitalbarista.cat.util.CodedMessage;

@Local
public interface CouponManager {
	public CodedMessage redeemCoupon(String couponCode);
	public List<Coupon> couponSummaryByClient(Long clientID);
}
