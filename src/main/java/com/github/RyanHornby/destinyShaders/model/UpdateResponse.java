package com.github.RyanHornby.destinyShaders.model;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class UpdateResponse {
    private Boolean updateNeeded;
    private String path;
    private String version;
}
