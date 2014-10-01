package com.digitalbarista.cat.controller;


import com.digitalbarista.cat.ejb.session.CouponManager;
import com.digitalbarista.cat.util.CouponRedemptionMessage;
import com.digitalbarista.cat.util.SecurityUtil;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.List;
import java.util.Set;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.hibernate.SessionFactory;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/home")
public class HomeController {

    @Autowired
    private HttpServletRequest request;

    @Autowired
    private CouponManager couponManager;
    
    @Autowired
    private SecurityUtil securityUtil;
    
    @Autowired
    private SessionFactory sf;
    
    //@RequestMapping(value = "/angular/app/")
    public String goToIndex(@ModelAttribute("model") ModelMap model) {
        SecurityContextHolder.getContext();
        model.addAttribute("contextPath", request.getContextPath());
        return "app";
    }

    @RequestMapping(value = "/redeemCoupon",method = RequestMethod.POST)
    public String redeemCoupon(@RequestParam("couponCode")String couponCode, Model model)
    {
        CouponRedemptionMessage message = couponManager.redeemCoupon(couponCode);
        String messageText = message.getDetailedMessage();
        if(message.getContact()!=null)
        {
            messageText +="<div><strong>Coupon Message Text:</strong></div><div style=\"margin-bottom:15px\"></div> <div id=\"fb-page-link\"><strong>Winning Facebook Page Link</strong> - <a href=\"https://www.facebook.com/profile.php?id="+message.getContact().getAddress()+"\">Cick to go to winning profile</a></div>";
        }
        model.addAttribute("message", messageText );
        return "redemption-home";
    }
    
    @RequestMapping(value = "/redeemCoupon",method = RequestMethod.GET)
    public String getCouponRedemptionPage(Model model)
    {
        return "redemption-home";
    }
    
    @RequestMapping("/luckyNumbers")
    @PreAuthorize("hasRole('ROLE_client') or hasRole('ROLE_account.manager')")
    public String getLuckyNumberCSV(HttpServletResponse response)
    {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        OutputStreamWriter writer = new OutputStreamWriter(baos);
        
        Set<Long> clientIDs = securityUtil.extractClientIds(sf.getCurrentSession());
        
        if(clientIDs.size()!=1)
        {
            throw new RuntimeException("More than one client ID.");
        }
        
        List<String> coupons = couponManager.findLuckyNumberList(clientIDs.iterator().next());
        boolean isFirst=true;
        try
        {
            for(String couponCode : coupons)
            {
                writer.write(isFirst?"":",\n");
                writer.write(couponCode);
                isFirst=false;
            }
            writer.close();
            baos.close();
            
            response.setContentType("text/csv");
            response.setContentLength(baos.toByteArray().length);
            response.setStatus(200);
            response.addHeader("Content-Disposition", "attachment; filename=lucky-numbers.csv");
            response.getOutputStream().write(baos.toByteArray());
            return null;
        }catch(IOException ex)
        {
            throw new RuntimeException("Couldn't output lucky numbers.",ex);
        }
    }
}