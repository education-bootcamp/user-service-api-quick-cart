package com.devstack.quickcart.user_service_api.api;

import lombok.Getter;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequestMapping("/api/v1/test")
@RestController
public class TestController {
    @GetMapping
    public String checkServerStatus(){
        return "WORKING!...";
    }
}
