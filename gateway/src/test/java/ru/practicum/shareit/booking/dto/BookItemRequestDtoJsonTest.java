package ru.practicum.shareit.booking.dto;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.boot.test.json.JsonContent;

import java.time.LocalDateTime;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

@JsonTest
@RequiredArgsConstructor(onConstructor_ = @Autowired)
class BookItemRequestDtoJsonTest {
    private final JacksonTester<BookItemRequestDto> json;
    private final ObjectMapper mapper;

    @Test
    void testBookItemRequestDtoSerialization() throws Exception {
        LocalDateTime startTime = LocalDateTime.of(2023, 10, 15, 10, 0, 0);
        LocalDateTime endTime = LocalDateTime.of(2023, 10, 20, 18, 0, 0);

        BookItemRequestDto dto = new BookItemRequestDto(1L, startTime, endTime);

        JsonContent<BookItemRequestDto> result = json.write(dto);

        assertThat(result).extractingJsonPathNumberValue("$.itemId").isEqualTo(1);
        assertThat(result)
                .extractingJsonPathStringValue("$.start")
                .isEqualTo("2023-10-15T10:00:00");
        assertThat(result)
                .extractingJsonPathStringValue("$.end")
                .isEqualTo("2023-10-20T18:00:00");

        // Дополнительная проверка полного JSON-вывода (опционально)
        assertThat(result).isStrictlyEqualToJson(mapper.writeValueAsString(dto));
    }

    @Test
    void testBookItemRequestDtoDeserialization() throws Exception {
        String jsonString = "{\"itemId\": 2, \"start\": \"2023-12-01T09:00:00\", \"end\": \"2023-12-10T17:00:00\"}";

        BookItemRequestDto result = json.parseObject(jsonString);

        assertThat(result.getItemId()).isEqualTo(2L);
        assertThat(result.getStart())
                .isEqualTo(LocalDateTime.of(2023, 12, 1, 9, 0, 0));
        assertThat(result.getEnd())
                .isEqualTo(LocalDateTime.of(2023, 12, 10, 17, 0, 0));
    }
}