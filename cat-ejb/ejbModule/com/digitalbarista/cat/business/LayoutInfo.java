package com.digitalbarista.cat.business;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

import com.digitalbarista.cat.data.LayoutInfoDO;

@XmlRootElement
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

	@XmlAttribute
	public String getUUID() {
		return UUID;
	}

	public void setUUID(String uuid) {
		UUID = uuid;
	}

	@XmlAttribute
	public Integer getX() {
		return x;
	}

	public void setX(Integer x) {
		this.x = x;
	}

	@XmlAttribute
	public Integer getY() {
		return y;
	}

	public void setY(Integer y) {
		this.y = y;
	}

	@XmlAttribute
	public String getCampaignUUID() {
		return campaignUUID;
	}

	public void setCampaignUUID(String campaignUUID) {
		this.campaignUUID = campaignUUID;
	}

	@XmlAttribute
	public Integer getVersion() {
		return version;
	}

	public void setVersion(Integer version) {
		this.version = version;
	}

}
