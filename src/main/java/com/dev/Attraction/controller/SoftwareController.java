package com.dev.Attraction.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/software")
public class SoftwareController {

	@GetMapping("/website")
	public String website() {
		
		return "front/software/website";
	}
	
	@GetMapping("/html5game")
	public String html5game() {
		
		return "front/software/html5game";
	}
	
	@GetMapping("/program")
	public String program() {
		
		return "front/software/program";
	}
	
	@GetMapping("/maintenance")
	public String maintenance() {
		
		return "front/software/maintenance";
	}
	
	@GetMapping("/arfilter")
	public String arfilter() {
		
		return "front/software/arfilter";
	}
}
