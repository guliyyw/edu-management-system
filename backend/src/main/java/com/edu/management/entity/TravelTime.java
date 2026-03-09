package com.edu.management.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Objects;

@Getter
@Setter
@ToString(exclude = {"teacher", "fromCampus", "toCampus"})
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TravelTime that = (TravelTime) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
