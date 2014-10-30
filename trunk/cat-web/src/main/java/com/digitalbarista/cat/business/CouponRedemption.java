package com.digitalbarista.cat.business;

import java.util.Date;

import com.digitalbarista.cat.data.CouponResponseDO.Type;

public class CouponRedemption
{
	private Date couponExpirationDate;
	private Integer couponExpirationDays;
	private Date offerUnavailableDate;
	private String couponName;
	private String offerCode;
	private Date responseDate;
	private String responseDetail;
	private Type responseType;
	private String actualMessage;
	
	public Date getCouponExpirationDate()
	{
		return couponExpirationDate;
	}
	public void setCouponExpirationDate(Date couponExpirationDate)
	{
		this.couponExpirationDate = couponExpirationDate;
	}
	public Integer getCouponExpirationDays()
	{
		return couponExpirationDays;
	}
	public void setCouponExpirationDays(Integer couponExpirationDays)
	{
		this.couponExpirationDays = couponExpirationDays;
	}
	public Date getOfferUnavailableDate()
	{
		return offerUnavailableDate;
	}
	public void setOfferUnavailableDate(Date offerUnavailableDate)
	{
		this.offerUnavailableDate = offerUnavailableDate;
	}
	public String getCouponName()
	{
		return couponName;
	}
	public void setCouponName(String couponName)
	{
		this.couponName = couponName;
	}
	public String getOfferCode()
	{
		return offerCode;
	}
	public void setOfferCode(String offerCode)
	{
		this.offerCode = offerCode;
	}
	public Date getResponseDate()
	{
		return responseDate;
	}
	public void setResponseDate(Date responseDate)
	{
		this.responseDate = responseDate;
	}
	public String getResponseDetail()
	{
		return responseDetail;
	}
	public void setResponseDetail(String responseDetail)
	{
		this.responseDetail = responseDetail;
	}
	public Type getResponseType()
	{
		return responseType;
	}
	public void setResponseType(Type responseType)
	{
		this.responseType = responseType;
	}
	public String getActualMessage()
	{
		return actualMessage;
	}
	public void setActualMessage(String actualMessage)
	{
		this.actualMessage = actualMessage;
	}
	
	
}
