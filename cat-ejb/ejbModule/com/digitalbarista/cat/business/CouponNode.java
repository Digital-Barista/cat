package com.digitalbarista.cat.business;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.persistence.EntityManager;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import org.apache.log4j.LogManager;

import com.digitalbarista.cat.audit.Auditable;
import com.digitalbarista.cat.data.CouponOfferDO;
import com.digitalbarista.cat.data.NodeDO;
import com.digitalbarista.cat.data.NodeInfoDO;
import com.digitalbarista.cat.data.NodeType;

@XmlRootElement(name="CouponNode")
@XmlType(name="CouponNode")
public class CouponNode extends Node implements Auditable {

	private static String dateFormat = "MM/dd/yyyy HH:mm:ss";

	private static final String INFO_PROPERTY_COUPON_ID="CouponId";
	private static final String INFO_PROPERTY_AVAILABLE_MESSAGE="AvailableMessage";
	private static final String INFO_PROPERTY_UNAVAILABLE_MESSAGE="UnavailableMsg";
	private static final String INFO_PROPERTY_MAX_COUPONS="MaxCoupons";
	private static final String INFO_PROPERTY_MAX_REDEMPTIONS="MaxRedemptions";
	private static final String INFO_PROPERTY_UNAVAILABLE_DATE="UnavailableDate";
	private static final String INFO_PROPERTY_COUPON_CODE="CouponCode";
	private static final String INFO_PROPERTY_EXPIRATION_DATE="ExpirationDate";
	private static final String INFO_PROPERTY_EXPIRATION_DAYS="ExpirationDays";
	private static final String INFO_PROPERTY_OFFER_CODE="OfferCode";
	
	public static final Long INFINITE_REDEMPTION_COUNT = -1l;
	public static final Long INFINITE_COUPONS_COUNT = -1l;
		
	public static final String START_COUPON_CODE = "{";
	public static final String END_COUPON_CODE = "}";
	
	public static final Integer RANDOM_CODE_LENGTH = 5;

	private Long couponId=null;
	private String availableMessage = START_COUPON_CODE + END_COUPON_CODE;
	private String unavailableMessage=null;
	private Long maxCoupons = INFINITE_COUPONS_COUNT;
	private Long maxRedemptions = INFINITE_REDEMPTION_COUNT;
	private Date unavailableDate = null;
	private String couponCode = null;
	private Date expireDate = null;
	private Integer expireDays = null;
	private String offerCode = null;
	
	@Override
	public void copyFrom(NodeDO dataObject, Integer version) {
		super.copyFrom(dataObject, version);
		for(NodeInfoDO ni : dataObject.getNodeInfo())
		{
			if(!ni.getVersion().equals(version))
				continue;
			
			try
			{
				if(ni.getName().equals(INFO_PROPERTY_COUPON_ID) && ni.getValue()!=null)
					couponId = new Long(ni.getValue());
	
				else if(ni.getName().equals(INFO_PROPERTY_AVAILABLE_MESSAGE))
					availableMessage = ni.getValue();
	
				else if(ni.getName().equals(INFO_PROPERTY_UNAVAILABLE_MESSAGE))
					unavailableMessage = ni.getValue();
	
				else if(ni.getName().equals(INFO_PROPERTY_MAX_COUPONS) && ni.getValue()!=null)
					maxCoupons = new Long(ni.getValue());
	
				else if(ni.getName().equals(INFO_PROPERTY_MAX_REDEMPTIONS) && ni.getValue()!=null)
					maxRedemptions = new Long(ni.getValue());
	
				else if(ni.getName().equals(INFO_PROPERTY_UNAVAILABLE_DATE) && ni.getValue()!=null)
					unavailableDate = new SimpleDateFormat(dateFormat).parse(ni.getValue());
	
				else if(ni.getName().equals(INFO_PROPERTY_COUPON_CODE))
					couponCode = ni.getValue();

				else if(ni.getName().equals(INFO_PROPERTY_EXPIRATION_DATE) && ni.getValue()!=null)
					expireDate = new SimpleDateFormat(dateFormat).parse(ni.getValue());

				else if(ni.getName().equals(INFO_PROPERTY_EXPIRATION_DAYS))
					expireDays = new Integer(ni.getValue());
	
				else if(ni.getName().equals(INFO_PROPERTY_OFFER_CODE))
					offerCode = ni.getValue();
	
			}
			catch(ParseException e)
			{
				LogManager.getLogger(getClass()).error("Error parsing the date stored in the database.  '"+ni.getValue()+"'");
			}
			catch(NumberFormatException e)
			{
				LogManager.getLogger(getClass()).error("Error parsing the number date stored in the database.  "+ni.getName()+"='"+ni.getValue()+"'");
			}
		}
	}

