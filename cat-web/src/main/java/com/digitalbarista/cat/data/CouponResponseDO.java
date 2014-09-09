package com.digitalbarista.cat.data;

import java.io.Serializable;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;

/**
 * Entity implementation class for Entity: CouponResponseDO
 *
 */
@Entity
@Table(name="coupon_responses")
public class CouponResponseDO implements Serializable,DataObject {

	public enum Type
	{
		Expired,
		OverMax,
		Issued
	}
	
	private Long primaryKey;
	private Date responseDate;
	private String responseDetail;
	private CouponOfferDO couponOffer;
	private SubscriberDO subscriber;
	private Type responseType;
	private String actualMessage;
	private Integer redemptionCount;
	private Set<CouponRedemptionDO> redemptions = new HashSet<CouponRedemptionDO>();
	private static final long serialVersionUID = 1L;

	public CouponResponseDO() {
		super();
	}
	
	@Id
	@Column(name="coupon_response_id")
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	public Long getPrimaryKey() {
		return this.primaryKey;
	}
	public void setPrimaryKey(Long primaryKey) {
		this.primaryKey = primaryKey;
	}
	
	@Column(name="response_date")
	public Date getResponseDate() {
		return this.responseDate;
	}
	public void setResponseDate(Date responseDate) {
		this.responseDate = responseDate;
	}
	
	@Column(name="response_detail")
	public String getResponseDetail() {
		return this.responseDetail;
	}
	public void setResponseDetail(String responseDetail) {
		this.responseDetail = responseDetail;
	}
	
	@ManyToOne
	@JoinColumn(name="coupon_offer_id")
	public CouponOfferDO getCouponOffer() {
		return this.couponOffer;
	}
	public void setCouponOffer(CouponOfferDO couponOffer) {
		this.couponOffer = couponOffer;
	}

	@ManyToOne
	@JoinColumn(name="subscriber_id")
	public SubscriberDO getSubscriber() {
		return subscriber;
	}
	public void setSubscriber(SubscriberDO subscriber) {
		this.subscriber = subscriber;
	}

	@Column(name="response_type")
	@Enumerated(EnumType.STRING)
	public Type getResponseType() {
		return responseType;
	}

	public void setResponseType(Type responseType) {
		this.responseType = responseType;
	}

	@Column(name="coupon_message")
	public String getActualMessage() {
		return actualMessage;
	}

	public void setActualMessage(String actualMessage) {
		this.actualMessage = actualMessage;
	}

	@Column(name="redemption_count")
	public Integer getRedemptionCount() {
		return redemptionCount;
	}

	public void setRedemptionCount(Integer redemptionCount) {
		this.redemptionCount = redemptionCount;
	}

	@OneToMany(targetEntity=CouponRedemptionDO.class,mappedBy="couponResponse")
	public Set<CouponRedemptionDO> getRedemptions() {
		return redemptions;
	}

	public void setRedemptions(Set<CouponRedemptionDO> redemptions) {
		this.redemptions = redemptions;
	}
}
