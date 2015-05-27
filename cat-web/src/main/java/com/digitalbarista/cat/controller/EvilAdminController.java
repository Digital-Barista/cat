
package com.digitalbarista.cat.controller;

import com.digitalbarista.cat.business.Contact;
import com.digitalbarista.cat.business.FacebookApp;
import com.digitalbarista.cat.business.ServiceError;
import com.digitalbarista.cat.business.ServiceResponse;
import com.digitalbarista.cat.business.criteria.ContactSearchCriteria;
import com.digitalbarista.cat.data.CampaignInfoDO;
import com.digitalbarista.cat.data.CampaignStatus;
import com.digitalbarista.cat.data.CampaignSubscriberLinkDO;
import com.digitalbarista.cat.data.EntryPointType;
import com.digitalbarista.cat.ejb.session.CampaignManager;
import com.digitalbarista.cat.ejb.session.ContactManager;
import com.digitalbarista.cat.ejb.session.FacebookManager;
import com.digitalbarista.cat.util.PagedList;
import com.digitalbarista.cat.util.SecurityUtil;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.hibernate.Criteria;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.Restrictions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/rest/appAdmin")
public class EvilAdminController {

    private Logger logger = LogManager.getLogger(getClass());

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
        if(app==null)
        {
            ret.addError(new ServiceError("The specified app could not be found!"));
            return ret;
        }
        Set<Long> clientIds = securityUtil.extractClientIds(sf.getCurrentSession());
        if(!clientIds.contains(app.getClientId()))
        {
            ret.addError(new ServiceError("You do not have permission to reset this app's welcome message state!"));
            return ret;
        }

        Criteria hibernateCrit = sf.getCurrentSession().createCriteria(CampaignInfoDO.class);
        hibernateCrit.add(Restrictions.eq("entryType", EntryPointType.Facebook));
        hibernateCrit.add(Restrictions.eq("entryAddress", app.getAppName()));
        hibernateCrit.add(Restrictions.eq("name", CampaignInfoDO.KEY_AUTO_START_NODE_UID));
        hibernateCrit.createAlias("campaign", "c");
        hibernateCrit.add(Restrictions.eq("c.status", CampaignStatus.Active));
        CampaignInfoDO cInfo = (CampaignInfoDO) hibernateCrit.uniqueResult();

        ContactSearchCriteria crit = new ContactSearchCriteria();
        List<Long> clientIDs = new ArrayList<Long>();
        clientIDs.add(app.getClientId());
        crit.setClientIds(clientIDs);
        int subscriberCount=0;
        PagedList<Contact> contacts;
        if("*".equals(contactUID) || "ALL".equals(contactUID))
        {
            subscriberCount=cInfo.getCampaign().getSubscribers().size();
            cInfo.getCampaign().getSubscribers().clear();
            contacts = contactManager.getContacts(crit, null);
        } else {
            crit.setContactUID(contactUID);
            contacts = contactManager.getContacts(crit, null);
            List<CampaignSubscriberLinkDO> toRemove = new ArrayList<CampaignSubscriberLinkDO>();
            for(CampaignSubscriberLinkDO csl : cInfo.getCampaign().getSubscribers())
            {
                if(EntryPointType.Facebook.equals(csl.getSubscriber().getType()) && contactUID.equals(csl.getSubscriber().getAddress()))
                {
                    toRemove.add(csl);
                }
            }
            subscriberCount=toRemove.size();
            cInfo.getCampaign().getSubscribers().removeAll(toRemove);
        }
        contactManager.delete(contacts.getResults());

        ret.setResult(""+subscriberCount+" subscribers were removed from the welcome campaign, and can now receive the welcome message again.");
        return ret;
    }
    
}
