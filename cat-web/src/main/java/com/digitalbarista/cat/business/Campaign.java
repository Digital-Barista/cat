package com.digitalbarista.cat.business;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElementRef;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;

import com.digitalbarista.cat.audit.Auditable;
import com.digitalbarista.cat.audit.PrimaryDescriminator;
import com.digitalbarista.cat.audit.SecondaryDescriminator;
import com.digitalbarista.cat.data.AddInMessageDO;
import com.digitalbarista.cat.data.CampaignDO;
import com.digitalbarista.cat.data.CampaignInfoDO;
import com.digitalbarista.cat.data.CampaignMode;
import com.digitalbarista.cat.data.CampaignStatus;
import com.digitalbarista.cat.data.CampaignVersionDO;

@XmlRootElement(name="Campaign")
public class Campaign implements BusinessObject<CampaignDO>,Auditable {

	private Long primaryKey;
	private String name;
	private Boolean isAutoStart;
	
	@SecondaryDescriminator
	private String uid;
	private int currentVersion;
	private Set<Node> nodes=new HashSet<Node>();
	private Set<Connector> connectors=new HashSet<Connector>();
	private Set<AddInMessage> addInMessages = new HashSet<AddInMessage>();
	private Set<CampaignInfo> campaignInfos = new HashSet<CampaignInfo>();
	private int subscriberCount = 0;
	
	@PrimaryDescriminator
	private Long clientPK;
	private CampaignMode mode;
	private CampaignStatus status;
	
	private Date lastPublished;
	
	public void copyFrom(CampaignDO dataObject, Integer version) {
		primaryKey=dataObject.getPrimaryKey();
		name=dataObject.getName();
		currentVersion=dataObject.getCurrentVersion();
		uid = dataObject.getUID();
		mode = dataObject.getMode();
		status = dataObject.getStatus();
		if(dataObject.getClient()!=null)
		{
			clientPK = dataObject.getClient().getPrimaryKey();
		}
		
		// Copy all addin messages
		if (dataObject.getAddInMessages() != null)
		{
			for (AddInMessageDO addDO : dataObject.getAddInMessages())
			{
				AddInMessage add = new AddInMessage();
				add.copyFrom(addDO);
				addInMessages.add(add);
			}
		}

		// Copy all campaign infos
		if (dataObject.getCampaignInfos() != null)
		{
			for (CampaignInfoDO campDO : dataObject.getCampaignInfos())
			{
				CampaignInfo camp = new CampaignInfo();
				camp.copyFrom(campDO);
				campaignInfos.add(camp);
				
				// Set the autoStart flag an autoStartNodeUID is present
				if (campDO.getName().equals(CampaignInfoDO.KEY_AUTO_START_NODE_UID) &&
					campDO.getValue() != null &&
					campDO.getValue().length() > 0)
					isAutoStart = true;
			}
		}
		
		if (dataObject.getVersions()!=null)
		{			
			for(CampaignVersionDO versionDO : dataObject.getVersions())
			{
				lastPublished=(lastPublished==null || lastPublished.before(versionDO.getPublishedDate()))?versionDO.getPublishedDate():lastPublished;
			}
		}
		
		// Add subscriber count
//		if (dataObject.getSubscribers() != null)
//		{
//			subscriberCount = dataObject.getSubscribers().size();
//		}
	}
	
	@Override
	public void copyFrom(CampaignDO dataObject) {
		copyFrom(dataObject,null);
	}

	@Override
	public void copyTo(CampaignDO dataObject) {
		dataObject.setName(name);
	}

	@Override
	public String auditString() {
		StringBuffer ret = new StringBuffer();
		ret.append(";uid:"+getUid());
		ret.append(";currentVersion:"+getCurrentVersion());
		ret.append(";clientID:"+getClientPK());
		ret.append(";name:"+getName());

		// Build list of add in messages
		ret.append(";addInMessages:(");
		if (getAddInMessages() == null ||
			getAddInMessages().size() == 0)
		{
			ret.append("none");
		}
		else
		{
			for (AddInMessage add : getAddInMessages())
				ret.append(add.getEntryType() + " - " + add.getType() + " - " + add.getMessage() + ", ");
		}
		ret.append(")");
		
		// Build list of campaign info
		ret.append(";campaignInfos:(");
		if (getCampaignInfos() == null ||
				getCampaignInfos().size() == 0)
		{
			ret.append("none");
		}
		else
		{
			for (CampaignInfo ci : getCampaignInfos())
				ret.append(ci.getEntryType() + " - " + ci.getName() + " - " + ci.getValue() + ", ");
		}
		ret.append(")");
		
		return ret.toString();
	}

	@XmlAttribute(name="id")
	public Long getPrimaryKey() {
		return primaryKey;
	}

	public void setPrimaryKey(Long primaryKey) {
		this.primaryKey = primaryKey;
	}

	@XmlAttribute
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	@XmlAttribute
	public int getCurrentVersion() {
		return currentVersion;
	}

	public void setCurrentVersion(int currentVersion) {
		this.currentVersion = currentVersion;
	}
	
	@XmlAttribute
	public Boolean getIsAutoStart() {
		return isAutoStart;
	}

	public void setIsAutoStart(Boolean isAutoStart) {
		this.isAutoStart = isAutoStart;
	}

	@XmlAttribute
	public String getUid() {
		return uid;
	}

	public void setUid(String uid) {
		this.uid = uid;
	}

	@XmlElementWrapper(name="Nodes")
	@XmlElementRef
	public Set<Node> getNodes() {
		return nodes;
	}

	public void setNodes(Set<Node> nodes) {
		this.nodes = nodes;
	}

	@XmlElementWrapper(name="Connectors")
	@XmlElementRef
	public Set<Connector> getConnectors() {
		return connectors;
	}

	public void setConnectors(Set<Connector> connectors) {
		this.connectors = connectors;
	}

	@XmlAttribute(name="clientid")
	public Long getClientPK() {
		return clientPK;
	}

	public void setClientPK(Long clientPK) {
		this.clientPK = clientPK;
	}

	@XmlAttribute
	public CampaignMode getMode() {
		return mode;
	}

	public void setMode(CampaignMode mode) {
		this.mode = mode;
	}

	@XmlElementWrapper(name="AddInMessages")
	@XmlElementRef
	public Set<AddInMessage> getAddInMessages() {
		return addInMessages;
	}

	public void setAddInMessages(Set<AddInMessage> addInMessages) {
		this.addInMessages = addInMessages;
	}

	@XmlElementWrapper(name="CampaignInfos")
	@XmlElementRef
	public Set<CampaignInfo> getCampaignInfos() {
		return campaignInfos;
	}

	public void setCampaignInfos(Set<CampaignInfo> campaignInfos) {
		this.campaignInfos = campaignInfos;
	}

	@XmlAttribute
	public int getSubscriberCount() {
		return subscriberCount;
	}

	public void setSubscriberCount(int subscriberCount) {
		this.subscriberCount = subscriberCount;
	}

	@XmlAttribute
	public CampaignStatus getStatus()
  {
  	return status;
  }

	public void setStatus(CampaignStatus status)
  {
  	this.status = status;
  }
	@XmlAttribute
	public Date getLastPublished() {
		return lastPublished;
	}

	public void setLastPublished(Date lastPublished) {
	}
	
}
