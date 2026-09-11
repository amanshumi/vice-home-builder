package com.homebuilder.schedule;

import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class JobScheduleController {

    private final JobScheduleService service;

    public JobScheduleController(JobScheduleService service) {
        this.service = service;
    }

    @GetMapping(path = "/jobs/{id}/schedule", produces = MediaType.APPLICATION_JSON_VALUE)
    public JobSchedule schedule(@PathVariable long id, Authentication authentication) {
        return service.getSchedule(id, JobReader.from(authentication));
    }
}
