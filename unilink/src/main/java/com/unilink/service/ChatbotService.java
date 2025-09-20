package com.unilink.service;

import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.http.client.MultipartBodyBuilder;
import org.springframework.http.codec.multipart.FilePart;
import reactor.core.publisher.Mono;

import java.util.Map;

@Service
public class ChatbotService {

    private final WebClient client;

    public ChatbotService() {
        this.client = WebClient.builder()
                .baseUrl("http://localhost:8000")   // FastAPI base URL
                .build();
    }

    /** ---------------- Base ---------------- */
    public Mono<String> welcome() {
        return client.get().uri("/")
                .retrieve()
                .bodyToMono(String.class);
    }

    /** ---------------- Data ---------------- */
    public Mono<String> uploadData(String projectId, FilePart file) {
        MultipartBodyBuilder builder = new MultipartBodyBuilder();
        builder.part("file", file);

        return client.post()
                .uri("/data/upload/{projectId}", projectId)
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .body(BodyInserters.fromMultipartData(builder.build()))
                .retrieve()
                .bodyToMono(String.class);
    }

    public Mono<String> processData(String projectId, Map<String,Object> body) {
        return client.post()
                .uri("/data/process/{projectId}", projectId)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(body)
                .retrieve()
                .bodyToMono(String.class);
    }

    /** ---------------- NLP ---------------- */
    public Mono<String> indexPush(String projectId, Map<String,Object> body) {
        return client.post()
                .uri("/nlp/index/push/{projectId}", projectId)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(body)
                .retrieve()
                .bodyToMono(String.class);
    }

    public Mono<String> indexInfo(String projectId) {
        return client.get()
                .uri("/nlp/index/info/{projectId}", projectId)
                .retrieve()
                .bodyToMono(String.class);
    }

    public Mono<String> indexSearch(String projectId, Map<String,Object> body) {
        return client.post()
                .uri("/nlp/index/search/{projectId}", projectId)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(body)
                .retrieve()
                .bodyToMono(String.class);
    }

    public Mono<String> answerRag(String projectId, Map<String,Object> body) {
        return client.post()
                .uri("/nlp/index/answer/{projectId}", projectId)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(body)
                .retrieve()
                .bodyToMono(String.class);
    }
}
