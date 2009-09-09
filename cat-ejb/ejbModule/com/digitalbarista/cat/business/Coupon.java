package com.digitalbarista.cat.business;

import java.util.Date;

import com.digitalbarista.cat.data.CouponOfferDO;
import com.digitalbarista.cat.data.CouponRedemptionDO;
import com.digitalbarista.cat.data.CouponResponseDO;

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
	
	public void copyFrom(CouponOfferDO dataObject)
	{
		if (dataObject.getCampaign() != null)
			campaignName=dataObject.getCampaign().getName();
		couponName=dataObject.getCouponName();
		maxCouponCount=dataObject.getMaxCoupons();
		maxRedemptions=dataObject.getMaxRedemptions();
		unavailableDate=dataObject.getOfferUnavailableDate();
		issuedCouponCount=dataObject.getIssuedCouponCount();
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
	
	public String getCampaignName() {
		return campaignName;
	}

	public void setCampaignName(String campaignName) {
		this.campaignName = campaignName;
	}

	public String getCouponName() {
		return couponName;
	}

	public void setCouponName(String couponName) {
		this.couponName = couponName;
	}

	public Long getMaxCouponCount() {
		return maxCouponCount;
	}

	public void setMaxCouponCount(Long maxCouponCount) {
		this.maxCouponCount = maxCouponCount;
	}

	public Long getMaxRedemptions() {
		return maxRedemptions;
	}

	public void setMaxRedemptions(Long maxRedemptions) {
		this.maxRedemptions = maxRedemptions;
	}

	public Date getUnavailableDate() {
		return unavailableDate;
	}

	public void setUnavailableDate(Date unavailableDate) {
		this.unavailableDate = unavailableDate;
	}

	public Long getIssuedCouponCount() {
		return issuedCouponCount;
	}

	public void setIssuedCouponCount(Long issuedCouponCount) {
		this.issuedCouponCount = issuedCouponCount;
	}

	public Long getTotalRedemptionCount() {
		return totalRedemptionCount;
	}

	public void setTotalRedemptionCount(Long totalRedemptionCount) {
		this.totalRedemptionCount = totalRedemptionCount;
	}

	public Long getAverageRedemptionTime() {
		return averageRedemptionTime;
	}

	public void setAverageRedemptionTime(Long averageRedemptionTime) {
		this.averageRedemptionTime = averageRedemptionTime;
	}
}
