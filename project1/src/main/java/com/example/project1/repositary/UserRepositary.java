package com.example.project1.repositary;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.project1.module.User;

@Repository
public interface UserRepositary extends JpaRepository<User,Long> {

     boolean existsByEmail(String email);
     Optional<User> findByEmail(String email);
} 
