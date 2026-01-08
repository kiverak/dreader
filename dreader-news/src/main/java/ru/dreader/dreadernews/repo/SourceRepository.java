package ru.dreader.dreadernews.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ru.dreader.dreadernews.entity.Source;

import java.util.List;
import java.util.Optional;

@Repository
public interface SourceRepository extends JpaRepository<Source, Long> {
    Optional<Source> findByName(String name);

    @Query(value = "SELECT * FROM source WHERE name IN :sourceNames", nativeQuery = true)
    List<Source> findAllBySourceNames(@Param("sourceNames") List<String> sourceNames);
}
