package com.edu.management.repository;

import com.edu.management.entity.Announcement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface AnnouncementRepository extends JpaRepository<Announcement, Long> {
    
    @Query("SELECT a FROM Announcement a WHERE a.isActive = true " +
           "AND (a.publishTime IS NULL OR a.publishTime <= :now) " +
           "AND (a.expireTime IS NULL OR a.expireTime >= :now) " +
           "ORDER BY a.isPinned DESC, a.createdAt DESC")
    List<Announcement> findActiveAnnouncements(@Param("now") LocalDateTime now);
    
    @Query("SELECT a FROM Announcement a WHERE a.isActive = true " +
           "AND (a.publishTime IS NULL OR a.publishTime <= :now) " +
           "AND (a.expireTime IS NULL OR a.expireTime >= :now) " +
           "ORDER BY a.isPinned DESC, a.createdAt DESC")
    List<Announcement> findTop5ActiveAnnouncements(@Param("now") LocalDateTime now);
    
    List<Announcement> findAllByOrderByIsPinnedDescCreatedAtDesc();
    
    List<Announcement> findByIsActiveOrderByIsPinnedDescCreatedAtDesc(Boolean isActive);
}
