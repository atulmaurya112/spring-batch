package com.learn.batch.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.learn.batch.model.BulkUploadJob;

public interface BulkUploadJobRepository extends JpaRepository<BulkUploadJob, Long> {

}
