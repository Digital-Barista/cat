package com.digitalbarista.cat.data;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 * Entity implementation class for Entity: CouponOfferDO
 *
 */
@Entity
@Table(name="coupon_offers")
public class CouponOfferDO implements Serializable {

	private Long primaryKey;
	private Long maxCoupons;
	private Long issuedCouponCount;
	private Long rejectedResponseCount;
	private Date couponExpirationDate;
	private Date offerUnavailableDate;
	private static final long serialVersionUID = 1L;

	public CouponOfferDO() {
		super();
	}   

	@Id
	@Column(name="coupon_offer_id")
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
}
