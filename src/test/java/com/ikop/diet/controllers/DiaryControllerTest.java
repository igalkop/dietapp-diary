package com.ikop.diet.controllers;

import com.ikop.diet.model.DiaryEntryCreateDTO;
import com.ikop.diet.model.DiaryEntryUpdateDTO;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCollection;

class DiaryControllerTest {

    private final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

    @Test
    void testEmptyDiaryCreateDto() {
        List<String> expectedMessages = List.of(
                "foodName cannot be empty",
                "foodPoints cannot be empty",
                "amount cannot be empty",
                "date cannot be empty"
        );
        DiaryEntryCreateDTO diaryEntryCreateDTO = new DiaryEntryCreateDTO();

        Set<ConstraintViolation<DiaryEntryCreateDTO>> errors = validator.validate(diaryEntryCreateDTO);

        assertThat(errors).isNotEmpty();
        List<String> actualErrorList = errors.stream().map(ConstraintViolation::getMessage).toList();
        assertThatCollection(actualErrorList).containsExactlyInAnyOrderElementsOf(expectedMessages);
    }

    @Test
    void testNotValidDiaryCreateDto() {
        List<String> expectedMessages = List.of(
                "foodPoints must be positive",
                "amount must be positive",
                "date must be today or previous date"
        );
        DiaryEntryCreateDTO diaryEntryCreateDTO = new DiaryEntryCreateDTO("food1", -2.3d, -2d, LocalDate.now().plusDays(1));

        Set<ConstraintViolation<DiaryEntryCreateDTO>> errors = validator.validate(diaryEntryCreateDTO);

        assertThat(errors).isNotEmpty();
        List<String> actualErrorList = errors.stream().map(ConstraintViolation::getMessage).toList();
        assertThatCollection(actualErrorList).containsExactlyInAnyOrderElementsOf(expectedMessages);
    }


    @Test
    void testEmptyDiaryUpdateDto() {
        List<String> expectedMessages = List.of(
                "foodName cannot be empty",
                "foodPoints cannot be empty",
                "amount cannot be empty",
                "date cannot be empty",
                "id cannot be empty"
        );
        DiaryEntryUpdateDTO diaryEntryUpdateDTO = new DiaryEntryUpdateDTO();

        Set<ConstraintViolation<DiaryEntryUpdateDTO>> errors = validator.validate(diaryEntryUpdateDTO);

        assertThat(errors).isNotEmpty();
        List<String> actualErrorList = errors.stream().map(ConstraintViolation::getMessage).toList();
        assertThatCollection(actualErrorList).containsExactlyInAnyOrderElementsOf(expectedMessages);
    }


    @Test
    void testNotValidDiaryUpdateDto() {
        List<String> expectedMessages = List.of(
                "foodPoints must be positive",
                "amount must be positive",
                "date must be today or previous date"
        );
        DiaryEntryUpdateDTO diaryEntryUpdateDTO = new DiaryEntryUpdateDTO(123L, "food1", -2.3d, -2d, LocalDate.now().plusDays(1));

        Set<ConstraintViolation<DiaryEntryUpdateDTO>> errors = validator.validate(diaryEntryUpdateDTO);

        assertThat(errors).isNotEmpty();
        List<String> actualErrorList = errors.stream().map(ConstraintViolation::getMessage).toList();
        assertThatCollection(actualErrorList).containsExactlyInAnyOrderElementsOf(expectedMessages);
    }
}