package com.digitalbarista.cat.business;

import javax.xml.bind.annotation.XmlElementRef;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.ArrayList;
import java.util.List;

/**
 * Date: 1/6/13
 */
@XmlRootElement(name="response")
public class ServiceResponse
{
    private ServiceMetadata metadata;
    private Object result;
    private List<ServiceError> errors;

    public void addError(ServiceError error){
        if (errors == null){
            errors = new ArrayList<ServiceError>();
        }
        errors.add(error);
    }
    @XmlElementRef
    public ServiceMetadata getMetadata() {
        return metadata;
    }

    public void setMetadata(ServiceMetadata metadata) {
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
    public List<ServiceError> getErrors() {
        return errors;
    }

    public void setErrors(List<ServiceError> errors) {
        this.errors = errors;
    }
}
