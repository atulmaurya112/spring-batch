package com.learn.batch.processor;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.listener.StepExecutionListener;
import org.springframework.batch.core.step.StepExecution;
import org.springframework.batch.infrastructure.item.ExecutionContext;
import org.springframework.batch.infrastructure.item.ItemProcessor;

import com.learn.batch.error.FailedRecord;
import com.learn.batch.strategy.BulkUploadStrategy;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class BulkProcessor<T> implements ItemProcessor<Map<String, String>, T>, StepExecutionListener {

	private final BulkUploadStrategy<T> strategy;
	private final List<FailedRecord> failedRecords;

	public BulkProcessor(BulkUploadStrategy<T> strategy, List<FailedRecord> failedRecords) {
		this.strategy = strategy;
		this.failedRecords = failedRecords;
	}

	@Override
	public void beforeStep(StepExecution stepExecution) {
		strategy.beforeStep();
	}

	@Override
	public T process(Map<String, String> row) {
		if (row == null || row.isEmpty()) {
			return null;
		}

		List<String> errors = strategy.validate(row);
		if (errors != null && !errors.isEmpty()) {
			failedRecords.add(new FailedRecord(row, errors));
			return null;
		}

		try {
			T entity = strategy.mapRow(row);
			if (entity == null) {
				failedRecords.add(new FailedRecord(row, List.of("Mapping failed")));
				return null;
			}

			strategy.beforeSave(entity);
			return entity;
		} catch (Exception e) {
			failedRecords.add(new FailedRecord(row, List.of(e.getMessage() != null ? e.getMessage() : "Processing failed")));
			return null;
		}
	}

	@Override
	public ExitStatus afterStep(StepExecution stepExecution) {

	    log.info("Step completed. status={}, readCount={}, writeCount={}, skipCount={}, " +
	             "readSkipCount={}, processSkipCount={}, writeSkipCount={}",
	            stepExecution.getStatus(),
	            stepExecution.getReadCount(),
	            stepExecution.getWriteCount(),
	            stepExecution.getSkipCount(),
	            stepExecution.getReadSkipCount(),
	            stepExecution.getProcessSkipCount(),
	            stepExecution.getWriteSkipCount());

	    ExecutionContext context = stepExecution.getExecutionContext();
	    context.put("FAILED", new ArrayList<>(failedRecords));

	    return stepExecution.getExitStatus();
	}
	
}