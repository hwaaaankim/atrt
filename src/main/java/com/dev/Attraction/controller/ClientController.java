package com.dev.Attraction.controller;

import java.util.ArrayList;

import org.apache.commons.codec.EncoderException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.dev.Attraction.model.Client;
import com.dev.Attraction.model.Sms;
import com.dev.Attraction.repository.SmsRepository;
import com.dev.Attraction.service.ClientService;
import com.dev.Attraction.service.SmsService;

@Controller
@RequestMapping("/client")
public class ClientController {

	@Autowired
	ClientService clientService;
	
	@Autowired
	SmsRepository smsRepository;
	
	@Autowired
	SmsService smsService;
	
	@PostMapping("/insert")
	public String clientInsert(
			Client client,
			Model model
			) throws EncoderException {
		
		ArrayList<Sms> list = (ArrayList<Sms>) smsRepository.findAll();
		for(Sms phone : list) {
			smsService.sendMessage(phone.getPhone01(), "어트렉션 문의 왔습니다.");
		}
		
		if(clientService.clientInsert(client)) {
			model.addAttribute("CODE","SUCCESS");
		}else {
			model.addAttribute("CODE","FAIL");
		}
		return "front/community/inquiry";
	}
	
	@PostMapping("/japanInsert")
	@ResponseBody
	public String japanInsert(
			Client client,
			Model model
			) throws EncoderException {
		
		ArrayList<Sms> list = (ArrayList<Sms>) smsRepository.findAll();
		for(Sms phone : list) {
			smsService.sendMessage(phone.getPhone01(), "일본마케팅 문의 왔습니다.");
		}
		String msg = "";
		if(clientService.japanClientInsert(client)) {
			msg = "신청이 완료 되었습니다. 빠르게 연락 드리도록 하겠습니다.";
		}else {
			msg = "에러가 발생하였습니다. 전화문의를 이용 해 주시기 바랍니다.";
		}
		
		
		StringBuilder sb = new StringBuilder();
		sb.append("alert('"+msg+"');");
		sb.append("location.href='/japan'");
		sb.insert(0, "<script>");
		sb.append("</script>");
		return sb.toString();
	}
}

