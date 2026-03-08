package com.edu.management.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "travel_times")
public class TravelTime {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "teacher_id", nullable = false)
    private Teacher teacher;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "from_campus_id", nullable = false)
    private Campus fromCampus;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "to_campus_id", nullable = false)
    private Campus toCampus;
    
    // 路程时间（分钟）
    @Column(nullable = false)
    private Integer travelMinutes;
    
    // 生效日期
    @Column(nullable = false)
    private LocalDate effectiveDate;
    
    @CreationTimestamp
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    private LocalDateTime updatedAt;
}
