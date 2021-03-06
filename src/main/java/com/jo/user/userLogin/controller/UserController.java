package com.jo.user.userLogin.controller;

import com.jo.user.userImg.S3Service;
import com.jo.user.userLogin.dto.ResponseDTO;
import com.jo.user.userLogin.dto.UserDTO;
import com.jo.user.userLogin.model.User;
import com.jo.user.userLogin.security.TokenProvider;
import com.jo.user.userLogin.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;


@Slf4j
@RestController
@RequestMapping("/user")
public class UserController {

    @Autowired
    private UserService userService;
    @Autowired
    private TokenProvider tokenProvider;
    @Autowired
    private S3Service s3Service;

    private PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    //S3관련 수정한것 3개 에러시 지우기
    //회원가입
    @PostMapping("/signup")
    public ResponseEntity<?> registerUser(@RequestBody UserDTO userDTO) {
        //S3관련1 위의 MultipartFile file,throws IOException를 추가했다 에러가 뜨면 바로 지워버리면될듯함
        try {
            //요청을 이용해 저장할 사용자 만들기
            User user = User.builder()
                    .email(userDTO.getEmail())
                    .password(userDTO.getPassword())
                    .build();
            //서비스를 이용해 리포지터리에 사용자 저장
            User registeredUser = userService.create(user);

            UserDTO responseUserDTO = UserDTO.builder()
                    .id(registeredUser.getId())
                    .email(registeredUser.getEmail())
                    .password(registeredUser.getPassword())
                    .build();
            return ResponseEntity.ok().body(responseUserDTO);
        } catch (Exception e) {
            //사용자 정보는 항상 하나이므로 리스트로 만들어야 하는 ResponseDTO를 사용하지 않고 그냥 UserDTO 리턴
            ResponseDTO responseDTO = ResponseDTO.builder().error(e.getMessage()).build();
            return ResponseEntity.badRequest().body(responseDTO);

        }
    }

    //로그인
    @PostMapping("/login")
    public ResponseEntity<?> authenticate(@RequestBody UserDTO userDTO) {
        User user = userService.getbyCredentials(
                //로그인할때 쓰이는 것
                userDTO.getEmail(),
                userDTO.getPassword(),
                passwordEncoder); //패스워드 암호화라고함

        if (user != null) {
            //토큰 생성
            final String token = tokenProvider.create(user);
            final UserDTO responseUserDTO = UserDTO.builder()
                    .id(user.getId())
                    .token(token)
                    .email(user.getEmail())
                    .build();
            return ResponseEntity.ok().body(responseUserDTO);
        } else {
            ResponseDTO responseDTO = ResponseDTO.builder()
                    .error("Login failed").build();
            return ResponseEntity.badRequest().body(responseDTO);}
    }

    //회원 정보 수정
    @PutMapping
    public User useredit(@RequestBody UserDTO userDTO){
        return userService.update(userDTO);
    }
}


