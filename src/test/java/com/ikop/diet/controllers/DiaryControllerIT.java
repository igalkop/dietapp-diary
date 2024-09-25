package com.ikop.diet.controllers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ikop.diet.mapper.DiaryEntryMapper;
import com.ikop.diet.model.*;
import com.ikop.diet.repository.DiaryEntryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.http.*;
import org.springframework.util.MultiValueMap;
import org.springframework.web.util.UriComponentsBuilder;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.net.URI;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.CollectionAssert.assertThatCollection;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@Testcontainers
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class DiaryControllerIT {


    @LocalServerPort
    private int port;

    private final String HOST = "localhost";

    @Autowired
    private DiaryEntryRepository diaryEntryRepository;

    @Autowired
    private ObjectMapper mapper;


    @Autowired
    private TestRestTemplate testRestTemplate;

    @Autowired
    private DiaryEntryMapper diaryEntryMapper;


    @Container
    @ServiceConnection
    static MySQLContainer<?> mySQLContainer = new MySQLContainer<>("mysql:8");


    @BeforeEach
    public void setUp() {
        diaryEntryRepository.deleteAll();
    }


    @Test
    void testGetAllForDate() {
        LocalDate now = LocalDate.now();
        DiaryEntry diaryEntry = new DiaryEntry(null, "food1", 3.3, 2, now);
        DiaryEntry diaryEntry2 = new DiaryEntry(null, "food2", 4.5, 1, now);
        DiaryEntry diaryEntry3 = new DiaryEntry(null, "food3", 1, 1, now);
        diaryEntryRepository.saveAll(List.of(diaryEntry, diaryEntry2, diaryEntry3));


        String getForDatePath = "/diary/api/list/YEAR/MONTH/DAY";
        getForDatePath = getForDatePath.replace("YEAR", String.valueOf(now.getYear())).replace("MONTH", String.valueOf(now.getMonthValue()))
                .replace("DAY", String.valueOf(now.getDayOfMonth()));
        URI urlGetEntriesForDate = UriComponentsBuilder.newInstance()
                .scheme("http")
                .host(HOST)
                .port(port)
                .path(getForDatePath)
                .build().toUri();

        ResponseEntity<DateInfoSummaryDTO> allDiaryEntriesForDate = testRestTemplate.getForEntity(urlGetEntriesForDate, DateInfoSummaryDTO.class);

        assertThat(allDiaryEntriesForDate.getStatusCode()).isEqualTo(HttpStatusCode.valueOf(200));
        assertThat(allDiaryEntriesForDate.getBody().getEntries().size()).isEqualTo(3);
        assertThatCollection(allDiaryEntriesForDate.getBody().getEntries()).containsExactlyInAnyOrderElementsOf(
                List.of(diaryEntryMapper.diaryEntryToDiaryEntryDto(diaryEntry), diaryEntryMapper.diaryEntryToDiaryEntryDto(diaryEntry2),
                        diaryEntryMapper.diaryEntryToDiaryEntryDto(diaryEntry3)));
        assertThat(allDiaryEntriesForDate.getBody().getTotalPoints()).isEqualTo(12.1);
    }

    @Test
    void testCreateDiaryEntry() {
        LocalDate date = LocalDate.now().minus(3, ChronoUnit.DAYS);
        DiaryEntryCreateDTO diaryEntry = new DiaryEntryCreateDTO("food 1", 1.5, 1d, date);
        DiaryEntryCreateDTO diaryEntry2 = new DiaryEntryCreateDTO("food 2", 2.5, 2d, date);
        DiaryEntryCreateDTO diaryEntry3 = new DiaryEntryCreateDTO("food 3", 3.5, 3d, date);
        List<DiaryEntryCreateDTO> allEntries = List.of(diaryEntry, diaryEntry2, diaryEntry3);

        URI urlCreateDiaryEntry = UriComponentsBuilder.newInstance()
                .scheme("http")
                .host(HOST)
                .port(port)
                .path("/diary/api")
                .build().toUri();


        for (DiaryEntryCreateDTO entry : allEntries) {
            ResponseEntity<DiaryEntryDTO> response = testRestTemplate.postForEntity(urlCreateDiaryEntry, entry, DiaryEntryDTO.class);
            assertThat(response.getStatusCode()).isEqualTo(HttpStatusCode.valueOf(201));
            assertThat(response.getBody())
                    .hasNoNullFieldsOrProperties()
                    .usingRecursiveComparison()
                    .ignoringFields("id")
                    .isEqualTo(entry);

        }

        long count = diaryEntryRepository.count();
        assertThat(count).isEqualTo(allEntries.size());
        assertThatCollection(diaryEntryRepository.findAll())
                .usingRecursiveFieldByFieldElementComparatorIgnoringFields("id")
                .isEqualTo(allEntries);
    }

    @Test
    void testUpdateDiaryEntryForDate() throws JsonProcessingException {
        LocalDate now = LocalDate.now();
        DiaryEntry diaryEntry = new DiaryEntry(null, "food1", 3.3, 2, now);
        DiaryEntry diaryEntry2 = new DiaryEntry(null, "food2", 4.5, 1, now);
        DiaryEntry diaryEntry3 = new DiaryEntry(null, "food3", 1, 1, now);
        diaryEntryRepository.saveAll(List.of(diaryEntry, diaryEntry2, diaryEntry3));

        String idPath = "/diary/api/ID";
        idPath = idPath.replace("ID", diaryEntry2.getId().toString());
        URI urlUpdateEntryForDate = UriComponentsBuilder.newInstance()
                .scheme("http")
                .host(HOST)
                .port(port)
                .path(idPath)
                .build().toUri();

        DiaryEntryUpdateDTO diaryEntryUpdateDTO = new DiaryEntryUpdateDTO(diaryEntry2.getId(), "food 2 updated", diaryEntry2.getFoodPoints(), 2d, diaryEntry2.getDate());
        String requestBody = mapper.writeValueAsString(diaryEntryUpdateDTO);
        MultiValueMap<String, String> headers = new HttpHeaders();
        headers.set(HttpHeaders.CONTENT_TYPE, APPLICATION_JSON_VALUE);
        HttpEntity<String> httpEntity = new HttpEntity<>(requestBody, headers);

        ResponseEntity<Void> responseEntity = testRestTemplate.exchange(urlUpdateEntryForDate, HttpMethod.PUT, httpEntity, Void.class);
        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatusCode.valueOf(200));
        assertThat(responseEntity.getBody()).isNull();

        Optional<DiaryEntry> optResponseAfterUpdateDbEntity = diaryEntryRepository.findById(diaryEntry2.getId());
        assertThat(optResponseAfterUpdateDbEntity).isPresent();
        DiaryEntry diaryEntryAfterUpdate = optResponseAfterUpdateDbEntity.get();
        assertThat(diaryEntryAfterUpdate)
                .hasNoNullFieldsOrProperties()
                .usingRecursiveComparison()
                .ignoringFields("id")
                .isEqualTo(diaryEntryUpdateDTO);

    }


    @Test
    void testUpdateDiaryEntryForDateForMismatchEntityId() throws JsonProcessingException {
        LocalDate now = LocalDate.now();
        DiaryEntry diaryEntry = new DiaryEntry(null, "food1", 3.3, 2, now);
        DiaryEntry diaryEntry2 = new DiaryEntry(null, "food2", 4.5, 1, now);
        DiaryEntry diaryEntry3 = new DiaryEntry(null, "food3", 1, 1, now);
        diaryEntryRepository.saveAll(List.of(diaryEntry, diaryEntry2, diaryEntry3));

        String idPath = "/diary/api/666";
        URI urlUpdateEntryForDate = UriComponentsBuilder.newInstance()
                .scheme("http")
                .host(HOST)
                .port(port)
                .path(idPath)
                .build().toUri();

        DiaryEntryUpdateDTO diaryEntryUpdateDTO = new DiaryEntryUpdateDTO(diaryEntry2.getId(), "food 2 updated", diaryEntry2.getFoodPoints(), 2d, diaryEntry2.getDate());

        String requestBody = mapper.writeValueAsString(diaryEntryUpdateDTO);
        MultiValueMap<String, String> headers = new HttpHeaders();
        headers.set(HttpHeaders.CONTENT_TYPE, APPLICATION_JSON_VALUE);
        HttpEntity<String> httpEntity = new HttpEntity<>(requestBody, headers);

        ResponseEntity<Void> responseEntity = testRestTemplate.exchange(urlUpdateEntryForDate, HttpMethod.PUT, httpEntity, Void.class);
        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatusCode.valueOf(400));
        assertThat(responseEntity.getBody()).isNull();

        // check nothing was changed in DB
        Optional<DiaryEntry> optResponseAfterUpdateDbEntity = diaryEntryRepository.findById(diaryEntry2.getId());
        assertThat(optResponseAfterUpdateDbEntity).isPresent();
        DiaryEntry diaryEntryAfterUpdate = optResponseAfterUpdateDbEntity.get();
        assertThat(diaryEntryAfterUpdate).isEqualTo(diaryEntry2);
    }


    @Test
    void testUpdateDiaryEntryForDateForNonExistingId() throws JsonProcessingException {
        LocalDate now = LocalDate.now();
        DiaryEntry diaryEntry = new DiaryEntry(null, "food1", 3.3, 2, now);
        DiaryEntry diaryEntry2 = new DiaryEntry(null, "food2", 4.5, 1, now);
        DiaryEntry diaryEntry3 = new DiaryEntry(null, "food3", 1, 1, now);
        diaryEntryRepository.saveAll(List.of(diaryEntry, diaryEntry2, diaryEntry3));

        DiaryEntryUpdateDTO diaryEntryNonExisting = new DiaryEntryUpdateDTO(666L, "food4", 1d, 1d, now);

        String idPath = "/diary/api/666";
        URI urlUpdateEntryForDate = UriComponentsBuilder.newInstance()
                .scheme("http")
                .host(HOST)
                .port(port)
                .path(idPath)
                .build().toUri();


        String requestBody = mapper.writeValueAsString(diaryEntryNonExisting);
        MultiValueMap<String, String> headers = new HttpHeaders();
        headers.set(HttpHeaders.CONTENT_TYPE, APPLICATION_JSON_VALUE);
        HttpEntity<String> httpEntity = new HttpEntity<>(requestBody, headers);

        ResponseEntity<Void> responseEntity = testRestTemplate.exchange(urlUpdateEntryForDate, HttpMethod.PUT, httpEntity, Void.class);
        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatusCode.valueOf(404));
        assertThat(responseEntity.getBody()).isNull();
    }


    @Test
    void testCreateInvalidDiaryEntryShouldFailByValidationErrors() {
        DiaryEntryCreateDTO entry = new DiaryEntryCreateDTO(null, 1.5, 1d, null);
        URI urlCreateDiaryEntry = UriComponentsBuilder.newInstance()
                .scheme("http")
                .host(HOST)
                .port(port)
                .path("/diary/api")
                .build().toUri();

        ResponseEntity<String> response = testRestTemplate.postForEntity(urlCreateDiaryEntry, entry, String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatusCode.valueOf(400));

        assertThat(response.getBody()).contains("foodName cannot be empty");
        assertThat(response.getBody()).contains("date cannot be empty");
    }

    @Test
    void testUpdateInvalidDiaryEntryShouldFailByValidationErrors() throws JsonProcessingException {
        LocalDate now = LocalDate.now();
        DiaryEntry diaryEntry = new DiaryEntry(null, "food1", 3.3, 2, now);
        diaryEntryRepository.save(diaryEntry);

        String idPath = "/diary/api/ID";
        idPath = idPath.replace("ID", diaryEntry.getId().toString());
        URI urlUpdateEntryForDate = UriComponentsBuilder.newInstance()
                .scheme("http")
                .host(HOST)
                .port(port)
                .path(idPath)
                .build().toUri();

        DiaryEntryUpdateDTO diaryEntryUpdateDTO = new DiaryEntryUpdateDTO(diaryEntry.getId(), "food 2 updated", -3d, 0d, LocalDate.now().plusDays(2));
        String requestBody = mapper.writeValueAsString(diaryEntryUpdateDTO);
        MultiValueMap<String, String> headers = new HttpHeaders();
        headers.set(HttpHeaders.CONTENT_TYPE, APPLICATION_JSON_VALUE);
        HttpEntity<String> httpEntity = new HttpEntity<>(requestBody, headers);

        ResponseEntity<String> responseEntity = testRestTemplate.exchange(urlUpdateEntryForDate, HttpMethod.PUT, httpEntity, String.class);
        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatusCode.valueOf(400));

        assertThat(responseEntity.getStatusCode()).isEqualTo(BAD_REQUEST);
        assertThat(responseEntity.getBody()).contains("foodPoints must be positive");
        assertThat(responseEntity.getBody()).contains("amount must be positive");
        assertThat(responseEntity.getBody()).contains("date must be today or previous date");
    }
}