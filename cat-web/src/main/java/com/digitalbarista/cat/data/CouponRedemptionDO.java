package com.digitalbarista.cat.data;

import java.io.Serializable;
import java.lang.Long;
import java.lang.String;
import java.util.Date;
import javax.persistence.*;

/**
 * Entity implementation class for Entity: CouponRedemptionDO
 *
 */
@Entity
@Table(name="coupon_redemptions")
public class CouponRedemptionDO implements Serializable {

	private Long primaryKey;
	private Date redemptionDate;
	private String redeemedByUsername;
	private CouponResponseDO couponResponse;
	private static final long serialVersionUID = 1L;

	public CouponRedemptionDO() {
		super();
	}
	
	@Id
	@Column(name="coupon_redemption_id")
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	public Long getPrimaryKey() {
		return this.primaryKey;
	}

	public void setPrimaryKey(Long primaryKey) {
		this.primaryKey = primaryKey;
	}   
	
	@Column(name="redemption_date")
	public Date getRedemptionDate() {
		return this.redemptionDate;
	}

	public void setRedemptionDate(Date redemptionDate) {
		this.redemptionDate = redemptionDate;
	}
	
	@Column(name="redeemed_by_username")
	public String getRedeemedByUsername() {
		return this.redeemedByUsername;
	}

	public void setRedeemedByUsername(String redeemedByUsername) {
		this.redeemedByUsername = redeemedByUsername;
	}

	@ManyToOne
	@JoinColumn(name="coupon_response_id")
	public CouponResponseDO getCouponResponse() {
		return couponResponse;
	}

	public void setCouponResponse(CouponResponseDO couponResponse) {
		this.couponResponse = couponResponse;
	}
   
}
