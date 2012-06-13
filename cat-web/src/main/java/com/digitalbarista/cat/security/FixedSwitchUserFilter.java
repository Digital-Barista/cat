package com.digitalbarista.cat.security;

import javax.servlet.http.HttpServletRequest;

import org.springframework.security.web.authentication.switchuser.SwitchUserFilter;

public class FixedSwitchUserFilter extends SwitchUserFilter {

	private String exitUserUrl = "/j_spring_security_exit_user";;
	private String switchUserUrl = "/j_spring_security_switch_user";
	
	@Override
	protected boolean requiresExitUser(HttpServletRequest request) {
		String uri = stripUri(request);
		return uri.endsWith(request.getServletPath() + this.exitUserUrl);
	}

	@Override
	protected boolean requiresSwitchUser(HttpServletRequest request) {
		String uri = stripUri(request);
		return uri.endsWith(request.getServletPath() + this.switchUserUrl);
	}

	private String stripUri(HttpServletRequest request)
	{
	  String uri = request.getRequestURI();
	  int idx = uri.indexOf(59);
	  if (idx > 0) {
	    uri = uri.substring(0, idx);
	  }
	  return uri;
	}

	@Override
	public void setExitUserUrl(String exitUserUrl) {
		this.exitUserUrl = exitUserUrl;
		super.setExitUserUrl(exitUserUrl);
	}

	@Override
	public void setSwitchUserUrl(String switchUserUrl) {
		this.switchUserUrl = switchUserUrl;
		super.setSwitchUserUrl(switchUserUrl);
	}
}
