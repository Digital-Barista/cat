package com.digitalbarista.cat.ejb.session;

import java.util.List;

import javax.ejb.Local;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;


import com.digitalbarista.cat.business.LayoutInfo;
import com.digitalbarista.cat.data.LayoutInfoDO;
import org.jboss.resteasy.annotations.providers.jaxb.Wrapped;

@Local
@Path("/layouts")
@Produces({"application/xml","application/json"})
@Consumes({"application/xml","application/json"})
public interface LayoutManager {
	public List<LayoutInfo> getLayoutInfo(List<String> uidList);
	@GET
	@Path("/{uid}")
	public LayoutInfo getLayoutInfo(@PathParam("uid") String uid, Integer version);
	public LayoutInfoDO getSimpleLayoutInfo(String uuid, Integer version);
	@DELETE
	@Path("/{uid}")
	public void delete(@PathParam("uid") String uid, Integer version);
	@GET
	@Wrapped(element="Layouts")
	public List<LayoutInfo> getLayoutsByCampaign(@QueryParam("campaignid") String uid);
	@Path("/{campaign-uid}/{version}")
	@GET
	@Wrapped(element="Layouts")
	public List<LayoutInfo> getLayoutsByCampaignAndVersion(@PathParam("campaign-uid") String uid,@PathParam("version") Integer version);
	@POST
	public void save(LayoutInfo layout);
}
