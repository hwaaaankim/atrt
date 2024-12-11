package com.dev.Attraction.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.dev.Attraction.model.Member;
import com.dev.Attraction.model.Role;
import com.dev.Attraction.repository.MemberRepository;

@Service
public class MemberService {

	@Bean
	PasswordEncoder passwordEncoder() {
	    return new BCryptPasswordEncoder();
	}
	
	@Autowired
	private MemberRepository memberRepository;
	
	public Member save(Member member) {
		String encodedPassword = passwordEncoder().encode(member.getPassword());
		member.setPassword(encodedPassword);
		Role role = new Role();
		role.setId(1L);
		member.getRoles().add(role);
		
		return memberRepository.save(member);
		
	}
}















