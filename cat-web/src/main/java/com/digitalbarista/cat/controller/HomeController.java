package com.digitalbarista.cat.controller;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpServletRequest;

@Controller
public class HomeController {

    private @Autowired
    HttpServletRequest request;

    @RequestMapping(value = "/angular/app/")
    public String goToIndex(@ModelAttribute("model") ModelMap model) {
        SecurityContextHolder.getContext();
        model.addAttribute("contextPath", request.getContextPath());
        return "app";
    }

}