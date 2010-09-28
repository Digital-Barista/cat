package com.digitalbarista.cat.data;


import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

/**
 * Entity implementation class for Entity: NodeConnectorLinkDO
 *
 */

@Entity
@Table(name="node_connector_link")
@Cache(usage=CacheConcurrencyStrategy.TRANSACTIONAL,region="cat/NodeConnectorLink")
public class NodeConnectorLinkDO implements Serializable,DataObject {
	   
	private Long primaryKey;
	private NodeDO node;
	private ConnectorDO connector;
	private Integer version;
	private ConnectionPoint connectionPoint;
	private static final long serialVersionUID = 1L;

	public NodeConnectorLinkDO() {
		super();
	}
	
	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	@Column(name="node_connector_link_id")
	public Long getPrimaryKey() {
		return this.primaryKey;
	}

	public void setPrimaryKey(Long primaryKey) {
		this.primaryKey = primaryKey;
	}
	
	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(name="node_id")
	public NodeDO getNode() {
		return this.node;
	}

	public void setNode(NodeDO node) {
		this.node = node;
	}
	
	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(name="connector_id")
	public ConnectorDO getConnector() {
		return this.connector;
	}

	public void setConnector(ConnectorDO connector) {
		this.connector = connector;
	}
	
	@Column(name="version")
	public Integer getVersion() {
		return this.version;
	}

	public void setVersion(Integer version) {
		this.version = version;
	}
	
	@Column(name="type")
	@Enumerated(EnumType.ORDINAL)
	public ConnectionPoint getConnectionPoint() {
		return this.connectionPoint;
	}

	public void setConnectionPoint(ConnectionPoint connectionPoint) {
		this.connectionPoint = connectionPoint;
	}
   
}
