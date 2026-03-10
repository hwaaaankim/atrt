package com.dev.Attraction.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.dev.Attraction.model.EmailTemplate;

public interface EmailTemplateRepository extends JpaRepository<EmailTemplate, Long> {
}