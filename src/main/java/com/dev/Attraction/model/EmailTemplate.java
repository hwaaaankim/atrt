package com.dev.Attraction.model;

import java.time.LocalDateTime;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;
import javax.persistence.Table;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name="tb_email_template")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class EmailTemplate {

    @Id
    private Long id; // 1 고정 사용

    @Column(name="subject", nullable=false, length=200)
    private String subject;

    @Lob
    @Column(name="html", nullable=false)
    private String html;

    @Column(name="updated_at", nullable=false)
    private LocalDateTime updatedAt;

    @PrePersist @PreUpdate
    void touch() {
        updatedAt = LocalDateTime.now();
    }
}
