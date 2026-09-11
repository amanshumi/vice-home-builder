package com.homebuilder.schedule;

public class JobNotFoundException extends RuntimeException {

    public JobNotFoundException(long jobId) {
        super("No job with id " + jobId);
    }
}
