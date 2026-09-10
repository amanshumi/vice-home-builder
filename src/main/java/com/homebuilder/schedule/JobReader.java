package com.homebuilder.schedule;

import java.util.Collection;
import java.util.Objects;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

/**
 * Who is asking, reduced to the only two things the access rule needs: are they a builder, and
 * which customer are they.
 */
public record JobReader(boolean builder, String customerId) {

    static final String CUSTOMER_ID_CLAIM = "customer_id";
    private static final String BUILDER_AUTHORITY = "ROLE_builder";

    public static JobReader from(Authentication authentication) {
        Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();
        boolean builder = authorities.stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch(BUILDER_AUTHORITY::equals);

        String customerId = null;
        if (authentication instanceof JwtAuthenticationToken jwtAuthentication) {
            Jwt jwt = jwtAuthentication.getToken();
            customerId = jwt.getClaimAsString(CUSTOMER_ID_CLAIM);
        }
        return new JobReader(builder, customerId);
    }

    /** A builder reads any job. Anyone else reads only jobs owned by their own customer id. */
    public boolean canRead(String jobCustomerId) {
        return builder || Objects.equals(customerId, jobCustomerId);
    }
}
