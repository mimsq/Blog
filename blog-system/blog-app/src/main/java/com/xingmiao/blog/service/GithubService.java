package com.xingmiao.blog.service;

import com.xingmiao.blog.dto.GithubContributionResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
public class GithubService {

    private final HttpClient httpClient;
    private static final String GITHUB_CONTRIBUTIONS_URL = "https://github.com/users/%s/contributions";

    public GithubService() {
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .build();
    }

    @Cacheable(value = "github-contributions", key = "#username + '-' + #from + '-' + #to")
    public GithubContributionResponse getContributions(String username, String from, String to) {
        try {
            String svg = getContributionsSvg(username, from, to);
            List<GithubContributionResponse.ContributionData> data = parseSvgToData(svg);
            
            return GithubContributionResponse.builder()
                    .success(true)
                    .username(username)
                    .svg(svg)
                    .data(data)
                    .build();
        } catch (Exception e) {
            log.error("获取GitHub贡献图失败: {}", e.getMessage(), e);
            throw new RuntimeException("获取GitHub贡献图失败: " + e.getMessage());
        }
    }

    @Cacheable(value = "github-contributions-svg", key = "#username + '-' + #from + '-' + #to")
    public String getContributionsSvg(String username, String from, String to) throws IOException, InterruptedException {
        String url = buildContributionsUrl(username, from, to);
        
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36")
                .header("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8")
                .GET()
                .timeout(Duration.ofSeconds(15))
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        
        if (response.statusCode() != 200) {
            throw new RuntimeException("GitHub API返回错误状态码: " + response.statusCode());
        }

        String html = response.body();
        return extractSvgFromHtml(html);
    }

    public List<GithubContributionResponse.ContributionData> getContributionsData(String username, String from, String to) {
        try {
            String svg = getContributionsSvg(username, from, to);
            return parseSvgToData(svg);
        } catch (Exception e) {
            log.error("获取贡献数据失败: {}", e.getMessage(), e);
            throw new RuntimeException("获取贡献数据失败: " + e.getMessage());
        }
    }

    private String buildContributionsUrl(String username, String from, String to) {
        StringBuilder url = new StringBuilder(String.format(GITHUB_CONTRIBUTIONS_URL, username));
        
        if (from != null || to != null) {
            url.append("?");
            List<String> params = new ArrayList<>();
            if (from != null) params.add("from=" + from);
            if (to != null) params.add("to=" + to);
            url.append(String.join("&", params));
        }
        
        return url.toString();
    }

    private String extractSvgFromHtml(String html) {
        // 简单的HTML解析，提取SVG元素
        int svgStart = html.indexOf("<svg");
        if (svgStart == -1) {
            throw new RuntimeException("未找到SVG元素");
        }
        
        int svgEnd = html.indexOf("</svg>", svgStart);
        if (svgEnd == -1) {
            throw new RuntimeException("SVG元素不完整");
        }
        
        return html.substring(svgStart, svgEnd + 6);
    }

    private List<GithubContributionResponse.ContributionData> parseSvgToData(String svg) {
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.parse(new ByteArrayInputStream(svg.getBytes()));
            
            NodeList rects = doc.getElementsByTagName("rect");
            List<GithubContributionResponse.ContributionData> data = new ArrayList<>();
            
            for (int i = 0; i < rects.getLength(); i++) {
                Element rect = (Element) rects.item(i);
                String date = rect.getAttribute("data-date");
                
                if (!date.isEmpty()) {
                    int count = Integer.parseInt(rect.getAttribute("data-count"));
                    int level = Integer.parseInt(rect.getAttribute("data-level"));
                    
                    data.add(GithubContributionResponse.ContributionData.builder()
                            .date(date)
                            .count(count)
                            .level(level)
                            .build());
                }
            }
            
            return data;
        } catch (Exception e) {
            log.error("解析SVG数据失败: {}", e.getMessage(), e);
            throw new RuntimeException("解析SVG数据失败: " + e.getMessage());
        }
    }
}
