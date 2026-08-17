package com.learn.batch.controller;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.PosixFilePermission;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.springframework.batch.core.job.Job;
import org.springframework.batch.core.job.JobExecution;
import org.springframework.batch.core.job.parameters.JobParameters;
import org.springframework.batch.core.job.parameters.JobParametersBuilder;
import org.springframework.batch.core.launch.JobOperator;
import org.springframework.batch.core.launch.NoSuchJobException;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.learn.batch.model.BulkUploadJob;
import com.learn.batch.repository.BulkUploadJobRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/bulk")
@RequiredArgsConstructor
@Slf4j
public class BulkController {

	private final Job job;
	private final JobOperator jobOperator;
	private final JobRepository jobRepository;
	private final BulkUploadJobRepository repo;

	@PostMapping("/upload/{type}")
	public Map<String, Object> upload(@PathVariable String type, @RequestParam MultipartFile file) {
	    Path temp = null;
	    try {
	        if (file.isEmpty()) {
	            throw new FileNotFoundException("File is empty");
	        }

	        String fileName = file.getOriginalFilename();
	        if (fileName == null || !fileName.toLowerCase().endsWith(".xlsx")) {
	            throw new UnsupportedOperationException("Only .xlsx files allowed");
	        }

	        Path tempDir = createTempDirectory();
	        temp = Files.createTempFile(tempDir, "upload-", ".xlsx");
	        file.transferTo(temp);

	        log.info("Starting bulk job for type: {}", type);

	        JobParameters params = new JobParametersBuilder()
	                .addString("file", temp.toString())
	                .addString("type", type)
	                .addLong("time", System.currentTimeMillis())
	                .toJobParameters();

	        JobExecution execution = jobOperator.start(job, params);

	        log.info("Job started with ID: {}", execution.getId());

	        return Map.of("jobId", execution.getId());
	    } catch (Exception e) {
	        log.error("Upload failed", e);
	        if (temp != null) {
	            deleteTempFile(temp);
	        }
	        
	        throw new RuntimeException("Upload failed: " + e.getMessage(), e);
	    }
	}
	
	private Path createTempDirectory() throws IOException {
	    Path tempDir = Paths.get(System.getProperty("java.io.tmpdir"), "myapp");
	    Files.createDirectories(tempDir);
	    setRestrictedPermissions(tempDir);
	    return tempDir;
	}
	
	private void setRestrictedPermissions(Path tempDir) throws IOException {
		if (Files.getFileAttributeView(tempDir, java.nio.file.attribute.PosixFileAttributeView.class) != null) {
			Files.setPosixFilePermissions(tempDir, Set.of(PosixFilePermission.OWNER_READ, PosixFilePermission.OWNER_WRITE, PosixFilePermission.OWNER_EXECUTE));
		}
	}

	private void deleteTempFile(Path temp) {
	    try {
	        Files.deleteIfExists(temp);
	    } catch (IOException e) {
	        log.warn("Could not delete temporary file: {}", temp, e);
	    }
	}

	@GetMapping("/status/{jobId}")
	public Map<String, Object> getStatus(@PathVariable Long jobId) throws NoSuchJobException {
		JobExecution jobExecution = jobRepository.getJobExecution(jobId);
		if (jobExecution == null) {
			throw new NoSuchJobException("Job not found");
		}

		Map<String, Object> res = new HashMap<>();
		res.put("status", jobExecution.getStatus().toString());
		res.put("startTime", jobExecution.getStartTime());
		res.put("endTime", jobExecution.getEndTime());
		return res;
	}

	@GetMapping("/download/{jobId}")
	public ResponseEntity<Resource> download(@PathVariable Long jobId) throws FileNotFoundException {
		try {
			BulkUploadJob bulkUploadJob = repo.findById(jobId).orElseThrow(() -> new RuntimeException("Job not found"));
			if (bulkUploadJob.getFilePath() == null) {
				throw new FileNotFoundException("No failed file available");
			}

			Path file = Paths.get(bulkUploadJob.getFilePath());
			if (!Files.exists(file)) {
				throw new FileNotFoundException("File not found on server");
			}

			Resource resource = new UrlResource(file.toUri());
			return ResponseEntity.ok()
					.header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + file.getFileName())
					.body(resource);
		} catch (Exception e) {
			log.error("Download failed", e);
			throw new FileNotFoundException("Download failed: " + e.getMessage());
		}
	}
	
}