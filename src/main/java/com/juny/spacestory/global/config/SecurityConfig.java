package com.juny.spacestory.global.config;

import com.juny.spacestory.global.security.jwt.refresh.RefreshRepository;
import com.juny.spacestory.global.security.oauth2.CustomAuthenticationFailureHandler;
import com.juny.spacestory.global.security.oauth2.CustomOAuth2UserService;
import com.juny.spacestory.global.security.oauth2.CustomSuccessHandler;
import java.util.Arrays;
import java.util.Collections;
import com.juny.spacestory.global.security.filter.JwtFilter;
import com.juny.spacestory.global.security.filter.LoginFilter;
import com.juny.spacestory.global.security.jwt.JwtUtil;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.csrf.CsrfFilter;
import org.springframework.security.web.csrf.CsrfTokenRequestAttributeHandler;
import org.springframework.security.web.header.HeaderWriterFilter;
import org.springframework.web.cors.CorsConfiguration;

@Configuration
@EnableWebSecurity(debug = true)
@RequiredArgsConstructor
public class SecurityConfig {

  private final AuthenticationConfiguration authenticationConfiguration;
  private final RefreshRepository refreshRepository;
  private final CustomOAuth2UserService customOAuth2UserService;
  private final CustomSuccessHandler customSuccessHandler;
  private final CustomAuthenticationFailureHandler authenticationFailureHandler;

  @Bean
  public AuthenticationManager authenticationManager(AuthenticationConfiguration configuration)
    throws Exception {

    return configuration.getAuthenticationManager();
  }

  @Bean
  public BCryptPasswordEncoder bCryptPasswordEncoder() {

    return new BCryptPasswordEncoder();
  }

  @Bean
  public SecurityFilterChain filterChain(HttpSecurity http, JwtUtil jwtUtil) throws Exception {

    http.cors(
      (cors) ->
        cors.configurationSource(
          request -> {
            CorsConfiguration config = new CorsConfiguration();

            config.setAllowedHeaders(Collections.singletonList("*"));
            config.setAllowedOrigins(
              List.of("http://localhost:5173", "https://spacestory.duckdns.org"));
            config.setAllowedMethods(Collections.singletonList("*"));

            config.setAllowCredentials(true);
            config.setExposedHeaders(Arrays.asList("Set-Cookie", "refreshToken", "XSRF-TOKEN"));
            config.setMaxAge(3600L);

            return config;
          }));

    http.authorizeHttpRequests(
      (auth) ->
        auth.requestMatchers(
            "/login",
            "/api/v1/auth/login",
            "/api/v1/auth/logout",
            "/api/v1/auth/register",
            "/swagger-ui/**",
            "/swagger-resources/**",
            "/v3/api-docs/**",
            "/api/v1/auth/tokens",
            "/api/v1/auth/tokens-by-cookie")
          .permitAll()
          .requestMatchers("/admin/**")
          .hasAuthority("ADMIN")
          .anyRequest()
          .authenticated());

    http
      .csrf(AbstractHttpConfigurer::disable)
      .formLogin(AbstractHttpConfigurer::disable)
      .httpBasic(AbstractHttpConfigurer::disable);

    http.oauth2Login(
      (oauth2) ->
        oauth2
          .userInfoEndpoint(
            (userInfoEndpointConfig) ->
              userInfoEndpointConfig.userService(customOAuth2UserService))
          .successHandler(customSuccessHandler)
          .failureHandler(authenticationFailureHandler));

    http.addFilterBefore(new JwtFilter(jwtUtil), LoginFilter.class);

    http.addFilterAt(
      new LoginFilter(
        authenticationManager(authenticationConfiguration), jwtUtil, refreshRepository),
      UsernamePasswordAuthenticationFilter.class);

    http.sessionManagement(
      (session) -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS));

    return http.build();
  }
}
