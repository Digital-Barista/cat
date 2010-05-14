package com.digitalbarista.cat.util;

import java.io.StringWriter;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;

import org.testng.annotations.Test;

public class JAXBSerializationTest {

	@Test
	public void test1() throws JAXBException
	{
		JAXBContext ctx = JAXBContext.newInstance(GenericContainer.class,SpecificObjectOne.class,SpecificObjectTwo.class);
		
		GenericContainer gc = new GenericContainer();
		gc.setGenericObject(new SpecificObjectOne());
		
		StringWriter writer = new StringWriter();
		ctx.createMarshaller().marshal(gc, writer);
		
		System.out.println(writer.getBuffer().toString());
	}
	
	@Test
	public void test2() throws JAXBException
	{
		JAXBContext ctx = JAXBContext.newInstance(GenericContainer.class,SpecificObjectOne.class,SpecificObjectTwo.class);
		
		GenericContainer gc = new GenericContainer();
		gc.setGenericObject(new SpecificObjectTwo());
		
		StringWriter writer = new StringWriter();
		ctx.createMarshaller().marshal(gc, writer);
		
		System.out.println(writer.getBuffer().toString());
	}
	
}
