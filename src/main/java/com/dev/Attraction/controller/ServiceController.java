package com.dev.Attraction.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/service")
public class ServiceController {

	@RequestMapping("/searchEngineMarketing")
	public String keywordMarketing() {
		return "front/service/searchEngineMarketing";
	}
	
	
	@RequestMapping("/snsMarketing")
	public String snsMarketing() {
		return "front/service/snsMarketing";
	}
	
	@RequestMapping("/mediaMarketing")
	public String mediaMarketing() {
		return "front/service/mediaMarketing";
	}
	
	@RequestMapping("/viralMarketing")
	public String viralMarketing() {
		return "front/service/viralMarketing";
	}
}
