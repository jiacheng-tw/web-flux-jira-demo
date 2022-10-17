package com.example.webfluxjirademo.service;

import com.example.webfluxjirademo.domain.comment.CommentDetail;
import com.example.webfluxjirademo.domain.issue.Issue;
import com.example.webfluxjirademo.domain.issue.Issues;
import com.example.webfluxjirademo.exception.BoardNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
public class IssueService {

    private final WebClient webClient;

    public IssueService(WebClient webClient) {
        this.webClient = webClient;
    }

    public Flux<Issue> findAllIssues(int boardId) {
        return webClient.get()
                .uri(String.format("/board/%d/issue", boardId))
                .retrieve()
                .onStatus(HttpStatus::is4xxClientError, response -> Mono.error(BoardNotFoundException::new))
                .bodyToMono(Issues.class)
                .flatMapMany(issues -> Flux.fromIterable(issues.getIssues()));
    }

    public Flux<Issue> findIssuesByStatus(int boardId, int statusId) {
        return findAllIssues(boardId)
                .filter(issue -> issue.getFields().getStatus().getId() == statusId);
    }

    public Flux<Issue> findIssuesByPoint(int boardId, int point) {
        return null;
    }

    public Mono<Issue> findIssueById(int id) {
        return webClient.get()
                .uri("/issue/" + id)
                .retrieve()
                .bodyToMono(Issue.class);
    }

    public Flux<CommentDetail> findIssueCommentsById(int id, int pageSize, int pageNum) {
        return findIssueById(id)
                .flatMapMany(issue -> Flux.fromIterable(issue.getFields().getComment().getComments()))
                .skip((long) (pageNum - 1) * pageSize)
                .take(pageSize);
    }
}
