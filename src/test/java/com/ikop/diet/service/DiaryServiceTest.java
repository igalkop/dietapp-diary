package com.ikop.diet.service;

import com.ikop.diet.model.DateInfoSummary;
import com.ikop.diet.model.DiaryEntry;
import com.ikop.diet.repository.DiaryEntryRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DiaryServiceTest {

    @Mock
    private DiaryEntryRepository diaryEntryRepository;

    @InjectMocks
    @Spy
    DiaryService diaryService;

    @Test
    void testSave() {
        LocalDate date = LocalDate.now();
        DiaryEntry diaryEntry = new DiaryEntry(null, "food 1", 2.3d, 1.5d, date);
        DiaryEntry savedDiaryEntry = new DiaryEntry(123L, "food 1", 2.3d, 1.5d, date);
        when(diaryEntryRepository.save(diaryEntry)).thenReturn(savedDiaryEntry);

        DiaryEntry result = diaryService.save(diaryEntry);

        assertThat(result).isEqualTo(savedDiaryEntry);
    }

    @Test
    void testGetAllForDate() {
        LocalDate date = LocalDate.now();
        List<DiaryEntry> allByDateFromDb = List.of(
                new DiaryEntry(123L, "food1", 1.0d, 2.5d, date),
                new DiaryEntry(456L, "food2", 1.0d, 2.5d, date),
                new DiaryEntry(789L, "food3", 1.0d, 2.5d, date)
        );
        when(diaryEntryRepository.findAllByDate(date)).thenReturn(allByDateFromDb);

        List<DiaryEntry> allForDate = diaryService.getAllForDate(date);

        assertThatCollection(allForDate).containsExactlyInAnyOrderElementsOf(allByDateFromDb);
    }

    @Test
    void testGetInfoForDate() {
        LocalDate date = LocalDate.now();
        List<DiaryEntry> allByDate = List.of(
                new DiaryEntry(123L, "food1", 1.0d, 2.5d, date),
                new DiaryEntry(456L, "food2", 2.0d, 3.5d, date),
                new DiaryEntry(789L, "food3", 3.0d, 4.5d, date)
        );
        when(diaryService.getAllForDate(date)).thenReturn(allByDate);
        DateInfoSummary expected = new DateInfoSummary();
        expected.setTotalPoints(1.0d * 2.5d + 2.0d * 3.5d + 3.0d * 4.5d);
        expected.setEntries(allByDate);

        DateInfoSummary infoForDate = diaryService.getInfoForDate(date);

        assertThat(infoForDate).isEqualTo(expected);
    }

    @Test
    void testUpdate() {
        LocalDate date = LocalDate.now();
        DiaryEntry diaryEntryToUpdate = new DiaryEntry(123L, "food 1", 1.5d, 4.2d, date);
        when(diaryEntryRepository.existsById(123L)).thenReturn(true);

        try {
            diaryService.update(123L, diaryEntryToUpdate);
        } catch (Throwable ex) {
            fail("No exception should be thrown");
        }
    }

    @Test
    void testUpdateWhenIdMismatch() {
        LocalDate date = LocalDate.now();
        DiaryEntry diaryEntryToUpdate = new DiaryEntry(123L, "food 1", 1.5d, 4.2d, date);

        Throwable thrown = catchThrowable(() -> {
            diaryService.update(456L, diaryEntryToUpdate);
        });

        assertThat(thrown).isInstanceOf(DiaryEntryNotMatchForUpdateException.class);
        assertThat(thrown).hasMessageContaining("provided id for update 456 not match the diary entry id 123");
    }

    @Test
    void testUpdateWhenEntityNotFoundInDb() {
        LocalDate date = LocalDate.now();
        DiaryEntry diaryEntryToUpdate = new DiaryEntry(123L, "food 1", 1.5d, 4.2d, date);
        when(diaryEntryRepository.existsById(123L)).thenReturn(false);

        Throwable thrown = catchThrowable(() -> {
            diaryService.update(123L, diaryEntryToUpdate);
        });

        assertThat(thrown).isInstanceOf(DiaryEntryNotFoundException.class);
        assertThat(thrown).hasMessageContaining("Diary Entry with id 123 not found");

    }
}