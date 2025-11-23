package com.soi.backend.domain.comment.controller;

import com.soi.backend.domain.comment.service.CommentService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/comment")


public class CommentController {

    private final CommentService commentService;
}
