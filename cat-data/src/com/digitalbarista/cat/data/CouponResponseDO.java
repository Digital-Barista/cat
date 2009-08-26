package com.digitalbarista.cat.data;

import com.digitalbarista.cat.data.CouponOfferDO;
import java.io.Serializable;
import java.lang.Long;
import java.lang.String;
import java.util.Date;
import javax.persistence.*;

/**
 * Entity implementation class for Entity: CouponResponseDO
 *
 */
@Entity
@Table(name="coupon_responses")
public class CouponResponseDO implements Serializable {

	private Long primaryKey;
	private Date responseDate;
	private String responseDetail;
	private CouponOfferDO couponOffer;
	private SubscriberDO subscriber;
	private CampaignDO campaign;
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

	@ManyToOne
	@JoinColumn(name="campaign_id")
	public CampaignDO getCampaign() {
		return campaign;
	}
	public void setCampaign(CampaignDO campaign) {
		this.campaign = campaign;
	}
}
