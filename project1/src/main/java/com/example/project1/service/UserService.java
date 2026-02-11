package com.example.project1.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import com.example.project1.module.User;
import com.example.project1.repositary.UserRepositary;

@Service
public class UserService {
    @Autowired
    private UserRepositary userRepositary;
     public User getLoggedInUser(){
        String email=SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepositary.findByEmail(email).orElseThrow(()->new RuntimeException("User not found"));
     }
}
