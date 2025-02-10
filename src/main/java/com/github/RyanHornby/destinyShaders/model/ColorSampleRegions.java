package com.github.RyanHornby.destinyShaders.model;

import lombok.Getter;

import java.util.List;

@Getter
public class ColorSampleRegions {
    private List<SampleRegion> innerCenter;
    private List<SampleRegion> outerCenter;
    private List<SampleRegion> trimUpper;
    private List<SampleRegion> trimLower;
    private List<SampleRegion> left;
    private List<SampleRegion> right;
    private List<SampleRegion> up;
    private List<SampleRegion> down;
}
