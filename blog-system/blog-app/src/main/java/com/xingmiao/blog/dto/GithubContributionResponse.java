package com.xingmiao.blog.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
@Builder
public class GithubContributionResponse {
    
    private boolean success;
    private String username;
    private String svg;
    private List<ContributionData> data;
    private String error;
    
    @Data
    @Builder
    public static class ContributionData {
        private String date;
        private int count;
        private int level;
    }
}