	@Override
	public void copyTo(NodeDO dataObject) {
		super.copyTo(dataObject);
		Integer version = dataObject.getCampaign().getCurrentVersion();
		Map<String,NodeInfoDO> nodes = new HashMap<String,NodeInfoDO>();
		for(NodeInfoDO ni : dataObject.getNodeInfo())
		{
			if(!ni.getVersion().equals(version))
				continue;
			nodes.put(ni.getName(), ni);
		}
		
		if(couponId!=null)
		{
			if(nodes.containsKey(INFO_PROPERTY_COUPON_ID))
				nodes.get(INFO_PROPERTY_COUPON_ID).setValue(couponId.toString());
			else
				buildAndAddNodeInfo(dataObject, INFO_PROPERTY_COUPON_ID, couponId.toString(), version);
		}

		if(availableMessage!=null)
		{
			if(nodes.containsKey(INFO_PROPERTY_AVAILABLE_MESSAGE))
				nodes.get(INFO_PROPERTY_AVAILABLE_MESSAGE).setValue(availableMessage);
			else
				buildAndAddNodeInfo(dataObject, INFO_PROPERTY_AVAILABLE_MESSAGE, availableMessage, version);
		}

		if(unavailableMessage!=null)
		{
			if(nodes.containsKey(INFO_PROPERTY_UNAVAILABLE_MESSAGE))
				nodes.get(INFO_PROPERTY_UNAVAILABLE_MESSAGE).setValue(unavailableMessage);
			else
				buildAndAddNodeInfo(dataObject, INFO_PROPERTY_UNAVAILABLE_MESSAGE, unavailableMessage, version);
		}

		if(maxCoupons!=null)
		{
			if(nodes.containsKey(INFO_PROPERTY_MAX_COUPONS))
				nodes.get(INFO_PROPERTY_MAX_COUPONS).setValue(maxCoupons.toString());
			else
				buildAndAddNodeInfo(dataObject, INFO_PROPERTY_MAX_COUPONS, maxCoupons.toString(), version);
		}

		if(maxRedemptions!=null)
		{
			if(nodes.containsKey(INFO_PROPERTY_MAX_REDEMPTIONS))
				nodes.get(INFO_PROPERTY_MAX_REDEMPTIONS).setValue(maxRedemptions.toString());
			else
				buildAndAddNodeInfo(dataObject, INFO_PROPERTY_MAX_REDEMPTIONS, maxRedemptions.toString(), version);
		}

		if(unavailableDate!=null)
		{
			if(nodes.containsKey(INFO_PROPERTY_UNAVAILABLE_DATE))
				nodes.get(INFO_PROPERTY_UNAVAILABLE_DATE).setValue(new SimpleDateFormat(dateFormat).format(unavailableDate));
			else
				buildAndAddNodeInfo(dataObject, INFO_PROPERTY_UNAVAILABLE_DATE, new SimpleDateFormat(dateFormat).format(unavailableDate), version);
		}

		if(couponCode!=null)
		{
			if(nodes.containsKey(INFO_PROPERTY_COUPON_CODE))
				nodes.get(INFO_PROPERTY_COUPON_CODE).setValue(couponCode);
			else
				buildAndAddNodeInfo(dataObject, INFO_PROPERTY_COUPON_CODE, couponCode, version);
		}

		if(expireDate != null)
		{
			if(nodes.containsKey(INFO_PROPERTY_EXPIRATION_DATE))
				nodes.get(INFO_PROPERTY_EXPIRATION_DATE).setValue(new SimpleDateFormat(dateFormat).format(expireDate));
			else
				buildAndAddNodeInfo(dataObject, INFO_PROPERTY_EXPIRATION_DATE, new SimpleDateFormat(dateFormat).format(expireDate), version);
			
			if (nodes.containsKey(INFO_PROPERTY_EXPIRATION_DAYS))
				dataObject.getNodeInfo().remove(nodes.get(INFO_PROPERTY_EXPIRATION_DAYS));
		}

		if(expireDays != null)
		{
			if(nodes.containsKey(INFO_PROPERTY_EXPIRATION_DAYS))
				nodes.get(INFO_PROPERTY_EXPIRATION_DAYS).setValue(expireDays.toString());
			else
				buildAndAddNodeInfo(dataObject, INFO_PROPERTY_EXPIRATION_DAYS, expireDays.toString(), version);

			if (nodes.containsKey(INFO_PROPERTY_EXPIRATION_DATE))
				dataObject.getNodeInfo().remove(nodes.remove(INFO_PROPERTY_EXPIRATION_DATE));
		}

		if(offerCode != null)
		{
			if(nodes.containsKey(INFO_PROPERTY_OFFER_CODE))
				nodes.get(INFO_PROPERTY_OFFER_CODE).setValue(offerCode);
			else
				buildAndAddNodeInfo(dataObject, INFO_PROPERTY_OFFER_CODE, offerCode, version);
		}
	}

