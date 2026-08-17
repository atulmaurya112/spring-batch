package com.learn.batch.model;

import java.time.LocalDate;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import lombok.Data;

@Entity
@Data
public class Student {

	@Id
	@GeneratedValue
	private Long id;

	@Column(unique = true)
	private String studentCode;

	private String studentName;
	private LocalDate studentDob;
	private Integer age;
	private String studentStatus;

	@ManyToOne
	private School school;

}