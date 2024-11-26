package com.campers;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class Application {

    @PostConstruct
    public void init() {
        try {
            Files.createDirectories(Paths.get("upload-dir"));
        } catch (IOException e) {
            throw new RuntimeException("Could not initialize storage", e);
        }
    }
}
