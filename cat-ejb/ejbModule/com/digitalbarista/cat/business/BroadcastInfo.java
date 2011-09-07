package com.digitalbarista.cat.business;

import java.util.Date;

import com.digitalbarista.cat.data.CampaignDO;
import com.digitalbarista.cat.data.CampaignMode;
import com.digitalbarista.cat.data.CampaignNodeLinkDO;

public class BroadcastInfo implements BusinessObject<CampaignDO> {

	private Integer subscriberCount;
	private String title;
	private String message;
	private Integer couponRedemptionCount;
	private boolean isCoupon = false;
	private Long couponId;
	private String entryPointUID;
	private Date broadcastDate;

	@Override
	public void copyFrom(CampaignDO dataObject) {
		if(dataObject.getMode() != CampaignMode.Broadcast)
			throw new IllegalArgumentException("Cannot generate broadcast info on a non-broadcast campaign.");
		for(CampaignNodeLinkDO nodeLink : dataObject.getNodes())
		{
			switch(nodeLink.getNode().getType())
			{
				case Message:
				{
					couponRedemptionCount=0;
					MessageNode node = new MessageNode();
					node.copyFrom(nodeLink.getNode());
					title = nodeLink.getNode().getName();
					message = node.getMessage();
					isCoupon=false;
					break;
				}
				case Coupon:
				{
					CouponNode node = new CouponNode();
					node.copyFrom(nodeLink.getNode());
					message = node.getAvailableMessage();
					title = nodeLink.getNode().getName();
					isCoupon=true;
					couponId=node.getCouponId();
					break;
				}
				case Entry:
				case OutgoingEntry:
					entryPointUID = nodeLink.getNode().getUID();
			}
		}
		if(dataObject.getCurrentVersion()>1)
			broadcastDate = dataObject.getVersions().get(1).getPublishedDate();
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

	public String getEntryPointUID()
  {
  	return entryPointUID;
  }

	public void setEntryPointUID(String entryPointUID)
  {
  	this.entryPointUID = entryPointUID;
  }
	
	public Date getBroadcastDate() {
		return broadcastDate;
	}

	public void setBroadcastDate(Date broadcastDate) {
		this.broadcastDate = broadcastDate;
	}
}
