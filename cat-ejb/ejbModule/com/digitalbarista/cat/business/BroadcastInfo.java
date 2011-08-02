package com.digitalbarista.cat.business;

import com.digitalbarista.cat.data.CampaignDO;
import com.digitalbarista.cat.data.CampaignMode;
import com.digitalbarista.cat.data.CampaignNodeLinkDO;
import com.digitalbarista.cat.data.NodeType;

public class BroadcastInfo implements BusinessObject<CampaignDO> {

	private Integer subscriberCount;
	private String title;
	private String message;
	private Integer couponRedemptionCount;
	private boolean isCoupon = false;
	private Long couponId;
	
	@Override
	public void copyFrom(CampaignDO dataObject) {
		if(dataObject.getMode() != CampaignMode.Broadcast)
			throw new IllegalArgumentException("Cannot generate broadcast info on a non-broadcast campaign.");
		title=dataObject.getName();
		for(CampaignNodeLinkDO nodeLink : dataObject.getNodes())
		{
			switch(nodeLink.getNode().getType())
			{
				case Message:
				{
					couponRedemptionCount=0;
					MessageNode node = new MessageNode();
					node.copyFrom(nodeLink.getNode());
					message = node.getMessage();
					isCoupon=false;
					break;
				}
				case Coupon:
				{
					CouponNode node = new CouponNode();
					node.copyFrom(nodeLink.getNode());
					message = node.getAvailableMessage();
					isCoupon=true;
					couponId=node.getCouponId();
					break;
				}
			}
		}
	}

	@Override
	public void copyTo(CampaignDO dataObject) {
	}

	public Integer getSubscriberCount() {
		return subscriberCount;
	}

	public void setSubscriberCount(Integer subscriberCount) {
		this.subscriberCount = subscriberCount;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public Integer getCouponRedemptionCount() {
		return couponRedemptionCount;
	}

	public void setCouponRedemptionCount(Integer couponRedemptionCount) {
		this.couponRedemptionCount = couponRedemptionCount;
	}

	public boolean isCoupon() {
		return isCoupon;
	}

	public void setCoupon(boolean isCoupon) {
		this.isCoupon = isCoupon;
	}

	public Long getCouponId() {
		return couponId;
	}

	public void setCouponId(Long couponId) {
		this.couponId = couponId;
	}

}
