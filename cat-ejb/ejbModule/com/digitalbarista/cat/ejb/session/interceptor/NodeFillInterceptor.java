package com.digitalbarista.cat.ejb.session.interceptor;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.annotation.Resource;
import javax.ejb.EJB;
import javax.ejb.SessionContext;
import javax.interceptor.AroundInvoke;
import javax.interceptor.InvocationContext;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import com.digitalbarista.cat.business.Campaign;
import com.digitalbarista.cat.business.ContactTag;
import com.digitalbarista.cat.business.Node;
import com.digitalbarista.cat.business.TaggingNode;
import com.digitalbarista.cat.data.ContactTagDO;
import com.digitalbarista.cat.data.NodeDO;
import com.digitalbarista.cat.data.NodeInfoDO;
import com.digitalbarista.cat.ejb.session.CacheAccessManager;
import com.digitalbarista.cat.ejb.session.CampaignManager;
import com.digitalbarista.cat.ejb.session.ContactManager;
import com.digitalbarista.cat.ejb.session.CacheAccessManager.CacheName;

public class NodeFillInterceptor {

	@Resource
	SessionContext ctx;
	
	@EJB(name="ejb/cat/CampaignManager")
	CampaignManager campaignManager;
		
	@EJB(name="ejb/cat/ContactManager")
	ContactManager contactManager;
	
	@EJB(name="ejb/cat/CacheAccessManager")
	CacheAccessManager cache;
	
	@PersistenceContext(unitName="cat-data")
	private EntityManager em;
		
	@AroundInvoke
	public Object fillNodes(InvocationContext ic) throws Exception
	{
		Integer version=null;
		if(Campaign.class.isAssignableFrom(ic.getMethod().getReturnType()))
		{
			Campaign camp = (Campaign)ic.proceed();
			if(camp==null)
				return null;
			for(Node node : camp.getNodes())
				fillNode(node,camp.getCurrentVersion());
			return camp;
		} else if(Node.class.isAssignableFrom(ic.getMethod().getReturnType())){
			if(ic.getParameters().length==2 && Integer.class.isAssignableFrom(ic.getParameters()[1].getClass()))
				version=(Integer)ic.getParameters()[1];
			Node ret = fillNode((Node)ic.proceed(),version);
			return ret;
		}
		return ic.proceed();
	}
	
	private Node fillNode(Node nodeToFill, Integer version)
	{
		if(nodeToFill instanceof TaggingNode)
		{
			String key = nodeToFill.getUid()+"/"+version;
			TaggingNode ret = (TaggingNode) cache.getCachedObject(CacheName.NodeCache, key);
			if(ret!=null && ret.getTags()!=null && ret.getTags().size()>0)
			{
				((TaggingNode)nodeToFill).setTags(ret.getTags());
			} else {
				ret = (TaggingNode)nodeToFill;
				NodeDO simpleNode = campaignManager.getSimpleNode(nodeToFill.getUid());
				if(version==null)
					version=simpleNode.getCampaign().getCurrentVersion();
				if (ret.getTags() == null)
					ret.setTags(new ArrayList<ContactTag>());
				ret.getTags().clear();
				ContactTag ct;
				for(NodeInfoDO ni : simpleNode.getNodeInfo())
				{
					if(!ni.getVersion().equals(version))
						continue;
								
					if(ni.getName().startsWith(TaggingNode.INFO_PROPERTY_TAG+"["))
					{
						Matcher r = Pattern.compile(TaggingNode.INFO_PROPERTY_TAG+"\\[([\\d]+)\\]").matcher(ni.getName());
						r.matches();
						ct = new ContactTag();
						ct.copyFrom(em.find(ContactTagDO.class, new Long(ni.getValue())));
						fillListAndSet(ret.getTags(),new Integer(r.group(1)), ct);
					}
				}
				if(version<simpleNode.getCampaign().getCurrentVersion())
					cache.cacheObject(CacheName.NodeCache, key, nodeToFill);
			}
		}
		return nodeToFill;
	}
	
	private <T> void fillListAndSet(List<T> theList, int pos, T value)
	{
		while(theList.size()<pos+1)
			theList.add(null);
		theList.set(pos, value);
	}	
}
