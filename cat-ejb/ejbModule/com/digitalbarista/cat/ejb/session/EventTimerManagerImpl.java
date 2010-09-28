   package com.digitalbarista.cat.ejb.session;

import java.util.Date;
import java.util.List;

import javax.annotation.Resource;
import javax.annotation.security.PermitAll;
import javax.annotation.security.RunAs;
import javax.ejb.EJB;
import javax.ejb.SessionContext;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import javax.transaction.Transaction;
import javax.transaction.TransactionManager;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.jboss.annotation.ejb.LocalBinding;
import org.jboss.annotation.security.RunAsPrincipal;

import com.digitalbarista.cat.data.ScheduledTaskDO;
import com.digitalbarista.cat.message.event.CATEvent;
import com.digitalbarista.cat.message.event.CATEventType;
import com.digitalbarista.cat.timer.CATTimer;

/**
 * Session Bean implementation class EventTimerManagerImpl
 */
@Stateless
@LocalBinding(jndiBinding="ejb/cat/EventTimerManager")
@RunAsPrincipal("admin")
@RunAs("admin")
@TransactionAttribute(TransactionAttributeType.REQUIRED)
public class EventTimerManagerImpl implements EventTimerManager {

	private Logger log = LogManager.getLogger(EventTimerManagerImpl.class);

	@Resource
	private SessionContext ctx; //Used to flag rollbacks.
	
	@Resource(mappedName="java:/TransactionManager")
	private TransactionManager tm;
	
	@PersistenceContext(unitName="cat-data")
	private EntityManager em;

	@EJB(name="ejb/cat/EventManager")
	private EventManager eventManager;
	
	@PermitAll
    public void setTimer(String uid, String target, CATEventType type, Date scheduledDate) {
    	ScheduledTaskDO task = new ScheduledTaskDO();
    	task.setEventType(type.toString());
    	task.setSourceUID(uid);
    	task.setTarget(target);
    	task.setScheduledDate(scheduledDate);
    	em.persist(task);
    	
    	Transaction tx=null;
    	try{tx=tm.getTransaction();}catch(Exception e){}
    	CATTimer.eventScheduled(tx);
    }

	@PermitAll
    public Date getNextEventTime()
    {
    	Query q = em.createNamedQuery("scheduled.tasks.by.start");
    	q.setMaxResults(1);
    	List<ScheduledTaskDO> rs = q.getResultList();
    	if(rs==null || rs.size()==0)
    		return null;
    	return rs.get(0).getScheduledDate();
    }

	@Override
	@PermitAll
	public void fireOverdueEvents() {
    	Query q = em.createNamedQuery("overdue.tasks.by.start");
    	q.setParameter("now", new Date());
    	List<ScheduledTaskDO> rs = q.getResultList();
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
    			em.remove(task);
    			continue;
    		}
    		if(eType==null)
    		{
    			log.error("Task of type "+task.getEventType()+" is not a valid CATEventType.  Removing scheduled task.");
    			em.remove(task);
    			continue;
    		}
    			
    		switch(eType)
    		{
    			case ConnectorFired:
    				if(task.getTarget()==null || task.getTarget().trim().length()==0)
    				{
    					e=CATEvent.buildFireConnectorForAllSubscribersEvent(task.getSourceUID());
    				} else {
    					e=CATEvent.buildFireConnectorForSubscriberEvent(task.getSourceUID(), task.getTarget());
    				}
    				eventManager.queueEvent(e);
    				break;
    			
    			default:
    				log.error("Scheduled task is not a valid schedulable event type.  Removing from schedule: "+task.toString());
    		}
    		em.remove(task);
    	}
	}
}
