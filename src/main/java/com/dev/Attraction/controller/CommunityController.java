package com.dev.Attraction.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/community")
public class CommunityController {
	
	@GetMapping("/inquiry")
	public String inquiry() {
		return "front/community/inquiry";
	}
	
	@GetMapping("/about")
	public String about() {
		return "front/community/about";
	}
	
	@GetMapping("/notice")
	public String notice() {
		return "front/community/notice";
	}
	
	@GetMapping("/faq")
	public String faq() {
		return "front/community/faq";
	}
	
	@GetMapping("/event")
	public String event() {
		return "front/community/event";
	}

}