	@Override
	public String auditString() {
		StringBuffer ret = new StringBuffer();
		ret.append("type:"+getType().toString());
		ret.append(";couponId:"+getCouponId());
		ret.append(";availableMessage:"+getAvailableMessage());
		ret.append(";unavailableMessage:"+getUnavailableMessage());
		ret.append(";maxCoupons:"+getMaxCoupons());
		ret.append(";maxRedemptions:"+getMaxRedemptions());
		if(getUnavailableDate()==null)
			ret.append(";unavailableDate:null");
		else
			ret.append(";unavailableDate:"+new SimpleDateFormat(dateFormat).format(getUnavailableDate()));

		ret.append(";expirationDays:"+getExpireDays());
		if(getExpireDate()==null)
			ret.append(";expirationDate:null");
		else
			ret.append(";expirationDate:"+new SimpleDateFormat(dateFormat).format(getExpireDate()));
		
		ret.append(";couponCode:"+getCouponCode());
		ret.append(";offerCode:"+getOfferCode());
		ret.append(";name:"+getName());
		ret.append(";UID:"+getUid());
		ret.append(";campaign:"+getCampaignUID());
		return ret.toString();
	}


	@Override
	public NodeType getType() {
		return NodeType.Coupon;
	}

	@XmlAttribute
	public Long getCouponId() {
		return couponId;
	}

	public void associateCouponOffer(CouponOfferDO offer)
	{
		couponId = offer.getPrimaryKey();
	}
	
	@XmlAttribute
	public String getAvailableMessage() {
		return availableMessage;
	}

	public void setAvailableMessage(String availableMessage) {
		this.availableMessage = availableMessage;
	}

	@XmlAttribute
	public String getUnavailableMessage() {
		return unavailableMessage;
	}

	public void setUnavailableMessage(String unavailableMessage) {
		this.unavailableMessage = unavailableMessage;
	}

	@XmlAttribute
	public Long getMaxCoupons() {
		return maxCoupons;
	}

	public void setMaxCoupons(Long maxCoupons) {
		this.maxCoupons = maxCoupons;
	}

	@XmlAttribute
	public Long getMaxRedemptions() {
		return maxRedemptions;
	}

	public void setMaxRedemptions(Long maxRedemptions) {
		this.maxRedemptions = maxRedemptions;
	}

	@XmlAttribute
	public Date getUnavailableDate() {
		return unavailableDate;
	}

	public void setUnavailableDate(Date unavailableDate) {
		this.unavailableDate = unavailableDate;
	}

	@XmlAttribute
	public String getCouponCode() {
		return couponCode;
	}

	public void setCouponCode(String couponCode) {
		this.couponCode = couponCode;
	}

	@XmlAttribute
	public Date getExpireDate() {
		return expireDate;
	}

	public void setExpireDate(Date expireDate) {
		this.expireDate = expireDate;
	}


	@XmlAttribute
	public Integer getExpireDays() {
		return expireDays;
	}

	public void setExpireDays(Integer expireDays) {
		this.expireDays = expireDays;
	}

	@XmlAttribute
	public String getOfferCode() {
		return offerCode;
	}

	public void setOfferCode(String offerCode) {
		this.offerCode = offerCode;
	}
}
