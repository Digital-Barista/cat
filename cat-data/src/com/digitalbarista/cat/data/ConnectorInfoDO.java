package com.digitalbarista.cat.data;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

/**
 * Entity implementation class for Entity: ConnectorInfoDO
 *
 */

@Entity
@Table(name="connector_info")
public class ConnectorInfoDO implements Serializable,DataObject {

	private Long primaryKey;
	private String name;
	private String value;
	private ConnectorDO connector;
	private Integer version;
	private static final long serialVersionUID = 1L;

	public ConnectorInfoDO() {
		super();
	}
	
	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	@Column(name="connector_info_id")
	public Long getPrimaryKey() {
		return this.primaryKey;
	}

	public void setPrimaryKey(Long primaryKey) {
		this.primaryKey = primaryKey;
	}
	
	@Column(name="name")
	public String getName() {
		return this.name;
	}

	public void setName(String name) {
		this.name = name;
	}
	
	@Column(name="value")
	public String getValue() {
		return this.value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(name="connector_id")
	public ConnectorDO getConnector() {
		return connector;
	}

	public void setConnector(ConnectorDO connector) {
		this.connector = connector;
	}

	@Column(name="version")
	public Integer getVersion() {
		return version;
	}

	public void setVersion(Integer version) {
		this.version = version;
	}
   
}
