package com.devstack.quickcart.user_service_api.dto.response.paginate;

import com.devstack.quickcart.user_service_api.dto.response.ResponseUserDto;

import java.util.List;

public class ResponseUserPaginateDto {
    private long count;
    private List<ResponseUserDto> users;
}
