package com.github.RyanHornby.destinyShaders.model.entity;

import jakarta.persistence.Basic;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

@Entity
@Data
@Table(name = "DestinyCollectibleDefinition")
public class DestinyTempEntity {
    @Id
    @Basic
    private int id;
    @Basic
    private String json;
}
