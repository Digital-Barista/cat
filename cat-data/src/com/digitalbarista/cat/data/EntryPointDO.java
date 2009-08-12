package com.digitalbarista.cat.data;

import java.io.Serializable;
import java.util.Set;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.OneToMany;
import javax.persistence.Table;

/**
 * Entity implementation class for Entity: EntryPointDO
 *
 */
@Entity
@Table(name="entry_points")

public class EntryPointDO implements DataObject,Serializable {

	
	private Long primaryKey;
	private String description;
	private String value;
	private EntryPointType type;
	private EntryRestrictionType restriction;
	private Set<ClientDO> clients;
	private Long restrictionID;
	private static final long serialVersionUID = 1L;
	private Set<KeywordDO> keywords;

	public EntryPointDO() {
		super();
	}
	
	@Id
	@Column(name="entry_point_id")
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	public Long getPrimaryKey() {
		return this.primaryKey;
	}
	
	public void setPrimaryKey(Long PrimaryKey) {
		this.primaryKey = PrimaryKey;
	}
	
	@Column(name="description")
	public String getDescription() {
		return this.description;
	}

	public void setDescription(String description) {
		this.description = description;
	}
	
	@Column(name="entry_point")
	public String getValue() {
		return this.value;
	}

	public void setValue(String value) {
		this.value = value;
	}
	
	@Column(name="entry_type")
	@Enumerated(EnumType.STRING)
	public EntryPointType getType() {
		return this.type;
	}

	public void setType(EntryPointType type) {
		this.type = type;
	}
	
	@Column(name="restriction_type")
	@Enumerated(EnumType.STRING)
	public EntryRestrictionType getRestriction() {
		return this.restriction;
	}

	public void setRestriction(EntryRestrictionType restriction) {
		this.restriction = restriction;
	}

	@ManyToMany(targetEntity=ClientDO.class)
	@JoinTable(
		name="client_entry_point_link",
		joinColumns=@JoinColumn(name="entry_point_id"),
		inverseJoinColumns=@JoinColumn(name="client_id")
	)
	public Set<ClientDO> getClients() {
		return clients;
	}

	public void setClients(Set<ClientDO> clients) {
		this.clients = clients;
	}

	@Column(name="restriction_id")
	public Long getRestrictionID() {
		return restrictionID;
	}

	public void setRestrictionID(Long restrictionID) {
		this.restrictionID = restrictionID;
	}

	@OneToMany(targetEntity=KeywordDO.class, mappedBy="entryPoint")
	public Set<KeywordDO> getKeywords() {
		return keywords;
	}

	public void setKeywords(Set<KeywordDO> keywords) {
		this.keywords = keywords;
	}
}
