package com.learn.batch.listener;

import java.io.ByteArrayOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.job.JobExecution;
import org.springframework.batch.core.listener.JobExecutionListener;
import org.springframework.batch.core.step.StepExecution;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import com.learn.batch.error.ExcelErrorWriter;
import com.learn.batch.error.FailedRecord;
import com.learn.batch.model.BulkUploadJob;
import com.learn.batch.repository.BulkUploadJobRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class JobCompletionListener implements JobExecutionListener {

	private static final String SUCCESS = "SUCCESS";
	private static final String FAILED = "FAILED";
	
	private final @Lazy BulkUploadJobRepository repo;

	@Override
	public void afterJob(JobExecution jobExecution) {
		Long jobId = jobExecution.getId();
		String type = jobExecution.getJobParameters().getString("type");

		try {
			if (jobExecution.getStatus() != BatchStatus.COMPLETED) {
				repo.save(new BulkUploadJob(jobId, type, null, FAILED));
				return;
			}

			List<FailedRecord> failed = collectFailed(jobExecution);
			if (failed.isEmpty()) {
				repo.save(new BulkUploadJob(jobId, type, null, SUCCESS));
				return;
			}

			ByteArrayOutputStream out = ExcelErrorWriter.generate(failed);
			Path dir = Paths.get("bulk-errors");
			Files.createDirectories(dir);
			String fileName = "failed-" + jobId + ".xlsx";
			Path path = dir.resolve(fileName);
			Files.write(path, out.toByteArray());
			
			repo.save(new BulkUploadJob(jobId, type, path.toString(), FAILED));
		} catch (Exception e) {
			log.error("Error in job completion", e);
			repo.save(new BulkUploadJob(jobId, type, null, FAILED));
			throw new RuntimeException(e);
		}
	}

	@SuppressWarnings("unchecked")
	private List<FailedRecord> collectFailed(JobExecution jobExecution) {
		List<FailedRecord> failed = new ArrayList<>();
		for (StepExecution step : jobExecution.getStepExecutions()) {
			List<FailedRecord> stepFailed = (List<FailedRecord>) step.getExecutionContext().get(FAILED);
			if (stepFailed != null) {
				failed.addAll(stepFailed);
			}
		}
		return failed;
	}
	
}