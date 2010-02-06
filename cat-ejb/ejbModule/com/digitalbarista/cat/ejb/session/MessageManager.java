package com.digitalbarista.cat.ejb.session;

import java.util.List;

import javax.ejb.Local;

import com.digitalbarista.cat.business.Campaign;
import com.digitalbarista.cat.business.CampaignMessagePart;


@Local
public interface MessageManager 
{
	List<CampaignMessagePart> getMessageParts(Campaign campaign, String message);
}
