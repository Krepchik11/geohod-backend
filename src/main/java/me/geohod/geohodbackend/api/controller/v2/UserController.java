package me.geohod.geohodbackend.api.controller.v2;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;
import me.geohod.geohodbackend.api.dto.response.UserResponse;
import me.geohod.geohodbackend.api.mapper.UserApiMapper;
import me.geohod.geohodbackend.api.response.ApiResponse;
import me.geohod.geohodbackend.data.dto.UserDto;
import me.geohod.geohodbackend.service.impl.UserService;

@RestController
@RequestMapping("/api/v2/users")
@RequiredArgsConstructor
public class UserController {
    private final UserApiMapper userMapper;
    private final UserService userService;

    @GetMapping("/by-tg-id/{tgId}")
    public ApiResponse<UserResponse> userByTgId(@PathVariable String tgId) {
        UserDto user = userService.getUserByTelegramId(tgId);
        UserResponse response = userMapper.map(user);
        
        return ApiResponse.success(response);
    }
}
