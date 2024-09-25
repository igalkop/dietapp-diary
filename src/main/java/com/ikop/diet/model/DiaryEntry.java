package com.ikop.diet.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Table(name = "diary_entry")
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Data
public class DiaryEntry {


    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, name = "food_name")
    private String foodName;

    @Column(nullable = false, name = "food_points")
    private double foodPoints;

    @Column(nullable = false, name = "amount")
    private double amount;

    @Column(nullable = false, name = "date")
    private LocalDate date;
}
