package com.ikop.diet.service;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.text.MessageFormat;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class DiaryEntryNotFoundException extends RuntimeException {
    public DiaryEntryNotFoundException(String id) {
        super(MessageFormat.format("Diary Entry with id {0} not found", id));
    }
}
