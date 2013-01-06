package com.digitalbarista.cat.business;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElementRef;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.List;

/**
 * Date: 1/6/13
 */
@XmlRootElement(name="response")
public class ServiceResponse
{
    private ServiceResponseMetadata metadata;
    private Object result;
    private List<ServiceResponseError> errors;

    @XmlElementRef
    public ServiceResponseMetadata getMetadata() {
        return metadata;
    }

    public void setMetadata(ServiceResponseMetadata metadata) {
        this.metadata = metadata;
    }

    public Object getResult() {
        return result;
    }

    public void setResult(Object result) {
        this.result = result;
    }

    @XmlElementWrapper(name="errors")
    @XmlElementRef
    public List<ServiceResponseError> getErrors() {
        return errors;
    }

    public void setErrors(List<ServiceResponseError> errors) {
        this.errors = errors;
    }
}
