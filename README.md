
# 인증시스템

### 개요: 유저 인증 후 로그인하고 권한에 맞는 요청에 응답

## 기술스택
* java11
* spring boot 2.7.6
* spring security
* jwt
* jpa

## 아키텍처
<img width="292" alt="인증인가" src="https://user-images.githubusercontent.com/85045177/209463761-35d58ec5-ff27-4704-bc1c-d34cae4f8361.png">

## 기능
* 회원가입/로그인: 중복되지 않는 정보로 가입하고 비밀번호는 암호화/ 유저를 인증하여 로그인하고 토큰 생성
* 인가: 토큰 정보를 통해 권한이 있는 유저에 한해서 응답
* 비밀번호 찾기: 이메일 인증을 통해 재설정된 비밀번호 제공

## 구현로직
1. 회원가입/로그인
``` java
public class UserService {
   ...
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
...
}
```

2. 토큰 생성
``` java
public class TokenProvider implements InitializingBean {
...
public String createToken(Authentication authentication) {
    String authorities = authentication.getAuthorities().stream()
            .map(GrantedAuthority::getAuthority)
            .collect(Collectors.joining(","));
    long now = (new Date()).getTime();
    Date validity = new Date(now + this.tokenValidityInMilliseconds);

    return Jwts.builder()
            .setSubject(authentication.getName())
            .claim(AUTHORITIES_KEY, authorities)//jwt body에 key, value 형식으로 넣는 것
            .signWith(key, SignatureAlgorithm.HS512)
            .setExpiration(validity)
            .compact();
	}
...
}
```

3. 이메일 인증으로 비밀번호 찾기
``` java
public class EmailAuthenticationService {
...
public MimeMessage createEmailForm(String email) throws MessagingException, UnsupportedEncodingException {
    User user = userRepository.findByEmail(email);

    createCode();
    String encodingPw = passwordEncoder.encode(authNum);
    user.updatePw(encodingPw);
    userRepository.save(user);
    MimeMessage message = emailSender.createMimeMessage();
    message.addRecipients(MimeMessage.RecipientType.TO, email); //보낼 이메일 설정
    message.setSubject("인증번호");
    message.setFrom("qkrdn2023@naver.com");
    message.setText((authNum), "utf-8", "html");

    return message;
}

//실제 메일 전송
@CacheEvict(value = "alluser", allEntries = true)
public String sendEmail(String toEmail) throws MessagingException, UnsupportedEncodingException {
    MimeMessage emailForm = createEmailForm(toEmail);
    emailSender.send(emailForm);
    return authNum; //인증 코드 반환
    }
}
```
