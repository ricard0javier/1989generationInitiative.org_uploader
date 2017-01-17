package com.thecodestein.controller;

import com.google.common.base.Strings;
import com.thecodestein.service.FileService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Set;
import java.util.stream.Collectors;

import static org.springframework.web.bind.annotation.RequestMethod.GET;

/**
 * Provide the mechanisms to interact with the existent articles shared in the static folder
 */
@Slf4j
@RestController
@RequestMapping(value = "/api")
public class ArticlesController {

    private final String articlesRelativePath;

    private FileService fileService;

    private String articlesPath;

    @Autowired
    public ArticlesController(FileService fileService,
                              @Value("${thecodestein.articlesFullPath}") String articlesPath,
                              @Value("${thecodestein.articlesRelativePath}") String articlesRelativePath) {
        this.fileService = fileService;
        this.articlesPath = articlesPath;
        this.articlesRelativePath = articlesRelativePath;
    }

    @RequestMapping(value = "/articles", method = GET)
    public Set<String> getArticles(HttpServletRequest request) {
        Path path = Paths.get(articlesPath);
        String contextUrl = getContextPath(request);
        return fileService.getFiles(path.toFile())
                .stream()
                .map(fileName -> contextUrl + articlesRelativePath + fileName)
                .collect(Collectors.toSet());
    }

    private String getContextPath(HttpServletRequest request) {

        String contextHeader = request.getHeader("X-Forwarded-Context");
        if (!Strings.isNullOrEmpty(contextHeader)) {
            log.debug("using context header '{}'='{}'", "X-Forwarded-Context", contextHeader);
            return contextHeader;
        }

        String port;

        if (request.getServerPort() == 80) {
            port = "";
        }
        else if (request.getServerPort() == 443) {
            port = "";
        }
        else {
            port = ":" + request.getServerPort();
        }

        String template = "{scheme}://{serverName}{port}/";
        template = template.replace("{scheme}", request.getScheme());
        template = template.replace("{serverName}", request.getServerName());
        template = template.replace("{port}", port);

        log.debug("Context Path resolved '{}'", template);
        return template;
    }
}
