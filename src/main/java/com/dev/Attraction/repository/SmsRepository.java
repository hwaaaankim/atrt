package com.dev.Attraction.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.dev.Attraction.model.Sms;

@Repository
public interface SmsRepository extends JpaRepository<Sms, Long>{

}
