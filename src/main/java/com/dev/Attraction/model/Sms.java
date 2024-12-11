package com.dev.Attraction.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import lombok.Data;

@Data
@Entity
@Table(name="setting")
public class Sms {
	
	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	@Column(name="id")
	private Long id;
	
	@Column(name="phone01")
	private String phone01;
	
	@Column(name="phone02")
	private String phone02;
	
	@Column(name="phone03")
	private String phone03;
}
