package com.digitalbarista.cat.ejb.session;

import java.util.List;
import java.util.Map;

import javax.ejb.Local;

import com.digitalbarista.cat.business.Campaign;
import com.digitalbarista.cat.business.Connector;
import com.digitalbarista.cat.business.Node;
import com.digitalbarista.cat.data.CampaignDO;
import com.digitalbarista.cat.data.ConnectorDO;
import com.digitalbarista.cat.data.NodeDO;

@Local
public interface CampaignManager {
	public Campaign getDetailedCampaign(String campaignUUID);
	public CampaignDO getSimpleCampaign(String campaignUUID);
	public Campaign getSpecificCampaignVersion(String campaignUUID, int version);
	public Campaign getLastPublishedCampaign(String campaignUUID);
	public void save(Campaign campaign);
	public void createFromTemplate(Campaign campaign, String campaignTemplateUUID);
	public void delete(Campaign campaign);
	public List<Campaign> getAllCampaigns();
	
	public Node getNode(String nodeUUID);
	public Node getSpecificNodeVersion(String nodeUUID,Integer version);
	public NodeDO getSimpleNode(String nodeUUID);
	public void save(Node node);
	public void delete(Node node);
	
	public Connector getConnector(String connectorUUID);
	public Connector getSpecificConnectorVersion(String connectorUUID, Integer version);
	public ConnectorDO getSimpleConnector(String connectorUUID);
	public void save(Connector connector);
	public void delete(Connector connector);
	
	public void publish(String campaignUUID);
	
	public Map<String,Long> getNodeSubscriberCount(String campaignUUID);
}
