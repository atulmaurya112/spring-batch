package com.learn.batch.strategy.impl;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Component;

import com.learn.batch.strategy.BulkUploadStrategy;

@Component
public class BulkStrategyFactory {

	private final Map<String, BulkUploadStrategy<?>> map = new ConcurrentHashMap<>();

	public BulkStrategyFactory(List<BulkUploadStrategy<?>> strategies) {
		strategies.forEach(s -> map.put(s.getType().toUpperCase(), s));
	}
	
	public BulkUploadStrategy<?> get(String type) {
		if (type == null || type.isBlank()) {
			throw new IllegalArgumentException("Type cannot be null/blank");
		}

		String key = type.trim().toUpperCase();
		BulkUploadStrategy<?> strategy = map.get(key);
		if (strategy == null) {
			throw new IllegalArgumentException("Invalid bulk type: " + type);
		}

		return strategy;
	}
	
}