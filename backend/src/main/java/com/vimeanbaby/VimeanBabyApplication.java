package com.vimeanbaby;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.UserDetailsServiceAutoConfiguration;

@SpringBootApplication(exclude = UserDetailsServiceAutoConfiguration.class)
public class VimeanBabyApplication {

    public static void main(String[] args) {
        SpringApplication.run(VimeanBabyApplication.class, args);
    }
}
