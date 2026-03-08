package com.edu.management.service.impl;

import com.edu.management.dto.*;
import com.edu.management.entity.*;
import com.edu.management.enums.ClassStatus;
import com.edu.management.repository.*;
import com.edu.management.service.ClassService;
import com.edu.management.service.TravelTimeService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ClassServiceImpl implements ClassService {
    
    private final ClassRepository classRepository;
    private final CourseRepository courseRepository;
    private final TeacherRepository teacherRepository;
    private final CampusRepository campusRepository;
    private final StudentRepository studentRepository;
    private final ClassStudentRepository classStudentRepository;
    private final TravelTimeService travelTimeService;
    
    @Override
    @Transactional
    public ClassDto create(ClassDto dto) {
        // 检查排课冲突
        if (checkScheduleConflict(dto.getTeacher().getId(), dto.getCampus().getId(), 
                dto.getDayOfWeek(), dto.getStartTime(), dto.getEndTime(), null)) {
            throw new RuntimeException("排课时间冲突");
        }
        
        ClassEntity classEntity = new ClassEntity();
        classEntity.setCourse(courseRepository.findById(dto.getCourse().getId())
                .orElseThrow(() -> new RuntimeException("课程不存在")));
        classEntity.setTeacher(teacherRepository.findById(dto.getTeacher().getId())
                .orElseThrow(() -> new RuntimeException("老师不存在")));
        classEntity.setCampus(campusRepository.findById(dto.getCampus().getId())
                .orElseThrow(() -> new RuntimeException("校区不存在")));
        classEntity.setClassroom(dto.getClassroom());
        classEntity.setDayOfWeek(dto.getDayOfWeek());
        classEntity.setStartTime(dto.getStartTime());
        classEntity.setEndTime(dto.getEndTime());
        classEntity.setStatus(ClassStatus.ACTIVE);
        
        classEntity = classRepository.save(classEntity);
        return toDto(classEntity);
    }
    
    @Override
    @Transactional
    public ClassDto update(Long id, ClassDto dto) {
        ClassEntity classEntity = classRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("班级不存在"));
        
        // 检查排课冲突
        if (checkScheduleConflict(dto.getTeacher().getId(), dto.getCampus().getId(), 
                dto.getDayOfWeek(), dto.getStartTime(), dto.getEndTime(), id)) {
            throw new RuntimeException("排课时间冲突");
        }
        
        classEntity.setCourse(courseRepository.findById(dto.getCourse().getId())
                .orElseThrow(() -> new RuntimeException("课程不存在")));
        classEntity.setTeacher(teacherRepository.findById(dto.getTeacher().getId())
                .orElseThrow(() -> new RuntimeException("老师不存在")));
        classEntity.setCampus(campusRepository.findById(dto.getCampus().getId())
                .orElseThrow(() -> new RuntimeException("校区不存在")));
        classEntity.setClassroom(dto.getClassroom());
        classEntity.setDayOfWeek(dto.getDayOfWeek());
        classEntity.setStartTime(dto.getStartTime());
        classEntity.setEndTime(dto.getEndTime());
        
        classEntity = classRepository.save(classEntity);
        return toDto(classEntity);
    }
    
    @Override
    @Transactional
    public void delete(Long id) {
        ClassEntity classEntity = classRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("班级不存在"));
        classEntity.setStatus(ClassStatus.CANCELLED);
        classRepository.save(classEntity);
    }
    
    @Override
    @Transactional(readOnly = true)
    public ClassDto getById(Long id) {
        ClassEntity classEntity = classRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("班级不存在"));
        return toDto(classEntity);
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<ClassDto> getAll() {
        return classRepository.findAll().stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<ClassDto> getByTeacherId(Long teacherId) {
        return classRepository.findByTeacherIdAndStatus(teacherId, ClassStatus.ACTIVE).stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<ClassDto> getByCampusId(Long campusId) {
        return classRepository.findByCampusIdAndStatus(campusId, ClassStatus.ACTIVE).stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<ClassDto> getByStudentId(Long studentId) {
        return classRepository.findByStudentIdAndStatus(studentId, ClassStatus.ACTIVE).stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }
    
    @Override
    @Transactional(readOnly = true)
    public boolean checkScheduleConflict(Long teacherId, Long campusId, Integer dayOfWeek, 
                                          LocalTime startTime, LocalTime endTime, Long excludeClassId) {
        List<ClassEntity> existingClasses = classRepository.findByTeacherIdAndDayOfWeekAndStatus(
                teacherId, dayOfWeek, ClassStatus.ACTIVE);
        
        for (ClassEntity existing : existingClasses) {
            if (excludeClassId != null && existing.getId().equals(excludeClassId)) {
                continue;
            }
            
            // 检查时间是否重叠
            if (startTime.isBefore(existing.getEndTime()) && endTime.isAfter(existing.getStartTime())) {
                return true;
            }
        }
        
        return false;
    }
    
    @Override
    @Transactional
    public void addStudentToClass(Long classId, Long studentId, Boolean isTrial) {
        ClassEntity classEntity = classRepository.findById(classId)
                .orElseThrow(() -> new RuntimeException("班级不存在"));
        
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new RuntimeException("学生不存在"));
        
        if (classStudentRepository.existsByClassEntityIdAndStudentId(classId, studentId)) {
            throw new RuntimeException("学生已在班级中");
        }
        
        ClassStudent classStudent = new ClassStudent();
        classStudent.setClassEntity(classEntity);
        classStudent.setStudent(student);
        classStudent.setIsTrial(isTrial);
        
        classStudentRepository.save(classStudent);
    }
    
    @Override
    @Transactional
    public void removeStudentFromClass(Long classId, Long studentId) {
        ClassStudent classStudent = classStudentRepository.findByClassEntityIdAndStudentId(classId, studentId)
                .orElseThrow(() -> new RuntimeException("学生不在班级中"));
        
        classStudentRepository.delete(classStudent);
    }
    
    @Override
    @Transactional
    public void convertTrialStudent(Long classId, Long studentId) {
        ClassStudent classStudent = classStudentRepository.findByClassEntityIdAndStudentId(classId, studentId)
                .orElseThrow(() -> new RuntimeException("学生不在班级中"));
        
        classStudent.setIsTrial(false);
        classStudent.setConvertedAt(java.time.LocalDateTime.now());
        classStudentRepository.save(classStudent);
    }
    
    private ClassDto toDto(ClassEntity classEntity) {
        CourseDto courseDto = CourseDto.builder()
                .id(classEntity.getCourse().getId())
                .name(classEntity.getCourse().getName())
                .type(classEntity.getCourse().getType())
                .unitPrice(classEntity.getCourse().getUnitPrice())
                .build();
        
        TeacherDto teacherDto = TeacherDto.builder()
                .id(classEntity.getTeacher().getId())
                .name(classEntity.getTeacher().getName())
                .phone(classEntity.getTeacher().getPhone())
                .build();
        
        CampusDto campusDto = CampusDto.builder()
                .id(classEntity.getCampus().getId())
                .name(classEntity.getCampus().getName())
                .build();
        
        List<ClassStudentDto> studentDtos = classEntity.getStudents().stream()
                .map(cs -> ClassStudentDto.builder()
                        .id(cs.getId())
                        .student(StudentDto.builder()
                                .id(cs.getStudent().getId())
                                .name(cs.getStudent().getName())
                                .parentPhone(cs.getStudent().getParentPhone())
                                .build())
                        .isTrial(cs.getIsTrial())
                        .convertedAt(cs.getConvertedAt())
                        .build())
                .collect(Collectors.toList());
        
        return ClassDto.builder()
                .id(classEntity.getId())
                .course(courseDto)
                .teacher(teacherDto)
                .campus(campusDto)
                .classroom(classEntity.getClassroom())
                .dayOfWeek(classEntity.getDayOfWeek())
                .startTime(classEntity.getStartTime())
                .endTime(classEntity.getEndTime())
                .status(classEntity.getStatus())
                .students(studentDtos)
                .createdAt(classEntity.getCreatedAt())
                .build();
    }
}
