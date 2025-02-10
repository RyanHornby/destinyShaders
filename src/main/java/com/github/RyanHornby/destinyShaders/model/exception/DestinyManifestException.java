package com.github.RyanHornby.destinyShaders.model.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class DestinyManifestException extends RuntimeException {
    private int errorCode;
}
