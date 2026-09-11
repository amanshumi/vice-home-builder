package com.homebuilder.schedule;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;

class KeycloakRealmRoleConverterTest {

    private final KeycloakRealmRoleConverter converter = new KeycloakRealmRoleConverter();

    @Test
    void mapsRealmRolesToRoleAuthorities() {
        Jwt jwt = jwtWithClaims(Map.of("realm_access", Map.of("roles", List.of("builder", "offline_access"))));

        assertThat(converter.convert(jwt))
                .extracting(GrantedAuthority::getAuthority)
                .containsExactlyInAnyOrder("ROLE_builder", "ROLE_offline_access");
    }

    @Test
    void aTokenWithoutRealmAccessHasNoAuthorities() {
        assertThat(converter.convert(jwtWithClaims(Map.of("customer_id", "cust-100")))).isEmpty();
    }

    private static Jwt jwtWithClaims(Map<String, Object> claims) {
        Jwt.Builder builder = Jwt.withTokenValue("token").header("alg", "RS256");
        claims.forEach(builder::claim);
        return builder.build();
    }
}
