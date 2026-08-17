package com.learn.batch.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.learn.batch.model.School;

public interface SchoolRepository extends JpaRepository<School, Long> {

	Optional<School> findBySchoolCode(String code);

}