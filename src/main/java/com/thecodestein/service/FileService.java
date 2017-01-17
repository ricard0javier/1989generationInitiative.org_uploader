package com.thecodestein.service;

import com.google.common.collect.Sets;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Provides the methods to operates files in a given path
 */
@Slf4j
@Service
public class FileService {

    public Set<String> getFiles(File root) {

        if (root == null || !root.exists() || !root.isDirectory()) {
            log.debug("root is null or does not exists or is not a directory");
            return Sets.newHashSet();
        }

        return Arrays.stream(root.listFiles())
                .map(File::getName)
                .collect(Collectors.toSet());

    }
}
