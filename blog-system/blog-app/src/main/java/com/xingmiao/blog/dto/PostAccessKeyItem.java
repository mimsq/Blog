package com.xingmiao.blog.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PostAccessKeyItem {
    @NotBlank(message = "口令不能为空")
    private String password;
    private String label;
    private LocalDateTime startsAt; // 可空，表示现在生效
    private LocalDateTime endsAt;   // 可空，若为空且有 durationHours 则由服务端计算
    private Integer durationHours;  // 可空，优先级低于 endsAt
}



