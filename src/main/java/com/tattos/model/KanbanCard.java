package com.tattos.model;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(name = "kanban_cards")
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class KanbanCard {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 255)
    private String titulo;

    @Column(columnDefinition = "TEXT")
    private String descricao;

    @Column(nullable = false, length = 50)
    private String status;

    @Column(nullable = false)
    private Integer ordem;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "orcamento_id")
    private Orcamento orcamento;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "projeto_id")
    private Projeto projeto;

    @CreatedDate
    @Column(name = "data_criacao", nullable = false, updatable = false)
    private LocalDateTime dataCriacao;
}
