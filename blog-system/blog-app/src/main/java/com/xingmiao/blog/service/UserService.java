package com.xingmiao.blog.service;

import com.xingmiao.blog.dto.UserCreateRequest;
import com.xingmiao.blog.dto.UserDto;
import com.xingmiao.blog.dto.UserUpdateRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

public interface UserService {
    UserDto create(UserCreateRequest request);
    UserDto update(Long id, UserUpdateRequest request);
    void delete(Long id);
    Optional<UserDto> getById(Long id);
    Optional<UserDto> getByUsername(String username);
    Page<UserDto> list(Pageable pageable);
}


