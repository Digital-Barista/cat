package com.digitalbarista.cat.twitter.mbean;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name="TokenPair")
public class TokenPair {
	private String token;
	private String secret;
	
	public void setToken(String token) {
		this.token = token;
	}

	public void setSecret(String secret) {
		this.secret = secret;
	}

	@XmlAttribute(name="Token")
	public String getToken() {
		return token;
	}
	
	@XmlAttribute(name="Secret")
	public String getSecret() {
		return secret;
	}
}
