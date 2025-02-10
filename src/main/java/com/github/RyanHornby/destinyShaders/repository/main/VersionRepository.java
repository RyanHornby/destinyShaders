package com.github.RyanHornby.destinyShaders.repository.main;

import com.github.RyanHornby.destinyShaders.model.entity.VersionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface VersionRepository extends JpaRepository<VersionEntity, String> {
}
