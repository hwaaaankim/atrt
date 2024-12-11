package com.dev.Attraction.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.dev.Attraction.model.Member;

@Repository
public interface MemberRepository extends JpaRepository<Member, Long>{
	
}
