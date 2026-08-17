package com.learn.batch.strategy.impl;

import java.time.LocalDate;
import java.time.Period;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.batch.core.annotation.BeforeStep;
import org.springframework.stereotype.Component;

import com.learn.batch.model.School;
import com.learn.batch.model.Student;
import com.learn.batch.repository.SchoolRepository;
import com.learn.batch.repository.StudentRepository;
import com.learn.batch.strategy.BulkUploadStrategy;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class StudentBulkUploadStrategy implements BulkUploadStrategy<Student> {

	private final StudentRepository studentRepo;
	private final SchoolRepository schoolRepo;

	private Set<String> excelCodes = ConcurrentHashMap.newKeySet();
	private Set<String> dbCodes = ConcurrentHashMap.newKeySet();

	@Override
	public String getType() {
		return "STUDENT";
	}

	@BeforeStep
	public void beforeStep() {
		excelCodes.clear();
		dbCodes.clear();
		dbCodes.addAll(studentRepo.findAllStudentCodes());
	}

	@Override
	public List<String> validate(Map<String, String> row) {
		List<String> errors = new ArrayList<>();

		String code = row.get("Student Code");
		if (code == null || code.trim().isEmpty()) {
			errors.add("studentCode is required");
		} else {
			code = code.trim();
			
			if (!excelCodes.add(code)) {
				errors.add("Duplicate in Excel");
			}

			if (dbCodes.contains(code)) {
				errors.add("Already exists in DB");
			}
		}

		String dobRaw = row.get("Student DOB");
		if (dobRaw != null && !dobRaw.isBlank()) {
			try {
				DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");
				LocalDate.parse(dobRaw, formatter);
			} catch (Exception e) {
				errors.add("Invalid DOB format");
			}
		}

		if (row.get("School Code") == null) {
			errors.add("School Code required");
		}
		
		return errors;
	}

	@Override
	public Student mapRow(Map<String, String> row) {
		Student s = new Student();
		s.setStudentCode(trim(row.get("Student Code")));
		s.setStudentName(trim(row.get("Student Name")));
		s.setStudentStatus(trim(row.get("Student Status")));

		String dobRaw = trim(row.get("Student DOB"));
		if (dobRaw != null) {
			DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");
			s.setStudentDob(LocalDate.parse(dobRaw, formatter));
		}

		School school = new School();
		school.setSchoolCode(trim(row.get("School Code")));
		school.setSchoolName(trim(row.get("School Name")));
		s.setSchool(school);
		return s;
	}

	@Override
	public void beforeSave(Student s) {
		School school = schoolRepo.findBySchoolCode(s.getSchool().getSchoolCode()).orElseGet(() -> {
			School newS = new School();
			newS.setSchoolCode(s.getSchool().getSchoolCode());
			newS.setSchoolName(s.getSchool().getSchoolName());
			return schoolRepo.save(newS);
		});

		s.setSchool(school);
		if (s.getStudentDob() != null) {
			s.setAge(Period.between(s.getStudentDob(), LocalDate.now(ZoneId.of("Asia/Calcutta"))).getYears());
		}
	}

	@Override
	public void saveAll(List<Student> list) {
		studentRepo.saveAll(list);
	}

	private String trim(String val) {
		return val == null ? null : val.trim();
	}

}