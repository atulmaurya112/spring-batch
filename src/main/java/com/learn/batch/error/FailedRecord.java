package com.learn.batch.error;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class FailedRecord implements Serializable {

    private static final long serialVersionUID = 1L;

    private Map<String, String> row;
    private List<String> errors;
    
}