package com.dev.Attraction.model;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import org.springframework.lang.Nullable;

import lombok.Data;

@Data
@Table(name="client")
@Entity
public class Client {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name="id")
	private Long id;
	
	@Column(name="name")
	private String name;
	
	@Column(name="phone")
	private String phone;
	
	@Column(name="email")
	@Nullable
	private String email;
	
	@Column(name="content")
	@Nullable
	private String content;
	
	@Column(name="detail")
	@Nullable
	private String detail;
	
	@Column(name="subject")
	private String subject;
	
	@Column(name="inquirydate")
	private Date inquiryDate;
	
	@Column(name="correctdate")
	private Date correctDate;
	
	@Column(name="sign")
	private Boolean sign;
	
	@Column(name="sort")
	private Boolean sort;
	
}






















