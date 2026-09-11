package com.homebuilder.schedule;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

/**
 * Applies the access rule: a builder reads any job, a customer reads only their own.
 *
 * <p>The job is loaded before the ownership check because ownership cannot be evaluated without
 * knowing who owns the job. A customer asking for a job that does not exist therefore gets 404,
 * not 403.
 */
@Service
public class JobScheduleService {

    private final JobScheduleRepository repository;

    public JobScheduleService(JobScheduleRepository repository) {
        this.repository = repository;
    }

    public JobSchedule getSchedule(long jobId, JobReader reader) {
        JobSchedule schedule = repository.findByJobId(jobId)
                .orElseThrow(() -> new JobNotFoundException(jobId));

        if (!reader.canRead(schedule.customerId())) {
            throw new AccessDeniedException("Job " + jobId + " belongs to another customer");
        }
        return schedule;
    }
}
