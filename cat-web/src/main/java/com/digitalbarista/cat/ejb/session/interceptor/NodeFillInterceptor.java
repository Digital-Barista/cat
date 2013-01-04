package com.digitalbarista.cat.ejb.session.interceptor;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


import com.digitalbarista.cat.business.Campaign;
import com.digitalbarista.cat.business.ContactTag;
import com.digitalbarista.cat.business.Node;
import com.digitalbarista.cat.business.TaggingNode;
import com.digitalbarista.cat.data.ContactTagDO;
import com.digitalbarista.cat.data.NodeDO;
import com.digitalbarista.cat.data.NodeInfoDO;
import com.digitalbarista.cat.ejb.session.CacheAccessManager;
import com.digitalbarista.cat.ejb.session.CacheAccessManager.CacheName;
import com.digitalbarista.cat.ejb.session.CampaignManager;
import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

@Component("nodeFillInterceptor")
@Lazy
public class NodeFillInterceptor implements MethodInterceptor {

    @Autowired
    private ApplicationContext ctx;
  
    @Autowired
    private SessionFactory sf;
    
    public Object invoke(MethodInvocation mi) throws Throwable
    {
            Integer version=null;
            if(mi.getMethod().getDeclaringClass().equals(CampaignManager.class))
            {
              if(Campaign.class.isAssignableFrom(mi.getMethod().getReturnType()))
              {
                      Campaign camp = (Campaign)mi.proceed();
                      if(camp==null)
                              return null;
                      for(Node node : camp.getNodes())
                              fillNode(node,camp.getCurrentVersion());
                      return camp;
              } else if(Node.class.isAssignableFrom(mi.getMethod().getReturnType())){
                      if(mi.getArguments().length==2 && Integer.class.isAssignableFrom(mi.getArguments()[1].getClass()))
                              version=(Integer)mi.getArguments()[1];
                      Node ret = fillNode((Node)mi.proceed(),version);
                      return ret;
              }
            }
            return mi.proceed();
    }

    private Node fillNode(Node nodeToFill, Integer version)
    {
            CacheAccessManager cache = ctx.getBean(CacheAccessManager.class);
            CampaignManager campaignManager = ctx.getBean(CampaignManager.class);
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
                                            ct.copyFrom((ContactTagDO)sf.getCurrentSession().get(ContactTagDO.class, new Long(ni.getValue())));
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
