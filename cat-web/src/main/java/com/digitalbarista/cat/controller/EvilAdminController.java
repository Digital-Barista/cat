
package com.digitalbarista.cat.controller;

import com.digitalbarista.cat.business.Contact;
import com.digitalbarista.cat.business.FacebookApp;
import com.digitalbarista.cat.business.ServiceError;
import com.digitalbarista.cat.business.ServiceResponse;
import com.digitalbarista.cat.business.criteria.ContactSearchCriteria;
import com.digitalbarista.cat.data.CampaignDO;
import com.digitalbarista.cat.ejb.session.CampaignManager;
import com.digitalbarista.cat.ejb.session.ContactManager;
import com.digitalbarista.cat.ejb.session.FacebookManager;
import com.digitalbarista.cat.util.PagedList;
import com.digitalbarista.cat.util.SecurityUtil;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import org.hibernate.Query;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/rest/appAdmin")
public class EvilAdminController {
    
    @Autowired
    FacebookManager fbManager;

    @Autowired
    CampaignManager campaignManager;
    
    @Autowired
    ContactManager contactManager;
    
    @Autowired
    SecurityUtil securityUtil;
    
    @Autowired
    SessionFactory sf;
    
    @RequestMapping("/clearWelcomes")
    @Transactional(propagation = Propagation.REQUIRED)
    public ServiceResponse clearWelcomes(@RequestParam(value="appName",required=true) String appName,
                                         @RequestParam(value="contactUID",required=true) String contactUID)
    {
        ServiceResponse ret = new ServiceResponse();
        FacebookApp app = fbManager.findFacebookAppByName(appName);
        Set<Long> clientIds = securityUtil.extractClientIds(sf.getCurrentSession());
        if(!clientIds.contains(app.getClientId()))
        {
            ret.addError(new ServiceError("You do not have permission to reset this app's welcome message state!"));
            return ret;
        }
        Query q = sf.getCurrentSession().getNamedQuery("entry.campaign");
        q.setParameter("clientId", app.getClientId());
        CampaignDO camp = (CampaignDO)q.uniqueResult();

        ContactSearchCriteria crit = new ContactSearchCriteria();
        List<Long> clientIDs = new ArrayList<Long>();
        clientIDs.add(app.getClientId());
        crit.setClientIds(clientIDs);
        if("*".equals(contactUID) || "ALL".equals(contactUID))
        {
            //noop for now.  Find them all.
        } else {
            crit.setContactUID(contactUID);
        }
        PagedList<Contact> contacts = contactManager.getContacts(crit, null);
        contactManager.delete(contacts.getResults());
        
        int subscriberCount = camp.getSubscribers().size();
        camp.getSubscribers().clear();
        ret.setResult(""+subscriberCount+" subscribers were removed from the welcome campaign, and can now receive the welcome message again.");
        return ret;
    }
    
}
