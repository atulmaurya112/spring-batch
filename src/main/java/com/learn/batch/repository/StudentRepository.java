package com.learn.batch.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.learn.batch.model.Student;

public interface StudentRepository extends JpaRepository<Student, Long> {

	@Query("SELECT s.studentCode FROM Student s")
	List<String> findAllStudentCodes();

}