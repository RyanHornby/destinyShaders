package com.github.RyanHornby.destinyShaders.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.RyanHornby.destinyShaders.model.ColorSampleRegions;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.*;
import java.net.http.HttpClient;
import java.nio.file.Files;
import java.nio.file.Paths;

@Configuration
public class Config {
    @Value("\\main.db")
    private String mainDb;

    @Value("\\temp.db")
    private String tempDb;

    @Value("\\icons\\")
    private String icons;

    @Value("https://www.bungie.net")
    private String baseUrl;

    @Value("\\config.json")
    private String configJson;

    @Bean
    public HttpClient httpClient() {
        return HttpClient.newHttpClient();
    }

    @Bean
    @Qualifier("fileBasePath")
    public String fileBasePath() {
        String fileBasePath = "";
        if (System.getenv("APPDATA") == null) {
            if (System.getProperty("user.home") == null) {
                fileBasePath = ".\\DestinyShaderFinder";
            } else {
                fileBasePath = System.getProperty("user.home") + "\\DestinyShaderFinder";
            }
        } else {
            fileBasePath = System.getenv("APPDATA") + "\\DestinyShaderFinder";
        }

        new File(fileBasePath).mkdirs();
        return fileBasePath;
    }

    @Bean
    public String mainDbLocation(@Qualifier("fileBasePath") String fileBasePath) {
        return fileBasePath + mainDb;
    }

    @Bean
    public String tempDbLocation(@Qualifier("fileBasePath") String fileBasePath) {
        return fileBasePath + tempDb;
    }

    @Bean
    public String iconsLocation(@Qualifier("fileBasePath") String fileBasePath) {
        return fileBasePath + icons;
    }

    @Bean
    @Qualifier("configJsonPath")
    public String configJsonPath(@Qualifier("fileBasePath") String fileBasePath) {
        return fileBasePath + configJson;
    }

    @Bean
    public String baseUrl() {
        return baseUrl;
    }

    @Bean
    public ColorSampleRegions colorSampleRegions(@Qualifier("configJsonPath") String configJsonPath) {
        try {
            File config = new File(configJsonPath);
            if (config.createNewFile()) {
                try (FileWriter writer = new FileWriter(configJsonPath)) {
                    InputStreamReader is = new InputStreamReader(this.getClass().getResourceAsStream("/config.json"));
                    is.transferTo(writer);
                    //writer.write(new String(Files.readAllBytes(Paths.get(this.getClass().getResource("/config.json").toURI()))));
                }
            }
            ObjectMapper mapper = new ObjectMapper();
            return mapper.readValue(config, ColorSampleRegions.class);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
