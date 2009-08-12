package com.digitalbarista.cat.data;

import com.digitalbarista.cat.data.EntryPointType;
import com.digitalbarista.cat.data.SubscriberDO;
import java.io.Serializable;
import java.lang.Long;
import java.lang.String;
import javax.persistence.*;

/**
 * Entity implementation class for Entity: SubscriberBlacklistDO
 *
 */
@Entity
@Table(name="subscriber_blacklist")
@NamedQuery(name="blacklist.entry", query="select b from SubscriberBlacklistDO b where b.subscriber.id=:subID and b.incomingAddress=:address and b.type=:type")
public class SubscriberBlacklistDO implements Serializable {

	
	private Long primaryKey;
	private SubscriberDO subscriber;
	private String incomingAddress;
	private EntryPointType type;
	private static final long serialVersionUID = 1L;

	public SubscriberBlacklistDO() {
		super();
	}  
	
	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	@Column(name="subscriber_blacklist_id")
	public Long getPrimaryKey() {
		return this.primaryKey;
	}
	public void setPrimaryKey(Long primaryKey) {
		this.primaryKey = primaryKey;
	}   

	@ManyToOne
	@JoinColumn(name="subscriber_id")
	public SubscriberDO getSubscriber() {
		return this.subscriber;
	}
	public void setSubscriber(SubscriberDO subscriber) {
		this.subscriber = subscriber;
	}   

	@Column(name="incoming_address")
	public String getIncomingAddress() {
		return this.incomingAddress;
	}
	public void setIncomingAddress(String incomingAddress) {
		this.incomingAddress = incomingAddress;
	}   

	@Column(name="incoming_type")
	@Enumerated(EnumType.STRING)
	public EntryPointType getType() {
		return this.type;
	}
	public void setType(EntryPointType type) {
		this.type = type;
	}
}
