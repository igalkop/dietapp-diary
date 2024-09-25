package com.ikop.diet.repository;

import com.ikop.diet.model.DiaryEntry;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface DiaryEntryRepository extends JpaRepository<DiaryEntry, Long> {
    List<DiaryEntry> findAllByDate(LocalDate date);
}
