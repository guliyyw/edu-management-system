package com.edu.management.service.impl;

import com.edu.management.dto.*;
import com.edu.management.entity.*;
import com.edu.management.enums.AttendanceStatus;
import com.edu.management.enums.ClassStatus;
import com.edu.management.enums.LessonStatus;
import com.edu.management.repository.*;
import com.edu.management.service.LessonService;
import com.edu.management.service.TravelTimeService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class LessonServiceImpl implements LessonService {
    
    private final LessonRepository lessonRepository;
    private final ClassRepository classRepository;
    private final StudentRepository studentRepository;
    private final LessonAttendanceRepository attendanceRepository;
    private final TravelTimeService travelTimeService;
    
    @Override
    @Transactional
    public LessonDto create(LessonDto dto) {
        ClassEntity classEntity = classRepository.findById(dto.getClassInfo().getId())
                .orElseThrow(() -> new RuntimeException("班级不存在"));
        
        // 检查跨校区冲突
        if (checkCrossCampusConflict(classEntity.getTeacher().getId(), classEntity.getCampus().getId(), 
                dto.getDate(), dto.getStartTime(), dto.getEndTime(), null)) {
            throw new RuntimeException("跨校区排课冲突，请检查路程时间");
        }
        
        Lesson lesson = new Lesson();
        lesson.setClassEntity(classEntity);
        lesson.setDate(dto.getDate());
        lesson.setStartTime(dto.getStartTime());
        lesson.setEndTime(dto.getEndTime());
        lesson.setClassroom(dto.getClassroom());
        lesson.setStatus(LessonStatus.SCHEDULED);
        
        lesson = lessonRepository.save(lesson);
        
        // 创建学生签到记录
        createAttendanceRecords(lesson, classEntity);
        
        return toDto(lesson);
    }
    
    @Override
    @Transactional
    public LessonDto update(Long id, LessonDto dto) {
        Lesson lesson = lessonRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("课节不存在"));
        
        ClassEntity classEntity = lesson.getClassEntity();
        
        // 检查跨校区冲突
        if (checkCrossCampusConflict(classEntity.getTeacher().getId(), classEntity.getCampus().getId(), 
                dto.getDate(), dto.getStartTime(), dto.getEndTime(), id)) {
            throw new RuntimeException("跨校区排课冲突，请检查路程时间");
        }
        
        lesson.setDate(dto.getDate());
        lesson.setStartTime(dto.getStartTime());
        lesson.setEndTime(dto.getEndTime());
        lesson.setClassroom(dto.getClassroom());
        
        lesson = lessonRepository.save(lesson);
        return toDto(lesson);
    }
    
    @Override
    @Transactional
    public void delete(Long id) {
        Lesson lesson = lessonRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("课节不存在"));
        lesson.setStatus(LessonStatus.CANCELLED);
        lessonRepository.save(lesson);
    }
    
    @Override
    @Transactional(readOnly = true)
    public LessonDto getById(Long id) {
        Lesson lesson = lessonRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("课节不存在"));
        return toDto(lesson);
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<LessonDto> getByClassId(Long classId) {
        return lessonRepository.findByClassEntityId(classId).stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<LessonDto> getByTeacherIdAndDate(Long teacherId, LocalDate date) {
        return lessonRepository.findByTeacherIdAndDate(teacherId, date).stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<LessonDto> getByTeacherIdAndDateRange(Long teacherId, LocalDate startDate, LocalDate endDate) {
        return lessonRepository.findByTeacherIdAndDateBetween(teacherId, startDate, endDate).stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<LessonDto> getByCampusIdAndDate(Long campusId, LocalDate date) {
        return lessonRepository.findByCampusIdAndDate(campusId, date).stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<LessonDto> getAllLessons() {
        return lessonRepository.findAll().stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }
    
    @Override
    @Transactional
    public List<LessonDto> generateLessons(Long classId, LocalDate startDate, LocalDate endDate) {
        ClassEntity classEntity = classRepository.findById(classId)
                .orElseThrow(() -> new RuntimeException("班级不存在"));

        // 如果没有设置默认上课时间，无法批量排课
        if (classEntity.getDefaultDayOfWeek() == null ||
            classEntity.getDefaultStartTime() == null ||
            classEntity.getDefaultEndTime() == null) {
            throw new RuntimeException("班级未设置默认上课时间，无法批量排课。请先设置班级的默认上课时间。");
        }

        List<LessonDto> generatedLessons = new ArrayList<>();

        LocalDate currentDate = startDate;
        while (!currentDate.isAfter(endDate)) {
            // 检查是否是上课日
            if (currentDate.getDayOfWeek().getValue() == classEntity.getDefaultDayOfWeek()) {
                Lesson lesson = new Lesson();
                lesson.setClassEntity(classEntity);
                lesson.setDate(currentDate);
                lesson.setStartTime(classEntity.getDefaultStartTime());
                lesson.setEndTime(classEntity.getDefaultEndTime());
                lesson.setClassroom(classEntity.getClassroom());
                lesson.setStatus(LessonStatus.SCHEDULED);

                lesson = lessonRepository.save(lesson);
                createAttendanceRecords(lesson, classEntity);
                generatedLessons.add(toDto(lesson));
            }
            currentDate = currentDate.plusDays(1);
        }

        return generatedLessons;
    }
    
    @Override
    @Transactional
    public void markAttendance(Long lessonId, Long studentId, AttendanceStatus status, String remark) {
        LessonAttendance attendance = attendanceRepository.findByLessonIdAndStudentId(lessonId, studentId)
                .orElseThrow(() -> new RuntimeException("签到记录不存在"));
        
        attendance.setStatus(status);
        attendance.setRemark(remark);
        
        attendanceRepository.save(attendance);
    }
    
    @Override
    @Transactional
    public void modifyAttendance(Long lessonId, Long studentId, AttendanceStatus status, 
                                  String remark, String modifyReason, Long modifiedBy) {
        LessonAttendance attendance = attendanceRepository.findByLessonIdAndStudentId(lessonId, studentId)
                .orElseThrow(() -> new RuntimeException("签到记录不存在"));
        
        attendance.setStatus(status);
        attendance.setRemark(remark);
        attendance.setModifiedBy(modifiedBy);
        attendance.setModifiedAt(LocalDateTime.now());
        attendance.setModifyReason(modifyReason);
        
        attendanceRepository.save(attendance);
        
        // TODO: 发送通知给老师
    }
    
    @Override
    @Transactional(readOnly = true)
    public boolean checkCrossCampusConflict(Long teacherId, Long campusId, LocalDate date, 
                                             LocalTime startTime, LocalTime endTime, Long excludeLessonId) {
        // 获取老师当天的所有课节
        List<Lesson> todayLessons = lessonRepository.findByTeacherIdAndDate(teacherId, date);
        
        for (Lesson lesson : todayLessons) {
            if (excludeLessonId != null && lesson.getId().equals(excludeLessonId)) {
                continue;
            }
            
            Long fromCampusId = lesson.getClassEntity().getCampus().getId();
            
            // 如果是同一校区，检查时间重叠
            if (fromCampusId.equals(campusId)) {
                if (startTime.isBefore(lesson.getEndTime()) && endTime.isAfter(lesson.getStartTime())) {
                    return true;
                }
            } else {
                // 跨校区，需要计算路程时间
                TravelTimeDto travelTime = travelTimeService.getEffectiveTravelTime(
                        teacherId, fromCampusId, campusId, date);
                
                int travelMinutes = travelTime != null ? travelTime.getTravelMinutes() : 30;
                
                // 冲突公式：上一节课结束时间 + 路程时间 > 下一节课开始时间
                LocalTime arrivalTime = lesson.getEndTime().plusMinutes(travelMinutes);
                if (arrivalTime.isAfter(startTime)) {
                    return true;
                }
            }
        }
        
        return false;
    }
    
    private void createAttendanceRecords(Lesson lesson, ClassEntity classEntity) {
        for (ClassStudent cs : classEntity.getStudents()) {
            LessonAttendance attendance = new LessonAttendance();
            attendance.setLesson(lesson);
            attendance.setStudent(cs.getStudent());
            attendance.setStatus(AttendanceStatus.PENDING);
            attendance.setIsTrial(cs.getIsTrial());
            attendanceRepository.save(attendance);
        }
    }
    
    private LessonDto toDto(Lesson lesson) {
        ClassEntity classEntity = lesson.getClassEntity();
        
        ClassDto classDto = ClassDto.builder()
                .id(classEntity.getId())
                .course(CourseDto.builder()
                        .id(classEntity.getCourse().getId())
                        .name(classEntity.getCourse().getName())
                        .build())
                .teacher(TeacherDto.builder()
                        .id(classEntity.getTeacher().getId())
                        .name(classEntity.getTeacher().getName())
                        .build())
                .campus(CampusDto.builder()
                        .id(classEntity.getCampus().getId())
                        .name(classEntity.getCampus().getName())
                        .build())
                .build();
        
        List<LessonAttendanceDto> attendanceDtos = lesson.getAttendances().stream()
                .map(a -> LessonAttendanceDto.builder()
                        .id(a.getId())
                        .student(StudentDto.builder()
                                .id(a.getStudent().getId())
                                .name(a.getStudent().getName())
                                .parentPhone(a.getStudent().getParentPhone())
                                .build())
                        .status(a.getStatus())
                        .remark(a.getRemark())
                        .isTrial(a.getIsTrial())
                        .modifiedBy(a.getModifiedBy())
                        .modifiedAt(a.getModifiedAt())
                        .modifyReason(a.getModifyReason())
                        .build())
                .collect(Collectors.toList());
        
        return LessonDto.builder()
                .id(lesson.getId())
                .classInfo(classDto)
                .date(lesson.getDate())
                .startTime(lesson.getStartTime())
                .endTime(lesson.getEndTime())
                .classroom(lesson.getClassroom())
                .status(lesson.getStatus())
                .attendances(attendanceDtos)
                .createdAt(lesson.getCreatedAt())
                .build();
    }
}
