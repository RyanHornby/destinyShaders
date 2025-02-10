package com.github.RyanHornby.destinyShaders.model.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class HttpException extends RuntimeException {
    private int httpCode;
}
