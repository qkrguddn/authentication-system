package com.sg.authentication.service;

import java.security.Principal;
import java.util.Collections;
import java.util.List;

import com.sg.authentication.dto.LoginDto;
import com.sg.authentication.dto.TokenDto;
import com.sg.authentication.dto.UserDto;
import com.sg.authentication.entity.Authority;
import com.sg.authentication.entity.User;
import com.sg.authentication.exception.DuplicateMemberException;
import com.sg.authentication.jwt.JwtFilter;
import com.sg.authentication.jwt.TokenProvider;
import com.sg.authentication.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestBody;

import javax.validation.Valid;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final TokenProvider tokenProvider;
    private final AuthenticationManagerBuilder authenticationManagerBuilder;

    @Transactional
    public UserDto signup(UserDto userDto) {
        if (!userRepository.findOneAuthoritiesByUsername(userDto.getUsername()).isEmpty()) {
            throw new DuplicateMemberException("이미 가입되어 있는 유저입니다.");
        }

        Authority authority = Authority.builder()
                .authorityName("ROLE_USER")
                .build();
        User user = User.builder()
                .username(userDto.getUsername())
                .email(userDto.getUserEmail())
                .password(passwordEncoder.encode(userDto.getPassword()))
                .nickname(userDto.getNickname())
                .authorities(Collections.singleton(authority))
                .activated(true)
                .build();
        return UserDto.from(userRepository.save(user));
    }
    @Transactional
    public String login(LoginDto loginDto){
        UsernamePasswordAuthenticationToken authenticationToken =
                new UsernamePasswordAuthenticationToken(loginDto.getUsername(), loginDto.getPassword());
        Authentication authentication = authenticationManagerBuilder.getObject().authenticate(authenticationToken);
        SecurityContextHolder.getContext().setAuthentication(authentication);
        String jwt = tokenProvider.createToken(authentication);
        return jwt;
    }

    @Cacheable("user")
    @Transactional(readOnly = true)
    public UserDto getUserWithAuthorities(String username) {
   return UserDto.from(userRepository.findOneAuthoritiesByUsername(username).get());

    }
    @Cacheable(value = "alluser")
    @Transactional(readOnly = true)
    public List<User> getAllUser(){
       return userRepository.findAll();
    }
    @Cacheable("user")
    @Transactional(readOnly = true)
    public UserDto getMyUserWithAuthorities(Principal principal) {
        String userName = principal.getName();
        return UserDto.from(userRepository.findOneAuthoritiesByUsername(userName).get());
    }
}