   package com.digitalbarista.cat.ejb.session;

import java.util.Date;
import java.util.List;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import com.digitalbarista.cat.data.ScheduledTaskDO;
import com.digitalbarista.cat.message.event.CATEvent;
import com.digitalbarista.cat.message.event.CATEventType;
import org.hibernate.Query;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * Session Bean implementation class EventTimerManagerImpl
 */
@Component
@Transactional(propagation=Propagation.REQUIRED)
public class EventTimerManager{

    private Logger log = LogManager.getLogger(EventTimerManager.class);

    @Autowired
    private SessionFactory sf;

    @Autowired
    private EventManager eventManager;

    public void setTimer(String uid, String target, Integer version, CATEventType type, Date scheduledDate) {

            CATEvent e;
            switch(type)
            {
                    case ConnectorFired:
                            if(target==null || target.trim().length()==0)
                            {
                                    e=CATEvent.buildFireConnectorForAllSubscribersEvent(uid, version);
                            } else {
                                    e=CATEvent.buildFireConnectorForSubscriberEvent(uid, target, version);
                            }
                            eventManager.queueEventForScheduledDelivery(e,scheduledDate);
                            break;

                    default:
                            log.error("Scheduled task is not a valid schedulable event type.  Will not schedule.");
            }

    }

    public Date getNextEventTime()
    {
    	Query q = sf.getCurrentSession().getNamedQuery("scheduled.tasks.by.start");
    	q.setMaxResults(1);
    	List<ScheduledTaskDO> rs = q.list();
    	if(rs==null || rs.size()==0)
    		return null;
    	return rs.get(0).getScheduledDate();
    }

	public void fireOverdueEvents() {
    	Query q = sf.getCurrentSession().getNamedQuery("overdue.tasks.by.start");
    	q.setParameter("now", new Date());
    	List<ScheduledTaskDO> rs = q.list();
    	if(rs==null)
    		return;
    	CATEvent e;
    	CATEventType eType=null;
    	for(ScheduledTaskDO task : rs)
    	{
    		try
    		{
    			eType = CATEventType.valueOf(task.getEventType());
    		}
    		catch(Exception ex)
    		{
    			log.error("An exception was thrown trying to get the task type of this scheduled item.  Scheduled item will be removed."+task.toString());
    			sf.getCurrentSession().delete(task);
    			continue;
    		}
    		if(eType==null)
    		{
    			log.error("Task of type "+task.getEventType()+" is not a valid CATEventType.  Removing scheduled task.");
    			sf.getCurrentSession().delete(task);
    			continue;
    		}
    			
    		switch(eType)
    		{
    			case ConnectorFired:
    				if(task.getTarget()==null || task.getTarget().trim().length()==0)
    				{
    					e=CATEvent.buildFireConnectorForAllSubscribersEvent(task.getSourceUID(),-1);
    				} else {
    					e=CATEvent.buildFireConnectorForSubscriberEvent(task.getSourceUID(), task.getTarget(),-1);
    				}
    				eventManager.queueEvent(e);
    				break;
    			
    			default:
    				log.error("Scheduled task is not a valid schedulable event type.  Removing from schedule: "+task.toString());
    		}
    		sf.getCurrentSession().delete(task);
    	}
	}
}
