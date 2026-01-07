package ru.dreader.dreaderusers.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.web.SecurityFilterChain;

import java.util.List;
import java.util.stream.Stream;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

        final String[] PERMIT_ALL_ACCESS_ENDPOINTS = {"/api/auth/**", "/api/error", "/api/test"};
        final String[] USER_ACCESS_ENDPOINTS = {"/api/user/**", "/api/category/*"};
        final String[] ADMIN_ACCESS_ENDPOINTS = {"/api/admin/**"};

        http.oauth2ResourceServer(oauth2 -> oauth2
                .jwt(jwt -> jwt.jwtAuthenticationConverter(jwtAuthenticationConverter())) );
        http.oauth2Login(Customizer.withDefaults());

        return http
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(request -> request
                        .requestMatchers(PERMIT_ALL_ACCESS_ENDPOINTS).permitAll()
                        .requestMatchers(ADMIN_ACCESS_ENDPOINTS).hasRole("ADMIN")
                        .requestMatchers(USER_ACCESS_ENDPOINTS).hasRole("USER")
                        .requestMatchers("/api/internal/**").hasRole("INTERNAL_SERVICE")
                        .anyRequest().authenticated())
                .build();
    }

    // преобразование "сырого" JWT-токена (полученного от Keycloak) в объект аутентификации Spring Security
    @Bean
    public JwtAuthenticationConverter jwtAuthenticationConverter() {
        var converter = new JwtAuthenticationConverter();
        var jwtGrantedAuthoritiesConverter = new JwtGrantedAuthoritiesConverter();
        converter.setPrincipalClaimName("preferred_username");
        converter.setJwtGrantedAuthoritiesConverter(jwt -> {
            var authorities = jwtGrantedAuthoritiesConverter.convert(jwt);
            var roles = (List<String>) jwt.getClaimAsMap("realm_access").get("roles");
            if (roles == null) roles = List.of();

            return Stream.concat(authorities.stream(),
                            roles.stream()
                                    .filter(role -> role.startsWith("ROLE_"))
                                    .map(SimpleGrantedAuthority::new)
                                    .map(GrantedAuthority.class::cast))
                    .toList();
        });

        return converter;
    }

    // настраивает кастомную загрузку пользователя для сценария OAuth2 Login (когда приложение само перенаправляет пользователя на страницу входа Keycloak)
//    @Bean
//    public OAuth2UserService<OidcUserRequest, OidcUser> oAuth2UserService() {
//        var oidcUserService = new OidcUserService();
//        return userRequest -> {
//            var oidcUser = oidcUserService.loadUser(userRequest);
//            var roles = (List<String>) oidcUser.getClaimAsMap("realm_access").get("roles");
//            var authorities = Stream.concat(oidcUser.getAuthorities().stream(),
//                            roles.stream()
//                                    .filter(role -> role.startsWith("ROLE_"))
//                                    .map(SimpleGrantedAuthority::new)
//                                    .map(GrantedAuthority.class::cast))
//                    .toList();
//
//            return new DefaultOidcUser(authorities, oidcUser.getIdToken(), oidcUser.getUserInfo());
//        };
//    }
}
