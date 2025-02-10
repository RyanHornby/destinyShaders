package com.github.RyanHornby.destinyShaders;

import com.github.RyanHornby.destinyShaders.service.ShaderService;
import com.github.RyanHornby.destinyShaders.ui.MainUIWindow;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;

@SpringBootApplication
@Slf4j
public class Main implements CommandLineRunner {
    @Autowired
    private ShaderService shaderService;
    @Autowired
    private String iconsLocation;

    public static void main(String[] args) {
        new SpringApplicationBuilder(Main.class).headless(false).run(args);
    }

    @Override
    public void run(String... args) throws Exception {
        MainUIWindow.main(shaderService);
    }
}