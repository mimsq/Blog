package com.xingmiao.blog.controller;

import com.xingmiao.blog.dto.GithubContributionResponse;
import com.xingmiao.blog.service.GithubService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/github")
@RequiredArgsConstructor
@Tag(name = "GitHub API", description = "GitHub相关接口")
@CrossOrigin(origins = "*") // 允许跨域访问
public class GithubController {

    private final GithubService githubService;

    @GetMapping("/contributions")
    @Operation(summary = "获取GitHub贡献图数据", description = "获取指定用户的GitHub贡献图SVG和解析数据")
    public ResponseEntity<GithubContributionResponse> getContributions(
            @Parameter(description = "GitHub用户名", example = "octocat")
            @RequestParam String username,
            
            @Parameter(description = "开始日期 (YYYY-MM-DD)", required = false)
            @RequestParam(required = false) String from,
            
            @Parameter(description = "结束日期 (YYYY-MM-DD)", required = false)
            @RequestParam(required = false) String to) {
        
        try {
            GithubContributionResponse response = githubService.getContributions(username, from, to);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(GithubContributionResponse.builder()
                            .success(false)
                            .error(e.getMessage())
                            .build());
        }
    }

    @GetMapping("/contributions/svg")
    @Operation(summary = "获取GitHub贡献图SVG", description = "仅获取贡献图的SVG内容")
    public ResponseEntity<String> getContributionsSvg(
            @Parameter(description = "GitHub用户名", example = "octocat")
            @RequestParam String username,
            
            @Parameter(description = "开始日期 (YYYY-MM-DD)", required = false)
            @RequestParam(required = false) String from,
            
            @Parameter(description = "结束日期 (YYYY-MM-DD)", required = false)
            @RequestParam(required = false) String to) {
        
        try {
            String svg = githubService.getContributionsSvg(username, from, to);
            return ResponseEntity.ok()
                    .header("Content-Type", "image/svg+xml")
                    .body(svg);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        }
    }

    @GetMapping("/contributions/data")
    @Operation(summary = "获取GitHub贡献数据", description = "仅获取解析后的贡献数据")
    public ResponseEntity<Map<String, Object>> getContributionsData(
            @Parameter(description = "GitHub用户名", example = "octocat")
            @RequestParam String username,
            
            @Parameter(description = "开始日期 (YYYY-MM-DD)", required = false)
            @RequestParam(required = false) String from,
            
            @Parameter(description = "结束日期 (YYYY-MM-DD)", required = false)
            @RequestParam(required = false) String to) {
        
        try {
            var data = githubService.getContributionsData(username, from, to);
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "username", username,
                    "data", data
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "error", e.getMessage()
            ));
        }
    }
}
