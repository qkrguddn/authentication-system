package com.sg.authentication.config;

import com.sg.authentication.jwt.JwtSecurityConfig;
import com.sg.authentication.jwt.TokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.filter.CorsFilter;

@RequiredArgsConstructor
@EnableWebSecurity
//controller에서 @PreAuthorize 메소드 단위로 사용하기 위해
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class SecurityConfig {
    private final TokenProvider tokenProvider;
    private final CorsFilter corsFilter;
    private final CorsConfig corsConfig;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity httpSecurity) throws Exception {
        httpSecurity
                .addFilter(corsConfig.corsFilter())
                .csrf().disable()
                .authorizeRequests()
                .antMatchers("/admin/**").hasRole("ADMIN")
                .anyRequest().permitAll()
                .and()
                .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                .and()
                .addFilterBefore(corsFilter, UsernamePasswordAuthenticationFilter.class)
                .apply(new JwtSecurityConfig(tokenProvider));

        return httpSecurity.build();
    }
}