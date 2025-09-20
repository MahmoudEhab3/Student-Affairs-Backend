package com.unilink.controller;

import com.unilink.service.ChatbotService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;
import java.util.Map;

@RestController
@RequestMapping("/chatbot")
public class ChatbotController {

    @Autowired
    private ChatbotService service;

    /** Base */
    @GetMapping("/welcome")
    public Mono<String> welcome() {
        return service.welcome();
    }

    /** Data */
    @PostMapping("/data/upload/{projectId}")
    public Mono<String> upload(@PathVariable String projectId,
                               @RequestPart("file") FilePart file) {
        return service.uploadData(projectId, file);
    }

    @PostMapping("/data/process/{projectId}")
    public Mono<String> process(@PathVariable String projectId,
                                @RequestBody Map<String,Object> body) {
        return service.processData(projectId, body);
    }

    /** NLP */
    @PostMapping("/nlp/index/push/{projectId}")
    public Mono<String> push(@PathVariable String projectId,
                             @RequestBody Map<String,Object> body) {
        return service.indexPush(projectId, body);
    }

    @GetMapping("/nlp/index/info/{projectId}")
    public Mono<String> info(@PathVariable String projectId) {
        return service.indexInfo(projectId);
    }

    @PostMapping("/nlp/index/search/{projectId}")
    public Mono<String> search(@PathVariable String projectId,
                               @RequestBody Map<String,Object> body) {
        return service.indexSearch(projectId, body);
    }

    @PostMapping("/nlp/index/answer/{projectId}")
    public Mono<String> answer(@PathVariable String projectId,
                               @RequestBody Map<String,Object> body) {
        return service.answerRag(projectId, body);
    }
}
