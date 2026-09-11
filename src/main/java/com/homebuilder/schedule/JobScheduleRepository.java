package com.homebuilder.schedule;

import java.sql.Date;
import java.util.Optional;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

/**
 * Reads the schedule through the database function {@code job_schedule(bigint)}, which owns the
 * "an on hold job has no known start date" rule. The function is the single place that decides
 * whether the stored {@code scheduled_start} is authoritative, so the rule is not duplicated here.
 */
@Repository
public class JobScheduleRepository {

    private static final String SELECT_SCHEDULE =
            "SELECT job_id, customer_id, status, scheduled_start FROM job_schedule(?)";

    private static final RowMapper<JobSchedule> MAPPER = (rs, rowNum) -> {
        Date scheduledStart = rs.getDate("scheduled_start");
        return new JobSchedule(
                rs.getLong("job_id"),
                rs.getString("customer_id"),
                rs.getString("status"),
                scheduledStart == null ? null : scheduledStart.toLocalDate());
    };

    private final JdbcTemplate jdbcTemplate;

    public JobScheduleRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public Optional<JobSchedule> findByJobId(long jobId) {
        return jdbcTemplate.query(SELECT_SCHEDULE, MAPPER, jobId).stream().findFirst();
    }
}
