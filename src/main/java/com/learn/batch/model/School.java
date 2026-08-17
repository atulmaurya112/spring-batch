package com.learn.batch.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@NoArgsConstructor
public class School {

	@Id
	@GeneratedValue
	private Long id;

	@Column(unique = true)
	private String schoolCode;

	private String schoolName;

}