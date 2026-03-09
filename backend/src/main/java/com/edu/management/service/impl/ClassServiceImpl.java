package com.edu.management.service.impl;

import com.edu.management.dto.*;
import com.edu.management.entity.*;
import com.edu.management.enums.ClassStatus;
import com.edu.management.enums.GradeLevel;
import com.edu.management.repository.*;
import com.edu.management.service.ClassService;
import com.edu.management.service.TravelTimeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ClassServiceImpl implements ClassService {

    private final ClassRepository classRepository;
    private final CourseRepository courseRepository;
    private final TeacherRepository teacherRepository;
    private final CampusRepository campusRepository;
    private final StudentRepository studentRepository;
    private final ClassStudentRepository classStudentRepository;
    private final LessonRepository lessonRepository;
    private final LessonAttendanceRepository lessonAttendanceRepository;
    private final TravelTimeService travelTimeService;

    @Override
    @Transactional
    public ClassDto create(ClassDto dto) {
        ClassEntity classEntity = new ClassEntity();
        classEntity.setCourse(courseRepository.findById(dto.getCourse().getId())
                .orElseThrow(() -> new RuntimeException("课程不存在")));
        classEntity.setTeacher(teacherRepository.findById(dto.getTeacher().getId())
                .orElseThrow(() -> new RuntimeException("老师不存在")));
        classEntity.setCampus(campusRepository.findById(dto.getCampus().getId())
                .orElseThrow(() -> new RuntimeException("校区不存在")));
        classEntity.setClassroom(dto.getClassroom());
        classEntity.setClassName(dto.getClassName());
        classEntity.setGradeLevel(dto.getGradeLevel() != null ? dto.getGradeLevel() : GradeLevel.GRADE_1);
        classEntity.setUnitPrice(dto.getUnitPrice());
        classEntity.setTeacherFee(dto.getTeacherFee());
        classEntity.setDefaultDayOfWeek(dto.getDefaultDayOfWeek());
        classEntity.setDefaultStartTime(dto.getDefaultStartTime());
        classEntity.setDefaultEndTime(dto.getDefaultEndTime());
        classEntity.setStatus(ClassStatus.ACTIVE);

        classEntity = classRepository.save(classEntity);
        return toDto(classEntity);
    }

    @Override
    @Transactional
    public ClassDto update(Long id, ClassDto dto) {
        ClassEntity classEntity = classRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("班级不存在"));

        classEntity.setCourse(courseRepository.findById(dto.getCourse().getId())
                .orElseThrow(() -> new RuntimeException("课程不存在")));
        classEntity.setTeacher(teacherRepository.findById(dto.getTeacher().getId())
                .orElseThrow(() -> new RuntimeException("老师不存在")));
        classEntity.setCampus(campusRepository.findById(dto.getCampus().getId())
                .orElseThrow(() -> new RuntimeException("校区不存在")));
        classEntity.setClassroom(dto.getClassroom());
        classEntity.setClassName(dto.getClassName());
        classEntity.setGradeLevel(dto.getGradeLevel() != null ? dto.getGradeLevel() : classEntity.getGradeLevel());
        classEntity.setUnitPrice(dto.getUnitPrice());
        classEntity.setTeacherFee(dto.getTeacherFee());
        classEntity.setDefaultDayOfWeek(dto.getDefaultDayOfWeek());
        classEntity.setDefaultStartTime(dto.getDefaultStartTime());
        classEntity.setDefaultEndTime(dto.getDefaultEndTime());
        if (dto.getStatus() != null) {
            classEntity.setStatus(dto.getStatus());
        }

        classEntity = classRepository.save(classEntity);
        return toDto(classEntity);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        ClassEntity classEntity = classRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("班级不存在"));

        // 彻底删除：先删除关联的课节签到记录
        List<Lesson> lessons = lessonRepository.findByClassEntityId(id);
        for (Lesson lesson : lessons) {
            lessonAttendanceRepository.deleteByLessonId(lesson.getId());
        }

        // 删除关联的课节
        lessonRepository.deleteByClassEntityId(id);

        // 删除班级学生关联
        classStudentRepository.deleteByClassEntityId(id);

        // 最后删除班级
        classRepository.delete(classEntity);
    }

    @Override
    @Transactional(readOnly = true)
    public ClassDto getById(Long id) {
        log.info("[班级服务] 获取班级详情，ID: {}", id);
        ClassEntity classEntity = classRepository.findByIdWithStudents(id)
                .orElseThrow(() -> new RuntimeException("班级不存在"));
        log.info("[班级服务] 查询到班级: {}，学生数量: {}", classEntity.getClassName(),
                classEntity.getStudents() != null ? classEntity.getStudents().size() : 0);
        ClassDto dto = toDto(classEntity);
        log.info("[班级服务] 转换后的DTO学生数量: {}", dto.getStudents() != null ? dto.getStudents().size() : 0);
        return dto;
    }

    @Override
    @Transactional(readOnly = true)
    public List<ClassDto> getAll() {
        log.info("[班级服务] 获取所有班级");
        List<ClassEntity> entities = classRepository.findAllWithStudents(ClassStatus.ACTIVE);
        log.info("[班级服务] 查询到班级数量: {}", entities.size());
        return entities.stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ClassDto> getByTeacherId(Long teacherId) {
        return classRepository.findByTeacherIdAndStatusWithStudents(teacherId, ClassStatus.ACTIVE).stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ClassDto> getByCampusId(Long campusId) {
        return classRepository.findByCampusIdAndStatusWithStudents(campusId, ClassStatus.ACTIVE).stream()
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
        // 由于班级时间现在不固定，冲突检测移到课节级别
        return false;
    }

    @Override
    @Transactional
    public void addStudentToClass(Long classId, Long studentId, Boolean isTrial) {
        log.info("[班级服务] 添加学生到班级，班级ID: {}，学生ID: {}，是否试课: {}", classId, studentId, isTrial);

        ClassEntity classEntity = classRepository.findById(classId)
                .orElseThrow(() -> new RuntimeException("班级不存在"));
        log.info("[班级服务] 找到班级: {}", classEntity.getClassName());

        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new RuntimeException("学生不存在"));
        log.info("[班级服务] 找到学生: {}", student.getName());

        if (classStudentRepository.existsByClassEntityIdAndStudentId(classId, studentId)) {
            log.warn("[班级服务] 学生已在班级中");
            throw new RuntimeException("学生已在班级中");
        }

        ClassStudent classStudent = new ClassStudent();
        classStudent.setClassEntity(classEntity);
        classStudent.setStudent(student);
        classStudent.setIsTrial(isTrial);

        ClassStudent saved = classStudentRepository.save(classStudent);
        log.info("[班级服务] 学生添加成功，ClassStudent ID: {}", saved.getId());

        // 强制刷新班级实体，确保学生集合被更新
        classEntity.getStudents().add(saved);
        log.info("[班级服务] 班级学生集合已更新，当前数量: {}", classEntity.getStudents().size());
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

    public ClassDto toDto(ClassEntity classEntity) {
        CourseDto courseDto = CourseDto.builder()
                .id(classEntity.getCourse().getId())
                .name(classEntity.getCourse().getName())
                .type(classEntity.getCourse().getType())
                .unitPrice(classEntity.getCourse().getUnitPrice())
                .trialPrice(classEntity.getCourse().getTrialPrice())
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
                .map(cs -> {
                    Student student = cs.getStudent();
                    return ClassStudentDto.builder()
                            .id(cs.getId())
                            .student(StudentDto.builder()
                                    .id(student.getId())
                                    .name(student.getName())
                                    .parentName(student.getParentName())
                                    .parentPhone(student.getParentPhone())
                                    .gradeLevel(student.getGradeLevel())
                                    .status(student.getStatus())
                                    .createdAt(student.getCreatedAt())
                                    .build())
                            .isTrial(cs.getIsTrial())
                            .convertedAt(cs.getConvertedAt())
                            .createdAt(cs.getCreatedAt())
                            .build();
                })
                .collect(Collectors.toList());

        return ClassDto.builder()
                .id(classEntity.getId())
                .course(courseDto)
                .teacher(teacherDto)
                .campus(campusDto)
                .classroom(classEntity.getClassroom())
                .className(classEntity.getClassName())
                .gradeLevel(classEntity.getGradeLevel())
                .unitPrice(classEntity.getUnitPrice())
                .teacherFee(classEntity.getTeacherFee())
                .defaultDayOfWeek(classEntity.getDefaultDayOfWeek())
                .defaultStartTime(classEntity.getDefaultStartTime())
                .defaultEndTime(classEntity.getDefaultEndTime())
                .status(classEntity.getStatus())
                .students(studentDtos)
                .createdAt(classEntity.getCreatedAt())
                .build();
    }
}
