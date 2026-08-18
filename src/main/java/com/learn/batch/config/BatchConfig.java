package com.learn.batch.config;

import java.util.Map;

import org.springframework.batch.core.job.Job;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.Step;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.infrastructure.item.ItemWriter;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

import com.learn.batch.listener.JobCompletionListener;
import com.learn.batch.processor.BulkProcessor;
import com.learn.batch.reader.ExcelItemReader;

import lombok.RequiredArgsConstructor;

@Configuration
@RequiredArgsConstructor
public class BatchConfig {

	private final JobRepository jobRepository;

	@Bean
	Step step(ObjectProvider<ExcelItemReader> reader, ObjectProvider<BulkProcessor<Object>> processor, ObjectProvider<ItemWriter<Object>> writer, PlatformTransactionManager txManager) {
		ExcelItemReader r = reader.getObject();
		BulkProcessor<Object> p = processor.getObject();
		ItemWriter<Object> w = writer.getObject();

		return new StepBuilder("bulk-step", jobRepository).<Map<String, String>, Object>chunk(2)
				.transactionManager(txManager)
				.faultTolerant()
				.skip(Exception.class)
				.skipLimit(10)
				.reader(r)
				.processor(p)
				.writer(w)
				.listener(p)
				.build();
	}

	@Bean
	Job job(Step step, JobCompletionListener listener) {
		return new JobBuilder("bulk-job", jobRepository)
				.start(step)
				.listener(listener)
				.build();
	}

}