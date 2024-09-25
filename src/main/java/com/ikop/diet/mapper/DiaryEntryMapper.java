package com.ikop.diet.mapper;

import com.ikop.diet.model.*;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface DiaryEntryMapper {
    DiaryEntry diaryEntryCreateDtoToDiaryEntry(DiaryEntryCreateDTO diaryEntryCreateDTO);

    DiaryEntryDTO diaryEntryToDiaryEntryDto(DiaryEntry diaryEntryCreated);

    DiaryEntry diaryEntryUpdateDtoToDiaryEntry(DiaryEntryUpdateDTO diaryEntryToUpdate);


    DateInfoSummaryDTO dateInfoSummaryToDateInfoSummaryDto(DateInfoSummary allEntriesForDate);

}

