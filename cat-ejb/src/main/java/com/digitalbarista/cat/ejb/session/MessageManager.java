package com.digitalbarista.cat.ejb.session;

import java.util.List;
import java.util.Map;

import javax.ejb.Local;

import com.digitalbarista.cat.business.Campaign;
import com.digitalbarista.cat.business.CampaignMessagePart;
import com.digitalbarista.cat.business.CouponNode;
import com.digitalbarista.cat.business.MessageNode;
import com.digitalbarista.cat.data.EntryPointType;


@Local
public interface MessageManager 
{
	CampaignMessagePart getMessagePart(Campaign campaign, EntryPointType entryType, String message);
	List<CampaignMessagePart> getMessageParts(Campaign campaign, MessageNode message);
    List<CampaignMessagePart> getUnavailableMessageParts(Campaign campaign, CouponNode coupon);
    List<CampaignMessagePart> getAvailableMessageParts(Campaign campaign, CouponNode coupon);
}
