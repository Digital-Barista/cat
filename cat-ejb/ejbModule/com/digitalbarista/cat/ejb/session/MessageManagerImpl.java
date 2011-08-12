package com.digitalbarista.cat.ejb.session;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;
import javax.annotation.security.PermitAll;
import javax.annotation.security.RunAs;
import javax.ejb.EJB;
import javax.ejb.SessionContext;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.hibernate.Session;
import org.jboss.annotation.ejb.LocalBinding;
import org.jboss.annotation.security.RunAsPrincipal;

import com.digitalbarista.cat.business.Campaign;
import com.digitalbarista.cat.business.CampaignMessagePart;
import com.digitalbarista.cat.business.CouponNode;
import com.digitalbarista.cat.business.MessageNode;
import com.digitalbarista.cat.data.AddInMessageDO;
import com.digitalbarista.cat.data.AddInMessageType;
import com.digitalbarista.cat.data.CampaignDO;
import com.digitalbarista.cat.data.ClientDO;
import com.digitalbarista.cat.data.EntryPointType;


/**
 * Session Bean implementation class MessageManagerImpl
 */
@Stateless
@LocalBinding(jndiBinding = "ejb/cat/MessageManager")
@TransactionAttribute(TransactionAttributeType.REQUIRED)
public class MessageManagerImpl implements MessageManager {

	public static final String CONTINUED_INDICATOR = "\n(...)";
	public static final String MESSAGE_SPLIT_INDICATOR = "<br>";
	
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
    public List<CampaignMessagePart> getMessageParts(Campaign campaign, MessageNode message)
    {
		List<CampaignMessagePart> ret = new ArrayList<CampaignMessagePart>();
		
		for (EntryPointType entryType : EntryPointType.values())
		{
			String text = message.getMessageForType(entryType);
			CampaignMessagePart part = getMessagePart(campaign, entryType, text);
			ret.add(part);
		}
		return ret;
    }
	
	@Override
	@PermitAll
    public List<CampaignMessagePart> getAvailableMessageParts(Campaign campaign, CouponNode coupon)
    {
		List<CampaignMessagePart> ret = new ArrayList<CampaignMessagePart>();
		
		for (EntryPointType entryType : EntryPointType.values())
		{
			String text = coupon.getAvailableMessageForType(entryType);
			CampaignMessagePart part = getMessagePart(campaign, entryType, text);
			ret.add(part);
		}
		return ret;
    }
	
	@Override
	@PermitAll
    public List<CampaignMessagePart> getUnavailableMessageParts(Campaign campaign, CouponNode coupon)
    {
		List<CampaignMessagePart> ret = new ArrayList<CampaignMessagePart>();
		
		for (EntryPointType entryType : EntryPointType.values())
		{
			String text = coupon.getUnavailableMessageForType(entryType);
			CampaignMessagePart part = getMessagePart(campaign, entryType, text);
			ret.add(part);
		}
		return ret;
    }
    
	@Override
	@PermitAll
	/**
	 * Get a map indexed by EntryPointType of the message(s) that should
	 * be sent including add in messages from the campaign and client. If
	 * the messages are too long for the specified EntryPointType they will
	 * be split into multiple messages and all but the last message will
	 * have CONTINUED_INDICATOR appended to it.
	 */
	public CampaignMessagePart getMessagePart(Campaign campaign, EntryPointType entryType, String message) 
	{

		String clientClientAddIn = "";
		String adminClientAddIn = "";
		String campaignAddIn = "";
		
		// Ignore campaign addin stuff if no campaign or a new campaign
		if (campaign != null &&
			campaign.getClientPK() != 0)
		{
			// Check permissions to this campaign
			if(!userManager.isUserAllowedForClientId(ctx.getCallerPrincipal().getName(), campaign.getClientPK()))
				throw new SecurityException("Current user is not allowed access to this campaign.");	
	
			
			CampaignDO campaignDO = em.find(CampaignDO.class, campaign.getPrimaryKey());
			if (campaignDO != null)
			{
				// Find campaign add in
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
			}

			ClientDO clientDO = em.find(ClientDO.class, campaign.getClientPK());
			
			// Find client add ins
			if (clientDO != null &&
					clientDO.getAddInMessages() != null)
			{
				for (AddInMessageDO addDO : clientDO.getAddInMessages())
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
		}
		
		// Build whole message
		String wholeMessage = message + campaignAddIn + clientClientAddIn + adminClientAddIn;
		
		// Create message part object to hold message parts
		CampaignMessagePart messagePart = new CampaignMessagePart();
		messagePart.setEntryType(entryType);
		messagePart.setMessages(new ArrayList<String>());
		messagePart.setMessages(splitMessage(wholeMessage, entryType.getMaxCharacters()));
			
		return messagePart;
	}
	
	private List<String> splitMessage(String message, Integer maxCharacters)
	{
		List<String> messages = new ArrayList<String>();
		
		
		// Split long messages into parts
		if (maxCharacters > 0 &&
			(message.length() > maxCharacters ||
			message.indexOf(MESSAGE_SPLIT_INDICATOR) > -1) )
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
				
				while (temp.length() + continuedIndicator.length() > maxCharacters ||
					temp.indexOf(MESSAGE_SPLIT_INDICATOR) > -1)
				{
					// Look for a split message indicator within range first
					Integer endIndex = temp.indexOf(MESSAGE_SPLIT_INDICATOR);
					if (endIndex < 0 ||
						endIndex > maxCharacters - continuedIndicator.length())
					{
						// Break the message at the earliest space
						endIndex = maxCharacters - continuedIndicator.length();
						while (endIndex > 0 &&
								!temp.substring(endIndex, endIndex + 1).matches("\\s"))
						{
								endIndex--;
						}
					}
					
					// If a space is never found break the message at the max length
					if (endIndex <= 0)
						endIndex = maxCharacters - continuedIndicator.length();
					
					String part = temp.substring(0, endIndex) + continuedIndicator;
					messages.add(part);
					temp = temp.substring(endIndex);
					
					// Left trim whitespace and force message split indicator
					String whitespaceExpression = "^(\\s+|" + MESSAGE_SPLIT_INDICATOR + ")";
					if (temp.length() > 0)
					{
						temp = temp.replaceAll(whitespaceExpression, "");
					}
					
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
