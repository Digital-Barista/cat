package com.digitalbarista.cat.ejb.session;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.annotation.Resource;
import javax.annotation.security.PermitAll;
import javax.annotation.security.RunAs;
import javax.ejb.EJB;
import javax.ejb.SessionContext;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.hibernate.Session;
import org.jboss.annotation.ejb.LocalBinding;
import org.jboss.annotation.security.RunAsPrincipal;

import com.digitalbarista.cat.business.Campaign;
import com.digitalbarista.cat.business.CampaignMessagePart;
import com.digitalbarista.cat.data.AddInMessageDO;
import com.digitalbarista.cat.data.AddInMessageType;
import com.digitalbarista.cat.data.CampaignDO;
import com.digitalbarista.cat.data.EntryPointType;


/**
 * Session Bean implementation class MessageManagerImpl
 */
@Stateless
@LocalBinding(jndiBinding = "ejb/cat/MessageManager")
@RunAsPrincipal("admin")
@RunAs("admin")
public class MessageManagerImpl implements MessageManager {

	public static final String CONTINUED_INDICATOR = "\n(...)";
	
	@Resource
	private SessionContext ctx; 

	@PersistenceContext(unitName="cat-data")
	private EntityManager em;
	
	@PersistenceContext(unitName="cat-data")
	private Session session;
	

	@EJB(name="ejb/cat/UserManager")
	UserManager userManager;



	@Override
	@PermitAll
	/**
	 * Get a map indexed by EntryPointType of the message(s) that should
	 * be sent including add in messages from the campaign and client. If
	 * the messages are too long for the specified EntryPointType they will
	 * be split into multiple messages and all but the last message will
	 * have CONTINUED_INDICATOR appended to it.
	 */
	public List<CampaignMessagePart> getMessageParts(Campaign campaign, String message) {

		List<CampaignMessagePart> ret = new ArrayList<CampaignMessagePart>();
		
		// Check permissions to this campaign
		if(!userManager.isUserAllowedForClientId(ctx.getCallerPrincipal().getName(), campaign.getClientPK()))
			throw new SecurityException("Current user is not allowed access to this campaign.");	
		
		CampaignDO campaignDO = em.find(CampaignDO.class, campaign.getPrimaryKey());
		if (campaignDO == null)
			throw new IllegalArgumentException("The specified campaign does not exist");
		
		// Iterate through each entry point type
		for (EntryPointType entryType : EntryPointType.values())
		{
			// Find campaign add in
			String campaignAddIn = "";
			if (campaignDO.getAddInMessages() != null)
			{
				for (AddInMessageDO addDO : campaignDO.getAddInMessages())
				{
					if (addDO.getEntryType() == entryType)
					{
						if (addDO.getType() == AddInMessageType.CLIENT)
							campaignAddIn = addDO.getMessage();
					}
				}
			}

			// Find client add ins
			String clientClientAddIn = "";
			String adminClientAddIn = "";
			if (campaignDO.getClient().getAddInMessages() != null)
			{
				for (AddInMessageDO addDO : campaignDO.getClient().getAddInMessages())
				{
					if (addDO.getEntryType() == entryType)
					{
						if (addDO.getType() == AddInMessageType.ADMIN)
							adminClientAddIn = addDO.getMessage();
						else if (addDO.getType() == AddInMessageType.CLIENT)
							clientClientAddIn = addDO.getMessage();
					}
				}
			}
			
			// Build whole message
			String wholeMessage = message + campaignAddIn + clientClientAddIn + adminClientAddIn;
			
			// Create message part object to hold message parts
			CampaignMessagePart messagePart = new CampaignMessagePart();
			messagePart.setEntryType(entryType);
			messagePart.setMessages(new ArrayList<String>());
			ret.add(messagePart);
			
			// Whitespace characters we care about
			ArrayList<Character> white = new ArrayList<Character>();
			white.add(' ');
			white.add('\t');
			white.add('\n');
			
			// Split long messages into parts
			if (entryType.getMaxCharacters() > 0 &&
				wholeMessage.length() > entryType.getMaxCharacters())
			{
				String temp = wholeMessage;
				
				while (temp.length() > entryType.getMaxCharacters())
				{
					// Break the message at the earliest space
					Integer endIndex = entryType.getMaxCharacters() - CONTINUED_INDICATOR.length();
					while (endIndex > 0 &&
							!white.contains(temp.charAt(endIndex)) )
							endIndex--;
						  
					
					String part = temp.substring(0, endIndex) + CONTINUED_INDICATOR;
					messagePart.getMessages().add(part);
					temp = temp.substring(endIndex);
					
					// If first character is whitespace remove it
					if (temp.length() > 0 &&
						white.contains(temp.charAt(0)) )
						temp = temp.substring(1);
				}
				
				if (temp.length() > 0)
					messagePart.getMessages().add(temp);
			}
			else
			{
				messagePart.getMessages().add(wholeMessage);
			}
		}
		
		return ret;
	}
}
