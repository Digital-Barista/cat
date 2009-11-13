package com.digitalbarista.cat.ejb.session;
import java.util.List;

import javax.ejb.Local;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;

import com.digitalbarista.cat.business.Coupon;
import com.digitalbarista.cat.util.CodedMessage;

@Local
@Path("/coupons")
@Produces({"application/xml","application/json"})
@Consumes({"application/xml","application/json"})
public interface CouponManager {
    @Path("/{code}")
    @POST
	public CodedMessage redeemCoupon(@PathParam("code") String couponCode);
    @GET
	public List<Coupon> couponSummaryByClient(@QueryParam("clientid") Long clientID);
}
