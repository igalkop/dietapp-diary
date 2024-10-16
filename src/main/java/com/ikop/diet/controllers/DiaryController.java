package com.ikop.diet.controllers;

import com.ikop.diet.mapper.DiaryEntryMapper;
import com.ikop.diet.model.*;
import com.ikop.diet.service.DiaryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Slf4j
public class DiaryController {

    private final DiaryService diaryService;
    private final DiaryEntryMapper mapper;


    @PostMapping
    public ResponseEntity<DiaryEntryDTO> createDiaryEntry(@Valid @RequestBody DiaryEntryCreateDTO diaryEntryCreateDTO) {
        log.info("request to create a Diary Entry: {}", diaryEntryCreateDTO);
        DiaryEntry diaryEntryCreated = diaryService.save(mapper.diaryEntryCreateDtoToDiaryEntry(diaryEntryCreateDTO));
        log.info("Diary Entry successfully created");
        return new ResponseEntity<>(mapper.diaryEntryToDiaryEntryDto(diaryEntryCreated), HttpStatusCode.valueOf(201));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Void> updateDiaryEntry(@PathVariable Long id, @Valid @RequestBody DiaryEntryUpdateDTO diaryEntryToUpdate) {
        log.info("request to update a Diary Entry: {}", diaryEntryToUpdate);
        diaryService.update(id, mapper.diaryEntryUpdateDtoToDiaryEntry(diaryEntryToUpdate));
        log.info("Diary Entry successfully updated");
        return ResponseEntity.ok(null);
    }

    @GetMapping("/list/{year}/{month}/{day}")
    public ResponseEntity<DateInfoSummaryDTO> getDateSummary(@PathVariable Integer year, @PathVariable Integer month, @PathVariable Integer day) {
        LocalDate date = LocalDate.of(year, month, day);
        log.info("request to get diary summery for date: {}", date);
        DateInfoSummary allEntriesForDate = diaryService.getInfoForDate(date);
        return ResponseEntity.ok(mapper.dateInfoSummaryToDateInfoSummaryDto(allEntriesForDate));
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public Map<String, String> handleValidationExceptions(
            MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });
        return errors;
    }
}
