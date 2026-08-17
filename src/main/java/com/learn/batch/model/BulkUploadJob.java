package com.learn.batch.model;

import java.time.LocalDateTime;
import java.time.ZoneId;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
public class BulkUploadJob {

	@Id
	private Long jobId;

	private String type;

	private String filePath;

	private String status;

	private LocalDateTime createdAt = LocalDateTime.now(ZoneId.of("Asia/Calcutta"));

	public BulkUploadJob(Long jobId, String type, String filePath, String status) {
		super();
		this.jobId = jobId;
		this.type = type;
		this.filePath = filePath;
		this.status = status;
	}

}