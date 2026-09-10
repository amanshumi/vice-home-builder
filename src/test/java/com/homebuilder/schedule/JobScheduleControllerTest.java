package com.homebuilder.schedule;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDate;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(JobScheduleController.class)
@Import({SecurityConfig.class, JobScheduleService.class})
class JobScheduleControllerTest {

    private static final SimpleGrantedAuthority BUILDER = new SimpleGrantedAuthority("ROLE_builder");
    private static final SimpleGrantedAuthority CUSTOMER = new SimpleGrantedAuthority("ROLE_customer");

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private JobScheduleRepository repository;

    /** Required by the resource server auto-configuration; tokens are stubbed, never decoded. */
    @MockitoBean
    private JwtDecoder jwtDecoder;

    @Test
    void missingTokenIsUnauthorized() throws Exception {
        mockMvc.perform(get("/jobs/1/schedule"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void builderReadsAnyJobAndGetsTheExactContract() throws Exception {
        given(repository.findByJobId(1L)).willReturn(Optional.of(
                new JobSchedule(1L, "cust-100", "active", LocalDate.of(2026, 10, 6))));

        mockMvc.perform(get("/jobs/1/schedule")
                        .with(jwt().authorities(BUILDER)))
                .andExpect(status().isOk())
                .andExpect(content().json(
                        "{\"jobId\":1,\"customerId\":\"cust-100\",\"status\":\"active\","
                                + "\"scheduledStart\":\"2026-10-06\"}", true));
    }

    @Test
    void customerReadsTheirOwnJob() throws Exception {
        given(repository.findByJobId(1L)).willReturn(Optional.of(
                new JobSchedule(1L, "cust-100", "active", LocalDate.of(2026, 10, 6))));

        mockMvc.perform(get("/jobs/1/schedule")
                        .with(jwt()
                                .authorities(CUSTOMER)
                                .jwt(token -> token.claim("customer_id", "cust-100"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.customerId").value("cust-100"));
    }

    @Test
    void customerReadingAnotherCustomersJobIsForbidden() throws Exception {
        given(repository.findByJobId(1L)).willReturn(Optional.of(
                new JobSchedule(1L, "cust-100", "active", LocalDate.of(2026, 10, 6))));

        mockMvc.perform(get("/jobs/1/schedule")
                        .with(jwt()
                                .authorities(CUSTOMER)
                                .jwt(token -> token.claim("customer_id", "cust-200"))))
                .andExpect(status().isForbidden());
    }

    @Test
    void unknownJobIsNotFound() throws Exception {
        given(repository.findByJobId(anyLong())).willReturn(Optional.empty());

        mockMvc.perform(get("/jobs/999/schedule")
                        .with(jwt().authorities(BUILDER)))
                .andExpect(status().isNotFound());
    }

    @Test
    void onHoldJobKeepsTheKeyAndNullsTheDate() throws Exception {
        given(repository.findByJobId(2L)).willReturn(Optional.of(
                new JobSchedule(2L, "cust-100", "on_hold", null)));

        mockMvc.perform(get("/jobs/2/schedule")
                        .with(jwt().authorities(BUILDER)))
                .andExpect(status().isOk())
                .andExpect(content().json(
                        "{\"jobId\":2,\"customerId\":\"cust-100\",\"status\":\"on_hold\","
                                + "\"scheduledStart\":null}", true));
    }

    @Test
    void aTokenWithNoRoleAndNoCustomerIdCannotRead() throws Exception {
        given(repository.findByJobId(1L)).willReturn(Optional.of(
                new JobSchedule(1L, "cust-100", "active", LocalDate.of(2026, 10, 6))));

        mockMvc.perform(get("/jobs/1/schedule").with(jwt()))
                .andExpect(status().isForbidden());
    }
}
