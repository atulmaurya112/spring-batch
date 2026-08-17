package com.learn.batch.strategy;

import java.util.List;
import java.util.Map;

public interface BulkUploadStrategy<T> {

	String getType();

	T mapRow(Map<String, String> row);

	List<String> validate(Map<String, String> row);

	void beforeStep();

	void beforeSave(T entity);

	void saveAll(List<T> list);

}