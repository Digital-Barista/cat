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
@Table(name="node_info")
public class NodeInfoDO implements Serializable,DataObject {

	private Long primaryKey;
	private String name;
	private String value;
	private NodeDO node;
	private Integer version;
	private static final long serialVersionUID = 1L;

	public NodeInfoDO() {
		super();
	}
	
	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	@Column(name="node_info_id")
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
	@JoinColumn(name="node_id")
	public NodeDO getNode() {
		return node;
	}

	public void setNode(NodeDO node) {
		this.node = node;
	}

	@Column(name="version")
	public Integer getVersion() {
		return version;
	}

	public void setVersion(Integer version) {
		this.version = version;
	}
   
}
