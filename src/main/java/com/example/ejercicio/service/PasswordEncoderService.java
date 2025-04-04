package com.example.ejercicio.service;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class PasswordEncoderService {
        private final PasswordEncoder passwordEncoder;


        public PasswordEncoderService() {
            this.passwordEncoder = new BCryptPasswordEncoder();
        }

        public String encodePassword(String password) {
            return passwordEncoder.encode(password);
        }

}
