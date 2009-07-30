package com.digitalbarista.cat.business;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

import com.digitalbarista.cat.data.ClientDO;
import com.digitalbarista.cat.data.EntryPointDO;
import com.digitalbarista.cat.data.EntryPointType;
import com.digitalbarista.cat.data.EntryRestrictionType;
import com.digitalbarista.cat.data.KeywordDO;

public class EntryPointDefinition implements BusinessObject<EntryPointDO>,Serializable {
	
	private Long primaryKey;
	private String description;
	private String value;
	private EntryPointType type;
	private EntryRestrictionType restriction;
	private Set<Integer> clientIDs;
	private Set<Keyword> keywords;
	private Long restrictionID;
	private static final long serialVersionUID = 1L;

	@Override
	public void copyFrom(EntryPointDO dataObject) {
		copyFrom(dataObject,null);
	}

	void copyFrom(EntryPointDO dataObject, Long clientPK) 
	{
		primaryKey=dataObject.getPrimaryKey();
		description=dataObject.getDescription();
		value=dataObject.getValue();
		type=dataObject.getType();
		restriction=dataObject.getRestriction();
		restrictionID=dataObject.getRestrictionID();
		
		clientIDs=new HashSet<Integer>();
		if (dataObject.getClients() != null)
		{
			for(ClientDO client : dataObject.getClients())
				clientIDs.add(client.getPrimaryKey().intValue());
		}
		
		keywords=new HashSet<Keyword>();
		Keyword tempKwd;
		
		if (dataObject.getKeywords() != null)
		{
			for(KeywordDO keyword : dataObject.getKeywords())
			{
				if(clientPK!=null && !keyword.getClient().getPrimaryKey().equals(clientPK))
					continue;
				tempKwd = new Keyword();
				tempKwd.copyFrom(keyword);
				keywords.add(tempKwd);
			}
		}
	}
	
	@Override
	public void copyTo(EntryPointDO dataObject) {
		dataObject.setDescription(description);
		dataObject.setValue(value);
		dataObject.setType(type);
		dataObject.setRestriction(restriction);
		dataObject.setRestrictionID(restrictionID);
	}

	public EntryPointDefinition() {
		super();
	}
	
	public Long getPrimaryKey() {
		return this.primaryKey;
	}
	
	public void setPrimaryKey(Long primaryKey) {
		this.primaryKey = primaryKey;
	}
	
	public String getDescription() {
		return this.description;
	}

	public void setDescription(String description) {
		this.description = description;
	}
	
	public String getValue() {
		return this.value;
	}

	public void setValue(String value) {
		this.value = value;
	}
	
	public EntryPointType getType() {
		return this.type;
	}

	public void setType(EntryPointType type) {
		this.type = type;
	}
	
	public EntryRestrictionType getRestriction() {
		return this.restriction;
	}

	public void setRestriction(EntryRestrictionType restriction) {
		this.restriction = restriction;
	}

	public Set<Integer> getClientIDs() {
		return clientIDs;
	}

	public void setClientIDs(Set<Integer> clientIDs) {
		this.clientIDs = clientIDs;
	}

	public Long getRestrictionID() {
		return restrictionID;
	}

	public void setRestrictionID(Long restrictionID) {
		this.restrictionID = restrictionID;
	}

	public Set<Keyword> getKeywords() {
		return keywords;
	}

	public void setKeywords(Set<Keyword> keywords) {
		this.keywords = keywords;
	}
}
