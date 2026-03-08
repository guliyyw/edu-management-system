package com.edu.management.entity;

import com.edu.management.enums.ClassStatus;
import com.edu.management.enums.GradeLevel;
import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Data
@Entity
@Table(name = "classes")
public class ClassEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "course_id", nullable = false)
    private Course course;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "teacher_id", nullable = false)
    private Teacher teacher;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "campus_id", nullable = false)
    private Campus campus;

    @Column(length = 50)
    private String classroom;

    // 班级名称（如：六年级英语A班）
    @Column(length = 100)
    private String className;

    // 年级
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private GradeLevel gradeLevel = GradeLevel.GRADE_1;

    // 班级单价（覆盖课程的单价，支持同课程不同班不同收费）
    @Column(precision = 10, scale = 2)
    private java.math.BigDecimal unitPrice;

    // 默认上课时间（用于排课参考，实际课节时间可灵活调整）
    // 上课星期几 (1-7)，null表示不固定
    @Column
    private Integer defaultDayOfWeek;

    // 默认开始时间
    @Column
    private java.time.LocalTime defaultStartTime;

    // 默认结束时间
    @Column
    private java.time.LocalTime defaultEndTime;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ClassStatus status = ClassStatus.ACTIVE;

    @OneToMany(mappedBy = "classEntity", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<ClassStudent> students = new HashSet<>();

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;
}
