/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.digitalbarista.cat.business;

import javax.xml.bind.annotation.XmlRootElement;

/**
 *
 * @author Falken
 */
@XmlRootElement
public class ErrorMessage {
  private Integer code;
  private String message;
  private String longMessage;

  public ErrorMessage(Integer code, String message, String longMessage)
  {
    this.code = code;
    this.message = message;
    this.longMessage = longMessage;
  }
  
  public Integer getCode() {
    return code;
  }

  public String getLongMessage() {
    return longMessage;
  }

  public String getMessage() {
    return message;
  }
}
