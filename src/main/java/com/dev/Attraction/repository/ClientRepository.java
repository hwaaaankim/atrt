package com.dev.Attraction.repository;

import java.util.Date;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.dev.Attraction.model.Client;

public interface ClientRepository extends JpaRepository<Client, Long>{

	Page<Client> findBySortOrderByInquiryDateDesc(Pageable pageable, Boolean sort);

    Page<Client> findByNameAndSortOrderByInquiryDateDesc(Pageable pageable, String name, Boolean sort);

    Page<Client> findByPhoneAndSortOrderByInquiryDateDesc(Pageable pageable, String phone, Boolean sort);

    Page<Client> findByEmailAndSortOrderByInquiryDateDesc(Pageable pageable, String email, Boolean sort);

    Page<Client> findBySubjectAndSortOrderByInquiryDateDesc(Pageable pageable, String subject, Boolean sort);

    Page<Client> findByInquiryDateBetweenAndSortOrderByInquiryDateDesc(Pageable pageable, Date startDate, Date endDate, Boolean sort);
	
}
