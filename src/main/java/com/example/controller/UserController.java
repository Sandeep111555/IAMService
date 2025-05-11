package com.example.controller;
import com.example.dao.UserDao;
import com.example.dto.UserDTO;
import com.example.model.User;
import com.example.service.JwtService;
import com.example.service.UserService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import javax.ws.rs.QueryParam;
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
            return ResponseEntity.badRequest().body("Username ,password, firstName and lastName are mandatory");
        }
        if(Objects.nonNull(userService.getUserByUserName(user.getUserName()))){
            return ResponseEntity.badRequest().body("Username already exists. Please use different username");
        }
        User saveUser = userService.saveUser(user);
        return ResponseEntity.ok(saveUser);
    }
    @PostMapping("/login")
    public ResponseEntity<?> login(Authentication authentication){
        if(authentication == null || !authentication.isAuthenticated()){
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Failed to authenticate user");
        }
        String userName = authentication.getName();
        if(userName == null){
            return ResponseEntity.badRequest().body("Username is mandatory");
        }
        User user = userService.getUserByUserName(userName);
        String token = jwtService.generateToken(user);
        return ResponseEntity.ok(token);
    }
    @GetMapping("/profile")
    public ResponseEntity<?> getProfile(Authentication authentication){
        if(authentication == null || !authentication.isAuthenticated()){
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Failed to authenticate user");
        }
        String userName = authentication.getName();
        if(!StringUtils.hasText(userName)){
            return ResponseEntity.badRequest().body("Username cannot be empty");
        }
        User user = userService.getUserByUserName(userName);
        if(Objects.isNull(user)){
            return ResponseEntity.badRequest().body(String.format("User: %s not found",userName));
        }
        UserDTO userDTO = new UserDTO();
        BeanUtils.copyProperties(user, userDTO);
        return ResponseEntity.ok().body(userDTO);
    }
}
