package com.sg.authentication.service;

import java.security.Principal;
import java.util.Collections;
import java.util.List;
import com.sg.authentication.dto.UserDto;
import com.sg.authentication.entity.Authority;
import com.sg.authentication.entity.User;
import com.sg.authentication.exception.DuplicateMemberException;
import com.sg.authentication.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

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