package com.iam.service;

import com.iam.dao.UserDao;
import com.iam.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserService {
    @Autowired
    private AuthenticationManager authenticationManager;
    @Autowired
    private UserDao userDao;
    @Autowired
    private JwtService jwtService;
    public User saveUser(User user){
        BCryptPasswordEncoder bCryptPasswordEncoder = new BCryptPasswordEncoder(12);
        user.setPassword(bCryptPasswordEncoder.encode(user.getPassword()));
        userDao.save(user);
        return user;
    }
    public User updateUser(User user){
        userDao.save(user);
        return user;
    }
    public User getUserByUserName(String userName){
        return userDao.findByUserName(userName);
    }
    public User getUserById(Long id){
        return userDao.findById(id).orElse(null);
    }
}
