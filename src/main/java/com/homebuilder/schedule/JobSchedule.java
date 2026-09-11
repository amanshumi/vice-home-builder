package com.homebuilder.schedule;

import java.time.LocalDate;

/**
 * The response contract for GET /jobs/{id}/schedule.
 *
 * <p>{@code scheduledStart} is always serialised, and is {@code null} when the job is on hold:
 * the schedule is unknown, not zero. Do not add {@code @JsonInclude(NON_NULL)} here.
 */
public record JobSchedule(
        long jobId,
        String customerId,
        String status,
        LocalDate scheduledStart) {
}
