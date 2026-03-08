package com.edu.management.config;

import com.edu.management.entity.*;
import com.edu.management.enums.*;
import com.edu.management.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {
    
    private final UserRepository userRepository;
    private final CampusRepository campusRepository;
    private final TeacherRepository teacherRepository;
    private final StudentRepository studentRepository;
    private final CourseRepository courseRepository;
    private final PasswordEncoder passwordEncoder;
    
    @Override
    @Transactional
    public void run(String... args) {
        // 初始化校区
        Campus campus1 = new Campus();
        campus1.setName("主校区");
        campus1.setAddress("北京市朝阳区主校区路1号");
        campus1.setStatus(CampusStatus.ACTIVE);
        campusRepository.save(campus1);
        
        Campus campus2 = new Campus();
        campus2.setName("东校区");
        campus2.setAddress("北京市朝阳区东校区路2号");
        campus2.setStatus(CampusStatus.ACTIVE);
        campusRepository.save(campus2);
        
        Campus campus3 = new Campus();
        campus3.setName("西校区");
        campus3.setAddress("北京市海淀区西校区路3号");
        campus3.setStatus(CampusStatus.ACTIVE);
        campusRepository.save(campus3);
        
        // 初始化老师
        Teacher teacher1 = new Teacher();
        teacher1.setName("张老师");
        teacher1.setPhone("13800138001");
        teacher1.setStatus(TeacherStatus.ACTIVE);
        Set<Campus> teacher1Campuses = new HashSet<>();
        teacher1Campuses.add(campus1);
        teacher1Campuses.add(campus2);
        teacher1.setCampuses(teacher1Campuses);
        teacherRepository.save(teacher1);
        
        Teacher teacher2 = new Teacher();
        teacher2.setName("李老师");
        teacher2.setPhone("13800138002");
        teacher2.setStatus(TeacherStatus.ACTIVE);
        Set<Campus> teacher2Campuses = new HashSet<>();
        teacher2Campuses.add(campus1);
        teacher2Campuses.add(campus3);
        teacher2.setCampuses(teacher2Campuses);
        teacherRepository.save(teacher2);
        
        Teacher teacher3 = new Teacher();
        teacher3.setName("王老师");
        teacher3.setPhone("13800138003");
        teacher3.setStatus(TeacherStatus.ACTIVE);
        Set<Campus> teacher3Campuses = new HashSet<>();
        teacher3Campuses.add(campus2);
        teacher3.setCampuses(teacher3Campuses);
        teacherRepository.save(teacher3);
        
        // 初始化学生
        Student student1 = new Student();
        student1.setName("张三");
        student1.setParentName("张三家长");
        student1.setParentPhone("13900139001");
        student1.setStatus(StudentStatus.ACTIVE);
        studentRepository.save(student1);
        
        Student student2 = new Student();
        student2.setName("李四");
        student2.setParentName("李四家长");
        student2.setParentPhone("13900139002");
        student2.setStatus(StudentStatus.ACTIVE);
        studentRepository.save(student2);
        
        Student student3 = new Student();
        student3.setName("王五");
        student3.setParentName("王五家长");
        student3.setParentPhone("13900139003");
        student3.setStatus(StudentStatus.ACTIVE);
        studentRepository.save(student3);
        
        Student student4 = new Student();
        student4.setName("赵六");
        student4.setParentName("赵六家长");
        student4.setParentPhone("13900139004");
        student4.setStatus(StudentStatus.ACTIVE);
        studentRepository.save(student4);
        
        // 初始化课程
        Course course1 = new Course();
        course1.setName("小学数学");
        course1.setType(CourseType.MATH);
        course1.setUnitPrice(new BigDecimal("150.00"));
        course1.setTrialPrice(BigDecimal.ZERO);
        course1.setStatus(CourseStatus.ACTIVE);
        courseRepository.save(course1);
        
        Course course2 = new Course();
        course2.setName("小学英语");
        course2.setType(CourseType.ENGLISH);
        course2.setUnitPrice(new BigDecimal("160.00"));
        course2.setTrialPrice(new BigDecimal("9.90"));
        course2.setStatus(CourseStatus.ACTIVE);
        courseRepository.save(course2);
        
        Course course3 = new Course();
        course3.setName("初中物理");
        course3.setType(CourseType.PHYSICS);
        course3.setUnitPrice(new BigDecimal("200.00"));
        course3.setTrialPrice(BigDecimal.ZERO);
        course3.setStatus(CourseStatus.ACTIVE);
        courseRepository.save(course3);
        
        Course course4 = new Course();
        course4.setName("初中化学");
        course4.setType(CourseType.CHEMISTRY);
        course4.setUnitPrice(new BigDecimal("180.00"));
        course4.setTrialPrice(BigDecimal.ZERO);
        course4.setStatus(CourseStatus.ACTIVE);
        courseRepository.save(course4);
        
        // 初始化用户
        // 管理员
        User admin = new User();
        admin.setUsername("admin");
        admin.setPassword(passwordEncoder.encode("admin123"));
        admin.setRealName("系统管理员");
        admin.setPhone("13800000000");
        admin.setRole(RoleType.ADMIN);
        admin.setStatus(UserStatus.ACTIVE);
        userRepository.save(admin);
        
        // 教务
        User staff1 = new User();
        staff1.setUsername("staff1");
        staff1.setPassword(passwordEncoder.encode("staff123"));
        staff1.setRealName("教务一号");
        staff1.setPhone("13800000001");
        staff1.setRole(RoleType.STAFF);
        staff1.setCampusId(campus1.getId());
        staff1.setStatus(UserStatus.ACTIVE);
        userRepository.save(staff1);
        
        User staff2 = new User();
        staff2.setUsername("staff2");
        staff2.setPassword(passwordEncoder.encode("staff123"));
        staff2.setRealName("教务二号");
        staff2.setPhone("13800000002");
        staff2.setRole(RoleType.STAFF);
        staff2.setCampusId(campus2.getId());
        staff2.setStatus(UserStatus.ACTIVE);
        userRepository.save(staff2);
        
        // 老师账号
        User teacherUser1 = new User();
        teacherUser1.setUsername("teacher1");
        teacherUser1.setPassword(passwordEncoder.encode("teacher123"));
        teacherUser1.setRealName("张老师");
        teacherUser1.setPhone("13800138001");
        teacherUser1.setRole(RoleType.TEACHER);
        teacherUser1.setTeacherId(teacher1.getId());
        teacherUser1.setStatus(UserStatus.ACTIVE);
        userRepository.save(teacherUser1);
        
        User teacherUser2 = new User();
        teacherUser2.setUsername("teacher2");
        teacherUser2.setPassword(passwordEncoder.encode("teacher123"));
        teacherUser2.setRealName("李老师");
        teacherUser2.setPhone("13800138002");
        teacherUser2.setRole(RoleType.TEACHER);
        teacherUser2.setTeacherId(teacher2.getId());
        teacherUser2.setStatus(UserStatus.ACTIVE);
        userRepository.save(teacherUser2);
        
        User teacherUser3 = new User();
        teacherUser3.setUsername("teacher3");
        teacherUser3.setPassword(passwordEncoder.encode("teacher123"));
        teacherUser3.setRealName("王老师");
        teacherUser3.setPhone("13800138003");
        teacherUser3.setRole(RoleType.TEACHER);
        teacherUser3.setTeacherId(teacher3.getId());
        teacherUser3.setStatus(UserStatus.ACTIVE);
        userRepository.save(teacherUser3);
        
        System.out.println("===== 初始化数据完成 =====");
        System.out.println("管理员账号: admin / admin123");
        System.out.println("教务账号: staff1 / staff123, staff2 / staff123");
        System.out.println("老师账号: teacher1 / teacher123, teacher2 / teacher123, teacher3 / teacher123");
    }
}
