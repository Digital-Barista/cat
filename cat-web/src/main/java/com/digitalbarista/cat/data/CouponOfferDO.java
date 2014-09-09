package com.digitalbarista.cat.data;

import java.io.Serializable;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;

/**
 * Entity implementation class for Entity: CouponOfferDO
 *
 */
@Entity
@Table(name="coupon_offers")
public class CouponOfferDO implements Serializable,DataObject {

	private Long primaryKey;
	private Long maxCoupons=0l;
	private Long issuedCouponCount=0l;
	private Long rejectedResponseCount=0l;
	private Long maxRedemptions;
	private Date couponExpirationDate;
	private Integer couponExpirationDays;
	private Date offerUnavailableDate;
	private String couponName;
	private String nodeUID;
	private String offerCode;
	private CampaignDO campaign;
	private Set<CouponResponseDO> responses = new HashSet<CouponResponseDO>();
	
	private static final long serialVersionUID = 1L;

	public CouponOfferDO() {
		super();
	}   

	@Id
	@Column(name="coupon_offer_id")
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	public Long getPrimaryKey() {
		return this.primaryKey;
	}
	public void setPrimaryKey(Long primaryKey) {
		this.primaryKey = primaryKey;
	}
	
	@Column(name="max_coupons_issued")
	public Long getMaxCoupons() {
		return this.maxCoupons;
	}
	public void setMaxCoupons(Long maxCoupons) {
		this.maxCoupons = maxCoupons;
	}
	
	@Column(name="coupon_issue_count")
	public Long getIssuedCouponCount() {
		return this.issuedCouponCount;
	}
	public void setIssuedCouponCount(Long issuedCouponCount) {
		this.issuedCouponCount = issuedCouponCount;
	}
	
	@Column(name="rejected_response_count")
	public Long getRejectedResponseCount() {
		return this.rejectedResponseCount;
	}
	public void setRejectedResponseCount(Long rejectedResponseCount) {
		this.rejectedResponseCount = rejectedResponseCount;
	}
	
	@Column(name="expiration_date")
	public Date getCouponExpirationDate() {
		return this.couponExpirationDate;
	}
	public void setCouponExpirationDate(Date couponExpirationDate) {
		this.couponExpirationDate = couponExpirationDate;
	}
	
	@Column(name="unavailable_date")
	public Date getOfferUnavailableDate() {
		return this.offerUnavailableDate;
	}
	public void setOfferUnavailableDate(Date offerUnavailableDate) {
		this.offerUnavailableDate = offerUnavailableDate;
	}

	@Column(name="node_uid")
	public String getNodeUID() {
		return nodeUID;
	}
	public void setNodeUID(String nodeUID) {
		this.nodeUID = nodeUID;
	}

	@Column(name="max_redemptions")
	public Long getMaxRedemptions() {
		return maxRedemptions;
	}

	public void setMaxRedemptions(Long maxRedemptions) {
		this.maxRedemptions = maxRedemptions;
	}

	@Column(name="coupon_name")
	public String getCouponName() {
		return couponName;
	}

	public void setCouponName(String couponName) {
		this.couponName = couponName;
	}
	
	@ManyToOne
	@JoinColumn(name="campaign_id")
	public CampaignDO getCampaign() {
		return campaign;
	}
	public void setCampaign(CampaignDO campaign) {
		this.campaign = campaign;
	}

	@OneToMany(targetEntity=CouponResponseDO.class,mappedBy="couponOffer")
	public Set<CouponResponseDO> getResponses() {
		return responses;
	}

	public void setResponses(Set<CouponResponseDO> responses) {
		this.responses = responses;
	}

	@Column(name="expiration_days")
	public Integer getCouponExpirationDays() {
		return couponExpirationDays;
	}

	public void setCouponExpirationDays(Integer couponExpirationDays) {
		this.couponExpirationDays = couponExpirationDays;
	}

	@Column(name="offer_code")
	public String getOfferCode() {
		return offerCode;
	}

	public void setOfferCode(String offerCode) {
		this.offerCode = offerCode;
	}
}
