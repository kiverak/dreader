package ru.dreader.dreaderusers.security;

import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.oidc.web.logout.OidcClientInitiatedLogoutSuccessHandler;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import ru.dreader.dreaderusers.auth.KeycloakLogoutHandler;

import java.util.List;
import java.util.stream.Stream;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final KeycloakLogoutHandler keycloakLogoutHandler;
    private final ClientRegistrationRepository clientRegistrationRepository;

    // API
    @Bean
    @Order(1)
    public SecurityFilterChain apiChain(HttpSecurity http) throws Exception {
        http
                .securityMatcher("/api/**")
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/auth/register", "/api/test").permitAll()
                        .requestMatchers("/api/admin/**").hasRole("ADMIN")
                        .requestMatchers("/api/user/**").hasRole("USER")
                        .requestMatchers("/api/internal/**").hasRole("INTERNAL_SERVICE")
                        .anyRequest().authenticated()
                )
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint((req, res, e) -> {
                            String path = req.getRequestURI();
                            if (path.startsWith("/api/auth/register") || path.startsWith("/api/test")) {
                                res.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                                return;
                            }
                            res.sendError(HttpServletResponse.SC_UNAUTHORIZED, e.getMessage());
                        })
                )
                .oauth2ResourceServer(oauth2 -> oauth2
                        .jwt(jwt -> jwt.jwtAuthenticationConverter(jwtAuthenticationConverter()))
                );

        return http.build();
    }

    // Browser
    @Bean
    @Order(2)
    public SecurityFilterChain browserChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.GET, "/logout").permitAll()
                        .requestMatchers("/oauth2/**").permitAll()
                        .anyRequest().authenticated()
                )
                .oauth2Login(oauth -> oauth
                        .loginPage("/oauth2/authorization/dreader-users")
                )
                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .addLogoutHandler(keycloakLogoutHandler)
                        .logoutSuccessHandler(oidcLogoutSuccessHandler())
                );

        return http.build();
    }

    @Bean
    public LogoutSuccessHandler oidcLogoutSuccessHandler() {
        var handler = new OidcClientInitiatedLogoutSuccessHandler(clientRegistrationRepository);
        handler.setPostLogoutRedirectUri("{baseUrl}/");
        return handler;
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

            var roleAuthorities = roles.stream()
                    .filter(r -> r.startsWith("ROLE_"))
                    .map(SimpleGrantedAuthority::new)
                    .toList();

            return Stream.concat(authorities.stream(), roleAuthorities.stream()).toList();
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
