package com.digitalbarista.cat.business;

import java.util.Calendar;
import java.util.HashSet;
import java.util.Set;

import javax.xml.bind.annotation.XmlRootElement;

import com.digitalbarista.cat.data.ContactDO;
import com.digitalbarista.cat.data.ContactTagLinkDO;
import com.digitalbarista.cat.data.EntryPointType;

@XmlRootElement
public class Contact implements BusinessObject<ContactDO>, Comparable<Contact>
{

	private Long contactId;
	private String address;
	private Calendar createDate;
	private Set<ContactTag> contactTags;
	private Long clientId;
	private EntryPointType type;
	private boolean blacklisted;
	private String UID;
	private Set<ContactInfo> contactInfos = new HashSet<ContactInfo>();
	private Set<CouponRedemption> couponRedemptions = new HashSet<CouponRedemption>();
	private Set<Campaign> subscribedCampaigns = new HashSet<Campaign>();
	
	
	public Long getClientId() {
		return clientId;
	}
	public void setClientId(Long clientId) {
		this.clientId = clientId;
	}
	public Long getContactId() {
		return contactId;
	}
	public void setContactId(Long contactId) {
		this.contactId = contactId;
	}
	public String getAddress() {
		return address;
	}
	public void setAddress(String address) {
		this.address = address;
	}
	public Calendar getCreateDate() {
		return createDate;
	}
	public void setCreateDate(Calendar createDate) {
		this.createDate = createDate;
	}
	public Set<ContactTag> getContactTags() {
		return contactTags;
	}
	public void setContactTags(Set<ContactTag> contactTags) {
		this.contactTags = contactTags;
	}
	public String getUID() {
		return UID;
	}
	public void setUID(String uID) {
		UID = uID;
	}
	public EntryPointType getType() {
		return type;
	}
	public void setType(EntryPointType type) {
		this.type = type;
	}
	
	
	public boolean getBlacklisted() {
		return blacklisted;
	}
	public void setBlacklisted(boolean blacklisted) {
		this.blacklisted = blacklisted;
	}
	
	public Set<ContactInfo> getContactInfos()
	{
		return contactInfos;
	}
	public void setContactInfos(Set<ContactInfo> contactInfos)
	{
		this.contactInfos = contactInfos;
	}
	
	public Set<CouponRedemption> getCouponRedemptions()
	{
		return couponRedemptions;
	}
	public void setCouponRedemptions(Set<CouponRedemption> couponRedemptions)
	{
		this.couponRedemptions = couponRedemptions;
	}
	
	public Set<Campaign> getSubscribedCampaigns()
	{
		return subscribedCampaigns;
	}
	public void setSubscribedCampaigns(Set<Campaign> subscribedCampaigns)
	{
		this.subscribedCampaigns = subscribedCampaigns;
	}
	@Override
	public void copyFrom(ContactDO dataObject) {
		contactId = dataObject.getContactId();
		address = dataObject.getAddress();
		if(address==null)
			address = dataObject.getAlternateId();
		createDate = dataObject.getCreateDate();
		clientId = dataObject.getClient().getPrimaryKey();
		type = dataObject.getType();
		blacklisted = dataObject.getBlacklist() != null;
		UID = dataObject.getUID();
		
		contactTags = new HashSet<ContactTag>();
		if (dataObject.getContactTags() != null)
		{
			for (ContactTagLinkDO tag : dataObject.getContactTags())
			{
				ContactTag temp = new ContactTag();
				temp.copyFrom(tag.getTag());
				temp.setTagDate(tag.getInitialTagDate());
				contactTags.add(temp);
			}
		}
	}
	@Override
	public void copyTo(ContactDO dataObject) {
		dataObject.setContactId(contactId);
		dataObject.setAddress(address);
		dataObject.setCreateDate(createDate);
		dataObject.setType(type);
	}
	@Override
	public int compareTo(Contact o) {
		return getType().compareTo(o.getType());
	}
	
	
}
