package com.digitalbarista.cat.util;

import javax.xml.bind.annotation.XmlElementRef;
import javax.xml.bind.annotation.XmlElementRefs;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

@XmlRootElement(name="root")
@XmlType(name="root")
public class GenericContainer {

	private Object genericObject;

	@XmlElementRefs({
		@XmlElementRef(name="SpecificObject1", type=SpecificObjectOne.class),
		@XmlElementRef(name="SpecificObject2", type=SpecificObjectTwo.class)
	})
	public Object getGenericObject() {
		return genericObject;
	}

	public void setGenericObject(Object genericObject) {
		this.genericObject = genericObject;
	}
}
