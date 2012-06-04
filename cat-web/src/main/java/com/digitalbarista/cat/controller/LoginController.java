package com.digitalbarista.cat.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class LoginController {

	@RequestMapping(value="/login")
	public String goToLogin()
	{
		return "login";
	}
	
	@RequestMapping(value="/login/afterlogout")
	public String goToRelogin()
	{
		return "logout";
	}
}