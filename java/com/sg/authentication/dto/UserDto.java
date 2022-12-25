package com.sg.authentication.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.sg.authentication.entity.User;
import lombok.*;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.Set;
import java.util.stream.Collectors;


@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserDto {

    @NotNull
    private String username;

    @NotNull
    private String userEmail;

    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    @NotNull
    @Size(min = 3, max = 100)
    private String password;

    @NotNull
    private String nickname;

    private Set<AuthorityDto> authorityDtoSet;


    public static UserDto from(User user) {
        if(user == null) return null;

        return UserDto.builder()
                .username(user.getUsername())
                .userEmail(user.getEmail())
                .nickname(user.getNickname())
                .authorityDtoSet(user.getAuthorities().stream()
                        .map(authority -> AuthorityDto.builder().authorityName(authority.getAuthorityName()).build())
                        .collect(Collectors.toSet()))
                .build();
    }
}