package com.group06.music_app;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/hello")
public class SayHello {

    @GetMapping
    public String hello() {
        return "Hello Viet Nam";
    }
}
