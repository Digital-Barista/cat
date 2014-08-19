package com.digitalbarista.cat.controller;

import com.digitalbarista.cat.business.*;
import com.digitalbarista.cat.ejb.session.CampaignManager;
import com.digitalbarista.cat.ejb.session.CouponManager;
import com.digitalbarista.cat.util.CouponRedemptionMessage;
import edu.emory.mathcs.backport.java.util.Arrays;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jettison.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@Controller
public class CouponController
{
    @Autowired
    private CouponManager couponManager;

    @RequestMapping(value = "/data/coupon")
    public ServiceResponse listCoupons(@RequestParam(required = false) Integer offset,
                                       @RequestParam(required = false) Integer limit,
                                       @RequestParam(required = false) String sort,
                                       @RequestParam(required = false) Integer direction)
    {
        ServiceMetadata meta = new ServiceMetadata(limit, offset, sort, direction);
        ServiceResponse ret = couponManager.getCouponSummaries(null, meta);
        return ret;
    }
    @RequestMapping(value = "/data/coupon/{couponCode}")
    public ServiceResponse redeemCoupon(@PathVariable String couponCode)
    {
        ServiceResponse ret = new ServiceResponse();
        CouponRedemptionMessage redemption = couponManager.redeemCoupon(couponCode);
        ret.setResult(redemption);
        return ret;
    }

}
