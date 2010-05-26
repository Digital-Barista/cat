package com.digitalbarista.cat.business;

import java.util.Date;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

import com.digitalbarista.cat.data.CouponOfferDO;
import com.digitalbarista.cat.data.CouponRedemptionDO;
import com.digitalbarista.cat.data.CouponResponseDO;

@XmlRootElement(name="Coupon")
public class Coupon implements
		BusinessObject<CouponOfferDO> {

	private String campaignName;
	private String couponName;
	private Long maxCouponCount;
	private Long maxRedemptions;
	private Date unavailableDate;
	private Long issuedCouponCount;
	private Long totalRedemptionCount;
	private Long averageRedemptionTime;
	private Date expireDate;
	private Integer expireDays;
	
	public void copyFrom(CouponOfferDO dataObject)
	{
		if (dataObject.getCampaign() != null)
			campaignName=dataObject.getCampaign().getName();
		couponName=dataObject.getCouponName();
		maxCouponCount=dataObject.getMaxCoupons();
		maxRedemptions=dataObject.getMaxRedemptions();
		unavailableDate=dataObject.getOfferUnavailableDate();
		issuedCouponCount=dataObject.getIssuedCouponCount();
		expireDate = dataObject.getCouponExpirationDate();
		expireDays = dataObject.getCouponExpirationDays();
		totalRedemptionCount=0l;
		averageRedemptionTime=0l;
		for(CouponResponseDO resp : dataObject.getResponses())
		{
			for(CouponRedemptionDO redemption : resp.getRedemptions())
			{
				totalRedemptionCount++;
				averageRedemptionTime+=(redemption.getRedemptionDate().getTime()-resp.getResponseDate().getTime());
			}
		}
		if(totalRedemptionCount>0)
			averageRedemptionTime = averageRedemptionTime / totalRedemptionCount;
		else
			averageRedemptionTime = 0l;
	}


	@Override
	public void copyTo(CouponOfferDO dataObject) {
		throw new UnsupportedOperationException("Cannot save 'Coupon' business objects, as they are merely summaries.");
	}
	
	@XmlAttribute
	public String getCampaignName() {
		return campaignName;
	}

	public void setCampaignName(String campaignName) {
		this.campaignName = campaignName;
	}

	@XmlAttribute
	public String getCouponName() {
		return couponName;
	}

	public void setCouponName(String couponName) {
		this.couponName = couponName;
	}

	@XmlAttribute
	public Long getMaxCouponCount() {
		return maxCouponCount;
	}

	public void setMaxCouponCount(Long maxCouponCount) {
		this.maxCouponCount = maxCouponCount;
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
	public Long getIssuedCouponCount() {
		return issuedCouponCount;
	}

	public void setIssuedCouponCount(Long issuedCouponCount) {
		this.issuedCouponCount = issuedCouponCount;
	}

	@XmlAttribute
	public Long getTotalRedemptionCount() {
		return totalRedemptionCount;
	}

	public void setTotalRedemptionCount(Long totalRedemptionCount) {
		this.totalRedemptionCount = totalRedemptionCount;
	}

	@XmlAttribute
	public Long getAverageRedemptionTime() {
		return averageRedemptionTime;
	}

	public void setAverageRedemptionTime(Long averageRedemptionTime) {
		this.averageRedemptionTime = averageRedemptionTime;
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
	
	
}
