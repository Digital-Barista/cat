package com.digitalbarista.cat.business.reporting;

import java.math.BigInteger;
import java.util.Calendar;

public class DateData 
{
	private Integer year;
	private Integer month;
	private Integer day;
	private Calendar date;
	private BigInteger count;
	private BigInteger total;
	
	public Calendar getDate()
	{
		return date;
	}
	public void setDate(Calendar date)
	{
		this.date = date;
	}
	
	public Integer getYear()
	{
		return year;
	}
	public void setYear(Integer year)
	{
		this.year = year;
	}
	public Integer getMonth()
	{
		return month;
	}
	public void setMonth(Integer month)
	{
		this.month = month;
	}
	public Integer getDay()
	{
		return day;
	}
	public void setDay(Integer day)
	{
		this.day = day;
	}
	public BigInteger getCount()
	{
		return count;
	}
	public void setCount(BigInteger count)
	{
		this.count = count;
	}
	public BigInteger getTotal()
	{
		return total;
	}
	public void setTotal(BigInteger total)
	{
		this.total = total;
	}
	
}
