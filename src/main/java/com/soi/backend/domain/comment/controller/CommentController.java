package com.soi.backend.domain.comment.controller;

import com.soi.backend.domain.comment.service.CommentService;
import com.soi.backend.global.exception.BaseController;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@GetMapping("/comment")


public class CommentController extends BaseController {

    private final CommentService commentService;
}
