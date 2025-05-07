package com.devstack.system.api;

import com.devstack.system.dto.request.RequestSystemUserAvatarDto;
import com.devstack.system.service.SystemUserAvatarService;
import com.devstack.system.service.impl.JwtService;
import com.devstack.system.util.StandardResponse;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.sql.SQLException;

@RestController
@RequestMapping("/user-service/api/v1/tests")
@RequiredArgsConstructor
public class TestController {

    @GetMapping("/visitor/test")
    public ResponseEntity<StandardResponse> test() {
    
        return new ResponseEntity<>(
                new StandardResponse(
                        200, "Success", null
                ),
                HttpStatus.OK
        );
    }

}
