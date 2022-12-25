package com.sg.authentication.controller;


import com.sg.authentication.dto.EmailAuthRequestDto;
import com.sg.authentication.dto.LoginDto;
import com.sg.authentication.dto.TokenDto;
import com.sg.authentication.jwt.JwtFilter;
import com.sg.authentication.jwt.TokenProvider;
import com.sg.authentication.service.EmailAuthenticationService;
import com.sg.authentication.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import javax.mail.MessagingException;
import javax.validation.Valid;
import java.io.UnsupportedEncodingException;

@RequiredArgsConstructor
@RestController
public class AuthController {

    private final EmailAuthenticationService emailAuthenticationService;
private final UserService userService;
    @PostMapping("/mailAuthenticate")
    public String mailConfirm(@RequestBody EmailAuthRequestDto emailDto) throws MessagingException, UnsupportedEncodingException {

        String authCode = emailAuthenticationService.sendEmail(emailDto.getEmail());
        return authCode;
    }

    @PostMapping("/authenticate")
    public ResponseEntity<TokenDto> authorize(@Valid @RequestBody LoginDto loginDto) {
        String jwt = userService.login(loginDto);
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.add(JwtFilter.AUTHORIZATION_HEADER, "Bearer " + jwt);

        return new ResponseEntity<>(new TokenDto(jwt), httpHeaders, HttpStatus.OK);
    }


}