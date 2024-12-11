package com.dev.Attraction.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/brand")
public class BrandController {

	@GetMapping("/wadiz")
	public String wadiz() {
		
		return "front/brand/wadiz";
	}
	
	@GetMapping("/openmarket")
	public String openmarket() {
		
		return "front/brand/openmarket";
	}
	
	@GetMapping("/design")
	public String design() {
		
		return "front/brand/design";
	}
	
	
}
