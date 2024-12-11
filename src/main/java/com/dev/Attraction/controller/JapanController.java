package com.dev.Attraction.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class JapanController {

	@GetMapping("/japan")
	public String japan() {
		
		return "japan/index";
	}
}
