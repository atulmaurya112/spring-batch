package com.learn.batch.writer;

import java.util.List;

import org.springframework.batch.infrastructure.item.Chunk;
import org.springframework.batch.infrastructure.item.ItemWriter;

import com.learn.batch.strategy.BulkUploadStrategy;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class BulkWriter<T> implements ItemWriter<T> {

	private final BulkUploadStrategy<T> strategy;

	@Override
	@SuppressWarnings("unchecked")
	public void write(Chunk<? extends T> chunk) {
		if (chunk == null || chunk.isEmpty()) {
			return;
		}

		List<T> items = (List<T>) chunk.getItems();
		strategy.saveAll(items);
	}
	
}