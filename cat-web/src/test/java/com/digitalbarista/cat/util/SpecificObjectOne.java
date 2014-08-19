package com.digitalbarista.cat.util;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

@XmlRootElement(name="SpecificObject1")
@XmlType(name="SpecificObject1")
public class SpecificObjectOne {

	private String name;

	@XmlAttribute
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
	
}
