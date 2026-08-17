package com.learn.batch.config;

import java.util.ArrayList;
import java.util.List;

import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.infrastructure.item.ItemWriter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.learn.batch.error.FailedRecord;
import com.learn.batch.processor.BulkProcessor;
import com.learn.batch.reader.ExcelItemReader;
import com.learn.batch.strategy.BulkUploadStrategy;
import com.learn.batch.strategy.impl.BulkStrategyFactory;
import com.learn.batch.writer.BulkWriter;

import lombok.RequiredArgsConstructor;

@Configuration
@RequiredArgsConstructor
public class BulkJobConfig {

	private final BulkStrategyFactory factory;

	@Bean
	@StepScope
	ExcelItemReader reader(@Value("#{jobParameters['file']}") String filePath) {
		return new ExcelItemReader(filePath);
	}

	@Bean
	@StepScope
	@SuppressWarnings("unchecked")
	BulkProcessor<Object> processor(@Value("#{jobParameters['type']}") String type, List<FailedRecord> failedRecords) {
		BulkUploadStrategy<Object> strategy = (BulkUploadStrategy<Object>) factory.get(type);
		return new BulkProcessor<>(strategy, failedRecords);
	}

	@Bean
	@StepScope
	@SuppressWarnings("unchecked")
	ItemWriter<Object> writer(@Value("#{jobParameters['type']}") String type) {
		BulkUploadStrategy<Object> strategy = (BulkUploadStrategy<Object>) factory.get(type);
		return new BulkWriter<>(strategy);
	}

	@Bean
	@StepScope
	List<FailedRecord> failedRecords() {
		return new ArrayList<>();
	}

}