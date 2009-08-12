package com.digitalbarista.cat.business;

import com.digitalbarista.cat.data.LayoutInfoDO;


public class LayoutInfo implements BusinessObject<LayoutInfoDO> {

	private String UUID;
	private Integer x;
	private Integer y;
	private String campaignUUID;
	private Integer version;
	
	@Override
	public void copyFrom(LayoutInfoDO dataObject) {
		if(dataObject==null)
			return;
		UUID=dataObject.getUID();
		x=dataObject.getXLoc();
		y=dataObject.getYLoc();
		campaignUUID=dataObject.getCampaign().getUID();
		version=dataObject.getVersion();
	}

	@Override
	public void copyTo(LayoutInfoDO dataObject) {
		dataObject.setUID(UUID);
		dataObject.setXLoc(x);
		dataObject.setYLoc(y);
	}

	public String getUUID() {
		return UUID;
	}

	public void setUUID(String uuid) {
		UUID = uuid;
	}

	public Integer getX() {
		return x;
	}

	public void setX(Integer x) {
		this.x = x;
	}

	public Integer getY() {
		return y;
	}

	public void setY(Integer y) {
		this.y = y;
	}

	public String getCampaignUUID() {
		return campaignUUID;
	}

	public void setCampaignUUID(String campaignUUID) {
		this.campaignUUID = campaignUUID;
	}

	public Integer getVersion() {
		return version;
	}

	public void setVersion(Integer version) {
		this.version = version;
	}

}
