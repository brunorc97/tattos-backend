package com.tattos.repository;

import com.tattos.model.Artist;
import com.tattos.model.enums.ArtistTag;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ArtistRepository extends JpaRepository<Artist, Long> {
    List<Artist> findByTag(ArtistTag tag);

    @Query("SELECT a FROM Artist a WHERE " +
           "LOWER(a.name) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(a.specialty) LIKE LOWER(CONCAT('%', :query, '%'))")
    List<Artist> searchArtists(@Param("query") String query);
}
