package com.rioni.lk.api.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "glossary_entries", uniqueConstraints = @UniqueConstraint(columnNames = {"source_no", "language"}))
public class GlossaryEntry {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(name = "source_no", nullable = false)
    private int sourceNo;

    @Column(nullable = false, length = 5)
    private String language;

    @Column(nullable = false, length = 10)
    private String letter;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String term;

    @Column(columnDefinition = "TEXT")
    private String english;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String definition;
}
