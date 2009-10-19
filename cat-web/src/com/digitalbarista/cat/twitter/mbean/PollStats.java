package com.digitalbarista.cat.twitter.mbean;

import java.util.Date;

public class PollStats {

	private Date lastPollTime;
	private int remainingQueries;
	private int maxQueries;
	private Date resetTime;
	private long lowestReadMessage=-1;
	private long highestDeletedMessage=-1;
	
	public Date getLastPollTime() {
		return lastPollTime;
	}
	public void setLastPollTime(Date lastPollTime) {
		this.lastPollTime = lastPollTime;
	}
	public int getRemainingQueries() {
		return remainingQueries;
	}
	public void setRemainingQueries(int remainingQueries) {
		this.remainingQueries = remainingQueries;
	}
	public long getLowestReadMessage() {
		return lowestReadMessage;
	}
	public void setLowestReadMessage(long lowestReadMessage) {
		this.lowestReadMessage = lowestReadMessage;
	}
	public long getHighestDeletedMessage() {
		return highestDeletedMessage;
	}
	public void setHighestDeletedMessage(long highestDeletedMessage) {
		this.highestDeletedMessage = highestDeletedMessage;
	}
	public Date getResetTime() {
		return resetTime;
	}
	public void setResetTime(Date resetTime) {
		this.resetTime = resetTime;
	}
	public int getMaxQueries() {
		return maxQueries;
	}
	public void setMaxQueries(int maxQueries) {
		this.maxQueries = maxQueries;
	}
	
}
