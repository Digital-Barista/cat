package com.digitalbarista.cat.controller;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class LogoutController {

	@RequestMapping(value="/logout")
	public void logout()
	{
		SecurityContextHolder.getContext().setAuthentication(null);
	}
	
}