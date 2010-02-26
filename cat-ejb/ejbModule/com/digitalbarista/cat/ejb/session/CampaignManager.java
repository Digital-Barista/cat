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
import javax.ws.rs.QueryParam;

import org.jboss.resteasy.annotations.providers.jaxb.Wrapped;
import org.jboss.resteasy.annotations.providers.jaxb.WrappedMap;

import com.digitalbarista.cat.business.Campaign;
import com.digitalbarista.cat.business.Connector;
import com.digitalbarista.cat.business.Node;
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
	public void createFromTemplate(Campaign campaign, @QueryParam("template-uid") String campaignTemplateUUID);
	@DELETE
	public void delete(Campaign campaign);
	@GET
	@Wrapped(element="Campaigns")
	public List<Campaign> getAllCampaigns();
	@Path("/templates")
	@GET
	@Wrapped(element="Templates")
	public List<Campaign> getAllTemplates();
	
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
	@Path("/nodes")
	@DELETE
	public void delete(Node node);
	
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
	@Path("/connectors")
	@DELETE
	public void delete(Connector connector);
	
	@POST
	@Path("{uid}/publish")
	public void publish(@PathParam("uid") String campaignUUID);
	
	@GET
	@Path("/{uid}/nodes")
	@Wrapped
	@WrappedMap(map="nodeSubscriberCount",key="nodeUID",entry="count")
	public Map<String,Long> getNodeSubscriberCount(@PathParam("uid") String campaignUUID);
}
