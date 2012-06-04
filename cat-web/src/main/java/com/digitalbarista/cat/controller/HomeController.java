package com.digitalbarista.cat.controller;


import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class HomeController 
{

	@RequestMapping(value="/home")
	public String goToIndex()
	{
    SecurityContextHolder.getContext();
		return "redirect:client_one/index.html";
	}
	
}