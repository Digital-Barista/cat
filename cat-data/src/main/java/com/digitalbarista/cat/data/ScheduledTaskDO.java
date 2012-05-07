package com.digitalbarista.cat.data;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;

/**
 * Entity implementation class for Entity: ScheduledTaskDO
 *
 */

@Entity
@Table(name="scheduled_campaign_tasks")
@NamedQueries({
	@NamedQuery(name="scheduled.tasks.by.start", query="select t from ScheduledTaskDO t order by t.scheduledDate"),
	@NamedQuery(name="overdue.tasks.by.start", query="select t from ScheduledTaskDO t where t.scheduledDate<=:now order by t.scheduledDate"),
	@NamedQuery(name="clear.tasks.for.connector", query="delete from ScheduledTaskDO t where t.sourceUID=:sourceUID"),
	@NamedQuery(name="clear.specific.task", query="delete from ScheduledTaskDO t where t.sourceUID=:sourceUID and t.target=:target")
})
public class ScheduledTaskDO implements Serializable {

	private Long primaryKey;
	private Date scheduledDate;
	private String sourceUID;
	private String eventType;
	private String target;
	private static final long serialVersionUID = 1L;

	public ScheduledTaskDO() {
		super();
	}  
	
	@Id
	@Column(name="scheduled_task_id")
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	public Long getPrimaryKey() {
		return this.primaryKey;
	}

	public void setPrimaryKey(Long primaryKey) {
		this.primaryKey = primaryKey;
	}
	
	@Column(name="scheduled_date")
	public Date getScheduledDate() {
		return this.scheduledDate;
	}

	public void setScheduledDate(Date scheduledDate) {
		this.scheduledDate = scheduledDate;
	}
	
	@Column(name="source_uid")
	public String getSourceUID() {
		return this.sourceUID;
	}

	public void setSourceUID(String sourceUID) {
		this.sourceUID = sourceUID;
	}
	
	@Column(name="event_type")
	public String getEventType() {
		return this.eventType;
	}

	public void setEventType(String eventType) {
		this.eventType = eventType;
	}
	
	@Column(name="target")
	public String getTarget() {
		return this.target;
	}

	public void setTarget(String target) {
		this.target = target;
	}
   
}
