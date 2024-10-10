package com.example.SpringBot.config;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.scheduling.annotation.EnableScheduling;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

@Configuration
@Data
@PropertySource("application.properties")
public class BotConfig {

    @Value("${bot.token}")
    String token;

}
//@Configuration
//@EnableScheduling
//@Data
//@PropertySource("application.properties")
//public class BotConfig {
//@Value("${bot.name}")
//    String botName;
//@Value("${bot.token}")
//    String botToken;
//@Value("${bot.owner}")
//    Long ownerId;

//    public String getBotName() {
//        return botName;
//    }
//
//    public String getBotToken() {
//        return botToken;
//    }
//
//    @Value("${bot.owner}")
//    Long ownerId;

