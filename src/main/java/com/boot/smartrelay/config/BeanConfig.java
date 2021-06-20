package com.boot.smartrelay.config;

import com.boot.smartrelay.beans.AdminUser;
import com.boot.smartrelay.beans.User;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.context.annotation.RequestScope;
import org.springframework.web.context.annotation.SessionScope;

@Configuration
public class BeanConfig {

    @Bean(name="loginAdminUser")
    @SessionScope
    public AdminUser loginAdminUser(){
        return new AdminUser();
    }

    @Bean
    @SessionScope
    public User loginUser(){
        return new User();
    }
}
