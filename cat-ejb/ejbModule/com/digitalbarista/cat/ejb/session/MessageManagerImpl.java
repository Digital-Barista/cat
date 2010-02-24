package com.digitalbarista.cat.ejb.session;

import java.util.ArrayList;
import java.util.List;

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
			messagePart.setMessages(splitMessage(wholeMessage, entryType.getMaxCharacters()));
			
			
		}
		
		return ret;
	}
	
	private List<String> splitMessage(String message, Integer maxCharacters)
	{
		List<String> messages = new ArrayList<String>();
		
		
		// Split long messages into parts
		if (maxCharacters > 0 &&
			message.length() > maxCharacters)
		{

			String continuedIndicator = "";
			String messageCountPlaceHolder = "";
			do
			{
				// Build place holder with proper number of characters
				messageCountPlaceHolder = "";
				for (Integer i = 0; i < getNumDigits(messages.size()); i++)
					messageCountPlaceHolder += "|";
				
				messages.clear();
				String temp = message;
				Integer messageCount = 1;
				continuedIndicator = "\n(" + messageCount + "/" + messageCountPlaceHolder + ")";
				
				while (temp.length() + continuedIndicator.length() > maxCharacters)
				{
					// Break the message at the earliest space
					Integer endIndex = maxCharacters - continuedIndicator.length();
					while (endIndex > 0 &&
							!temp.substring(endIndex, endIndex + 1).matches("\\s") )
							endIndex--;
						  
					
					String part = temp.substring(0, endIndex) + continuedIndicator;
					messages.add(part);
					temp = temp.substring(endIndex);
					
					// Left trim whitespace
					String whitespaceExpression = "^\\s+";
					if (temp.length() > 0)
						temp = temp.replaceAll(whitespaceExpression, "");
					
					// Increment count
					messageCount++;
					continuedIndicator = "\n(" + messageCount + "/" + messageCountPlaceHolder + ")";
				}
				
				if (temp.length() > 0)
					messages.add(temp + continuedIndicator);
			}while(getNumDigits(messages.size()) > messageCountPlaceHolder.length());
			
			// Replace all message count place holders with the actual count
			for (int i = 0; i < messages.size(); i++)
				messages.set(i, messages.get(i).replaceAll("/\\|+\\)$", "/" + ((Integer)messages.size()).toString()) + ")" );
				
		}
		else
		{
			messages.add(message);
		}
		
		return messages;
	}
	
	private Integer getNumDigits(Integer value)
	{
		Integer ret = value.toString().replace("-", "").length();
		return ret;
	}
}
