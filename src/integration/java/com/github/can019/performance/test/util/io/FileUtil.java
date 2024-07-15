package com.github.can019.performance.test.util.io;

import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

@Slf4j
public class FileUtil {
    public static void createDirectory(Path path) {
        try {
            if(!Files.exists(path)){
                Files.createDirectories(path);
            }
        } catch (IOException e) {
            log.error("Error creating directory: {}", e.getMessage());
        }
    }
}
