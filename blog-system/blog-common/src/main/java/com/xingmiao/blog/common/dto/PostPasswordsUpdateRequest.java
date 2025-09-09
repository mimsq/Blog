package com.xingmiao.blog.common.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PostPasswordsUpdateRequest {
    @NotNull
    private String mode; // replace | append
    @NotNull
    private List<PostAccessKeyItem> items;
}



