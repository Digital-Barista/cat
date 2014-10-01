package com.digitalbarista.cat.business;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Date: 1/6/13
 */
@XmlRootElement(name="error")
public class ServiceError
{
    private String code;
    private String message;

    public ServiceError(){

    }
    public ServiceError(String code, String message){
        this.code = code;
        this.message = message;
    }
    public ServiceError(String message){
        this.message = message;
    }
    
    @XmlAttribute
    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    @XmlAttribute
    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
