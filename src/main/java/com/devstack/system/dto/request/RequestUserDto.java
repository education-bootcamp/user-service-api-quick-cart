package com.devstack.system.dto.request;



import lombok.*;

import java.util.Date;
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
@ToString
public class RequestUserDto {

    private String email;
    private String firstName;
    private String lastName;
    private String password;
    private Date createdDate;
}
