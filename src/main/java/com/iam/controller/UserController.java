package com.iam.controller;
import com.iam.dto.UserDTO;
import com.iam.model.User;
import com.iam.service.JwtService;
import com.iam.service.UserService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Objects;

@RestController
@RequestMapping("/users")
public class UserController {
    @Autowired
    private UserService userService;
    @Autowired
    private JwtService jwtService;
    @PostMapping("/register")
    public ResponseEntity<?> addUser(@RequestBody User user){
        if(user.getUserName() == null || user.getPassword() == null || user.getFirstName() == null || user.getLastName() == null){
            return ResponseEntity.badRequest().body(Map.of("message","Username ,password, firstName and lastName are mandatory"));
        }
        if(Objects.nonNull(userService.getUserByUserName(user.getUserName()))){
            return ResponseEntity.badRequest().body(Map.of("message","Username already exists. Please use different username"));
        }
        User saveUser = userService.saveUser(user);
        return ResponseEntity.ok(saveUser);
    }
    @PostMapping("/login")
    public ResponseEntity<?> login(Authentication authentication){
        if(authentication == null || !authentication.isAuthenticated()){
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message","Failed to authenticate user"));
        }
        String userName = authentication.getName();
        if(userName == null){
            return ResponseEntity.badRequest().body(Map.of("message","Username is mandatory"));
        }
        User user = userService.getUserByUserName(userName);
        String token = jwtService.generateToken(user);
        return ResponseEntity.ok(token);
    }
    @GetMapping("/profile")
    public ResponseEntity<?> getProfile(Authentication authentication){
        if(authentication == null || !authentication.isAuthenticated()){
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message","Failed to authenticate user"));
        }
        String userName = authentication.getName();
        if(!StringUtils.hasText(userName)){
            return ResponseEntity.badRequest().body(Map.of("message","Username cannot be empty"));
        }
        User user = userService.getUserByUserName(userName);
        if(Objects.isNull(user)){
            return ResponseEntity.badRequest().body(Map.of("message",String.format("User: %s not found",userName)));
        }
        UserDTO userDTO = new UserDTO();
        BeanUtils.copyProperties(user, userDTO);
        return ResponseEntity.ok().body(userDTO);
    }
    @PatchMapping("/update")
    public ResponseEntity<?> updateUser (@RequestBody UserDTO userDTO){
        BCryptPasswordEncoder bCryptPasswordEncoder = new BCryptPasswordEncoder(12);
        if(userDTO == null || userDTO.getUserName() == null){
            return ResponseEntity.badRequest().body(Map.of("message","UserName is not present in request"));
        }
        User user = userService.getUserByUserName(userDTO.getUserName());
        if(Objects.isNull(user)){
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("message","User not found"));
        }
        if(userDTO.getOldPassword()!= null && userDTO.getNewPassword() != null){
            if(!bCryptPasswordEncoder.matches(userDTO.getOldPassword(), user.getPassword())){
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("message","Please enter correct old password"));
            }
            user.setPassword(bCryptPasswordEncoder.encode(userDTO.getNewPassword()));
        }
        if(userDTO.getFirstName() != null){
            user.setFirstName(userDTO.getFirstName());
        }
        if(userDTO.getLastName() != null){
            user.setLastName(userDTO.getLastName());
        }
        userService.updateUser(user);
        return ResponseEntity.ok(Map.of("message","Profile updated successfully"));
    }
}
