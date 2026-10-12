package com.tattos.repository;

import com.tattos.model.KanbanCard;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface KanbanCardRepository extends JpaRepository<KanbanCard, Long> {
    // Additional query methods if needed
}
