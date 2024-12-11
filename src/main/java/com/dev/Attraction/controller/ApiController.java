package com.dev.Attraction.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.dev.Attraction.service.MemberService;

@RestController
@RequestMapping("/api/v1")
public class ApiController {

	
	@Autowired
	MemberService memberService;
	
	@PostMapping("/memberJoin")
	public String memberJoin(){
		
		return "success";
	}
}
