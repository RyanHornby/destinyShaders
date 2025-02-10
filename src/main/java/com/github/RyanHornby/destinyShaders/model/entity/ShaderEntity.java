package com.github.RyanHornby.destinyShaders.model.entity;

import com.github.RyanHornby.destinyShaders.model.Color;
import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
@Table(name = "shaders")
public class ShaderEntity {
    @Id
    @Basic
    @Column(name = "id")
    private int id;
    @Basic
    @Column(name = "name")
    private String name;
    @Basic
    @Column(name = "image_path")
    private String imagePath;
    @Basic
    @Column(name = "inner_center")
    @Enumerated(EnumType.STRING)
    private Color innerCenter;
    @Basic
    @Column(name = "outer_center")
    @Enumerated(EnumType.STRING)
    private Color outerCenter;
    @Basic
    @Column(name = "trim_upper")
    @Enumerated(EnumType.STRING)
    private Color trimUpper;
    @Basic
    @Column(name = "trim_lower")
    @Enumerated(EnumType.STRING)
    private Color trimLower;
    @Basic
    @Column(name = "left")
    @Enumerated(EnumType.STRING)
    private Color left;
    @Basic
    @Column(name = "right")
    @Enumerated(EnumType.STRING)
    private Color right;
    @Basic
    @Column(name = "up")
    @Enumerated(EnumType.STRING)
    private Color up;
    @Basic
    @Column(name = "down")
    @Enumerated(EnumType.STRING)
    private Color down;
}
