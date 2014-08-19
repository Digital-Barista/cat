package com.digitalbarista.cat.audit;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.Table;

/**
 * Entity implementation class for Entity: AuditDO
 *
 */
@Entity
@Table(name="audit_generic")
public class AuditDO implements Serializable {

	private Long primaryKey;
	private AuditType auditType;
	private Date timestamp;
	private String descriminator1;
	private String descriminator2;
	private String username;
	private String data;
	private static final long serialVersionUID = 1L;

	public AuditDO() {
		super();
	}
	
	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	@Column(name="audit_id")
	public Long getPrimaryKey() {
		return this.primaryKey;
	}

	public void setPrimaryKey(Long primaryKey) {
		this.primaryKey = primaryKey;
	}
	
	@Column(name="audit_type")
	@Enumerated(EnumType.STRING)
	public AuditType getAuditType() {
		return this.auditType;
	}
	public void setAuditType(AuditType auditType) {
		this.auditType = auditType;
	}
	
	@Column(name="audit_time")
	public Date getTimestamp() {
		return this.timestamp;
	}
	public void setTimestamp(Date timestamp) {
		this.timestamp = timestamp;
	}
	
	@Column(name="descriminator_1")
	public String getDescriminator1() {
		return this.descriminator1;
	}
	public void setDescriminator1(String descriminator1) {
		this.descriminator1 = descriminator1;
	}   

	@Column(name="descriminator_2")
	public String getDescriminator2() {
		return this.descriminator2;
	}
	public void setDescriminator2(String descriminator2) {
		this.descriminator2 = descriminator2;
	}
	
	@Column(name="audit_user")
	public String getUsername() {
		return this.username;
	}
	public void setUsername(String username) {
		this.username = username;
	}
	
	@Lob
	@Column(name="audit_data")
	public String getData() {
		return this.data;
	}
	public void setData(String data) {
		this.data = data;
	}
   
}
