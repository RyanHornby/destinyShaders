package com.github.RyanHornby.destinyShaders.repository.temp;

import com.github.RyanHornby.destinyShaders.model.entity.DestinyTempEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DestinyTempRepository extends JpaRepository<DestinyTempEntity, Integer> {
    @Query("SELECT d FROM DestinyTempEntity d WHERE json LIKE '%shader%'")
    public List<DestinyTempEntity> findAllShaders();
}
