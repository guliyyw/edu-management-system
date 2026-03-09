package com.edu.management.service;

import com.edu.management.dto.TeacherDto;
import com.edu.management.dto.TeacherRequest;

import java.util.List;

public interface TeacherService {

    TeacherDto create(TeacherRequest request);

    TeacherDto update(Long id, TeacherRequest request);

    void delete(Long id);

    TeacherDto getById(Long id);

    List<TeacherDto> getAll();

    List<TeacherDto> getActiveTeachers();

    List<TeacherDto> getByCampusId(Long campusId);
}
