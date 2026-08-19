package com.sombackend.nextccs.service;

import org.springframework.stereotype.Service;

@Service
public class HelloService {

    public String getHelloMessage() {
        return "Welcome to Next-CCS Backend!";
    }
}