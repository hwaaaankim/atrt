package com.dev.Attraction.repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.dev.Attraction.model.EmailRecipient;

public interface EmailRecipientRepository extends JpaRepository<EmailRecipient, Long> {

    Optional<EmailRecipient> findByEmail(String email);

    // 화면 무한스크롤용: 최신순(desc)
    @Query("select r " +
           "from EmailRecipient r " +
           "where (:cursor is null or r.id < :cursor) " +
           "order by r.id desc")
    List<EmailRecipient> findNext(@Param("cursor") Long cursor, Pageable pageable);

    // 발송용: 처음부터 끝까지 순차 처리(asc)
    @Query("select r " +
           "from EmailRecipient r " +
           "where (:cursor is null or r.id > :cursor) " +
           "order by r.id asc")
    List<EmailRecipient> findNextAsc(@Param("cursor") Long cursor, Pageable pageable);

    @Query("select r.email from EmailRecipient r where r.email in :emails")
    List<String> findExistingEmails(@Param("emails") Collection<String> emails);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("delete from EmailRecipient r where r.id in :ids")
    int deleteByIdInBulk(@Param("ids") Collection<Long> ids);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("delete from EmailRecipient r")
    int deleteAllInBulk();
}