package ru.dreader.dreadergateway.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;

@Configuration
@EnableWebFluxSecurity
public class SecurityConfig {

    @Bean
//    public SecurityWebFilterChain springSecurityFilterChain(ServerHttpSecurity http, ReactiveJwtAuthenticationConverter jwtAuthenticationConverter) {
    public SecurityWebFilterChain springSecurityFilterChain(ServerHttpSecurity http) {

        return http
                .csrf(ServerHttpSecurity.CsrfSpec::disable)
                .authorizeExchange(exchanges -> exchanges
                        .pathMatchers("/login", "/error", "/actuator/**", "/users/api/test", "/users/api/auth/**").permitAll()
                        .anyExchange().authenticated()
                )
                .oauth2Login(Customizer.withDefaults())
                .oauth2Client(Customizer.withDefaults())
//                .oauth2ResourceServer(oauth2 -> oauth2.jwt(jwt -> jwt.jwtAuthenticationConverter(jwtAuthenticationConverter)))
                .build();
    }

    // for jwt token for test TODO remove after making front
//    @Bean
//    public ReactiveJwtAuthenticationConverter jwtAuthenticationConverter() {
//        var converter = new ReactiveJwtAuthenticationConverter();
//        var jwtGrantedAuthoritiesConverter = new JwtGrantedAuthoritiesConverter();
//        converter.setPrincipalClaimName("preferred_username");
//        converter.setJwtGrantedAuthoritiesConverter(jwt -> {
//            var authorities = jwtGrantedAuthoritiesConverter.convert(jwt);
//            var roles = (List<String>) jwt.getClaimAsMap("realm_access").get("roles");
//            if (roles == null) roles = List.of();
//
//            return Flux.fromIterable(authorities)
//                    .concatWith(Flux.fromIterable(roles)
//                            .filter(role -> role.startsWith("ROLE_"))
//                            .map(SimpleGrantedAuthority::new)
//                            .map(GrantedAuthority.class::cast));
//        });
//
//        return converter;
//    }
}
