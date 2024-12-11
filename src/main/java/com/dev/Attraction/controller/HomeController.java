package com.dev.Attraction.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class HomeController {

	@RequestMapping({"/", "/index",""})
	public String index() {
		return "front/index";
	}
	
	@RequestMapping("/aboutInsta")
	public String aboutInsta() {
		return "front/instagram/about";
	}
	
	@RequestMapping("/productInsta")
	public String productInsta() {
		return "front/instagram/product";
	}
	
	@RequestMapping("/effectInsta")
	public String effectInsta() {
		return "front/instagram/effect";
	}
	
	@RequestMapping("/portInsta")
	public String portInsta() {
		return "front/instagram/portfolio";
	}
	
	@RequestMapping("/commentInsta")
	public String commentInsta() {
		return "front/instagram/comment";
	}
	@RequestMapping("/hospital")
	public String medicalHome(
			Model model
			) {
		model.addAttribute("page", 0);
		return "front/medical/medical";
	}
	
	@RequestMapping("/hospital/{page}")
	public String medicalPage(
			@PathVariable(required=false) int page,
			Model model
			) {
		model.addAttribute("page", page);
		return "front/medical/medical";
	}
	
	
}
