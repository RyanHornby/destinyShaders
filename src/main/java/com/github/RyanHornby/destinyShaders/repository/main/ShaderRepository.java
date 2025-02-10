package com.github.RyanHornby.destinyShaders.repository.main;

import com.github.RyanHornby.destinyShaders.model.entity.ShaderEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ShaderRepository extends JpaRepository<ShaderEntity, Integer> {
}
