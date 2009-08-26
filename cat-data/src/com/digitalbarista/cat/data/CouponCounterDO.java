package com.digitalbarista.cat.data;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.Table;
import javax.persistence.Transient;

/**
 * Entity implementation class for Entity: CouponCounter
 *
 */
@Entity
@Table(name="coupon_counters")
public class CouponCounterDO implements Serializable,DataObject {

	private Integer couponCodeLength;
	private int[] bitScramble;
	private Long nextNumber;
	private static final long serialVersionUID = 1L;

	public CouponCounterDO() {
		super();
	}   

	@Id
	@Column(name="coupon_code_length")
	public Integer getCouponCodeLength() {
		return this.couponCodeLength;
	}
	public void setCouponCodeLength(Integer couponCodeLength) {
		this.couponCodeLength = couponCodeLength;
	}
	
	@Column(name="coupon_bit_scramble")
	@Lob
	private byte[] getBitScrambleBlob() {
		if(bitScramble==null) return null;
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		try {
			new ObjectOutputStream(out).writeObject(bitScramble);
		} catch (IOException e) {
			throw new IllegalArgumentException("There was an error serializing the object.",e);
		}
		return out.toByteArray();
	}
	private void setBitScrambleBlob(byte[] bitScrambleBlob) {
		try {
			bitScramble = (int[])new ObjectInputStream(new ByteArrayInputStream(bitScrambleBlob)).readObject();
		} catch (Exception e) {
			bitScramble = null;
			throw new IllegalArgumentException("There was an error deserializing the bit scramble array.",e);
		}
	}
	
	@Column(name="coupon_next_number")
	public Long getNextNumber() {
		return this.nextNumber;
	}
	public void setNextNumber(Long nextNumber) {
		this.nextNumber = nextNumber;
	}

	@Transient
	public int[] getBitScramble() {
		return bitScramble;
	}
	public void setBitScramble(int[] bitScramble) {
		this.bitScramble = bitScramble;
	}
   
}
