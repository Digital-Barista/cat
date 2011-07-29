package com.digitalbarista.cat.ejb.session;

import java.util.List;
import java.util.Map;

import javax.ejb.Local;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;

import org.jboss.resteasy.annotations.providers.jaxb.Wrapped;
import org.jboss.resteasy.annotations.providers.jaxb.WrappedMap;

import com.digitalbarista.cat.business.BroadcastInfo;
import com.digitalbarista.cat.business.Campaign;
import com.digitalbarista.cat.business.Connector;
import com.digitalbarista.cat.business.Contact;
import com.digitalbarista.cat.business.CouponNode;
import com.digitalbarista.cat.business.EntryData;
import com.digitalbarista.cat.business.MessageNode;
import com.digitalbarista.cat.business.Node;
import com.digitalbarista.cat.business.criteria.ContactSearchCriteria;
import com.digitalbarista.cat.data.CampaignDO;
import com.digitalbarista.cat.data.ConnectorDO;
import com.digitalbarista.cat.data.NodeDO;

@Local
@Path("/campaigns")
@Produces({"application/xml","application/json"})
@Consumes({"application/xml","application/json"})
public interface CampaignManager {
	@GET
	@Path("/{uid}")
	public Campaign getDetailedCampaign(@PathParam("uid") String campaignUUID);
	public CampaignDO getSimpleCampaign(String campaignUUID);
	@GET
	@Path("/{uid}/versions/{version}")
	public Campaign getSpecificCampaignVersion(@PathParam("uid") String campaignUUID, @PathParam("version") int version);
	@Path("/{uid}/published")
	@GET
	public Campaign getLastPublishedCampaign(@PathParam("uid") String campaignUUID);
	@POST
	public Campaign save(Campaign campaign);
	@Path("/{template-uid}")
	@POST
	public Campaign createFromTemplate(Campaign campaign, @PathParam("template-uid") String campaignTemplateUUID);
	public void delete(Campaign campaign);
	@DELETE
	@Path("/{uid}")
	public void deleteCampaign(@PathParam("uid") String uid);
	@GET
	@Wrapped(element="Campaigns")
	public List<Campaign> getAllCampaigns();
	@Path("/templates")
	@GET
	@Wrapped(element="Templates")
	public List<Campaign> getAllTemplates();
	
	public List<Campaign> getCampaigns(List<Long> clientIDs);
	public List<BroadcastInfo> getBroadcastCampaigns(List<Long> clientIDs);
	public List<Campaign> getCampaignTemplates(List<Long> clientIDs);
	
	
	@GET
	@Path("/nodes/{uid}")
	public Node getNode(@PathParam("uid") String nodeUUID);
	@GET
	@Path("/nodes/{uid}/versions/{version}")
	public Node getSpecificNodeVersion(@PathParam("uid") String nodeUUID,@PathParam("version") Integer version);
	public NodeDO getSimpleNode(String nodeUUID);
	@Path("/nodes")
	@POST
	public void save(Node node);
	public void delete(Node node);
	@Path("/nodes/{uid}")
	@DELETE
	public void deleteNode(@PathParam("uid") String uid);
	
	@GET
	@Path("/connectors/{uid}")
	public Connector getConnector(@PathParam("uid") String connectorUUID);
	@GET
	@Path("/connectors/{uid}/versions/{version}")
	public Connector getSpecificConnectorVersion(@PathParam("uid") String connectorUUID, @PathParam("version") Integer version);
	public ConnectorDO getSimpleConnector(String connectorUUID);
	@Path("/connectors")
	@POST
	public void save(Connector connector);
	public void delete(Connector connector);
	@Path("/connectors/{uid}")
	@DELETE
	public void deleteConnector(@PathParam("uid") String uid);
	
	@POST
	@Path("{uid}/publish")
	public void publish(@PathParam("uid") String campaignUUID);
	
	@GET
	@Path("/{uid}/nodes")
	@Wrapped
	@WrappedMap(map="nodeSubscriberCount",key="nodeUID",entry="count")
	public Map<String,Long> getNodeSubscriberCount(@PathParam("uid") String campaignUUID);
	
	public CampaignDO getCampaignForNode(String uid);
	public CampaignDO getCampaignForConnector(String uid);
	public Integer getCurrentCampaignVersionForNode(String uid);
	public Integer getCurrentCampaignVersionForConnector(String uid);
	
	public void broadcastMessage(Long clientPK, List<EntryData> entryPoints, MessageNode message, List<Contact> contacts);
	public void broadcastMessageSearch(Long clientPK, List<EntryData> entryPoints, MessageNode message, ContactSearchCriteria search);
	public void broadcastCoupon(Long clientPK, List<EntryData> entryPoints, CouponNode coupon, List<Contact> contacts);
	public void broadcastCouponSearch(Long clientPK, List<EntryData> entryPoints, CouponNode coupon, ContactSearchCriteria search);
}
