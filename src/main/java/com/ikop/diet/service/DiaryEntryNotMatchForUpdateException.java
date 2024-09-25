package com.ikop.diet.service;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.text.MessageFormat;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class DiaryEntryNotMatchForUpdateException extends RuntimeException {
    public DiaryEntryNotMatchForUpdateException(String idInPath, String diaryEntryEntityId) {
        super(MessageFormat.format("provided id for update {0} not match the diary entry id {1}", idInPath, diaryEntryEntityId));
    }
}
