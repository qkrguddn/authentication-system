package com.sg.authentication.controller;

import com.sg.authentication.dto.UserDto;
import com.sg.authentication.entity.User;
import com.sg.authentication.repository.UserRepository;
import com.sg.authentication.service.CustomUserDetailsService;
import com.sg.authentication.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import javax.validation.Valid;
import java.security.Principal;
import java.util.List;

@RequiredArgsConstructor
@RestController
public class UserController {
    private final UserService userService;

    @PostMapping("/signup")
    public ResponseEntity<UserDto> signup(@Valid @RequestBody UserDto userDto) {
        return ResponseEntity.ok(userService.signup(userDto));
    }

    @GetMapping("/user")
    @PreAuthorize("hasAnyRole('USER','ADMIN')")
    public UserDto getMyUserInfo(Principal principal){
        return userService.getMyUserWithAuthorities(principal);
       }

    @GetMapping("/user/{username}")
    @PreAuthorize("hasAnyRole('ADMIN')")
    public UserDto getUserInfo(@PathVariable String username) {
        return userService.getUserWithAuthorities(username);
    }

    @GetMapping("admin/alluser")
    @PreAuthorize("hasAnyRole('ADMIN')")
    public List<User> getAllUser(Principal principal){
        System.out.println(principal.getName());
        return userService.getAllUser();
    }

}