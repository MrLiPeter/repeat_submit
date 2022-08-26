package com.lxh.controller;

import com.lxh.annotation.RepeatSubmit;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HelloController {

    @PostMapping("/hello")
    @RepeatSubmit(interval = 10000)
    public String hello(@RequestBody String json){
        return json;
    }



}
