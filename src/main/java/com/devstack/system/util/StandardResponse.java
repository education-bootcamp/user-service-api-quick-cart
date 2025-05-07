package com.devstack.system.util;

import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class StandardResponse {
    private int code;
    private String message;
    private Object data;
}
