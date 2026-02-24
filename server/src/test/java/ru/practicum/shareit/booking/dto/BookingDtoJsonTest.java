package ru.practicum.shareit.booking.dto;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.boot.test.json.JsonContent;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.user.dto.UserDto;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@JsonTest
@RequiredArgsConstructor(onConstructor_ = @Autowired)
class BookingDtoJsonTest {
    private final JacksonTester<BookingDto> json;

    @Test
    void testFullBookingDtoSerialization() throws Exception {
        LocalDateTime startTime = LocalDateTime.of(2023, 10, 15, 10, 0, 0);
        LocalDateTime endTime = LocalDateTime.of(2023, 10, 20, 18, 0, 0);

        UserDto booker = UserDto.builder()
                .id(1L)
                .name("user")
                .email("user@mail.ru")
                .build();

        ItemDto item = ItemDto.builder()
                .id(100L)
                .name("item")
                .description("good item")
                .available(true)
                .userId(2L)
                .build();

        BookingDto bookingDto = BookingDto.builder()
                .id(1000L)
                .start(startTime)
                .end(endTime)
                .status("APPROVED")
                .booker(booker)
                .item(item)
                .build();

        JsonContent<BookingDto> result = json.write(bookingDto);

        assertThat(result).extractingJsonPathNumberValue("$.id").isEqualTo(1000);
        assertThat(result)
                .extractingJsonPathStringValue("$.start")
                .isEqualTo("2023-10-15T10:00:00");
        assertThat(result)
                .extractingJsonPathStringValue("$.end")
                .isEqualTo("2023-10-20T18:00:00");
        assertThat(result)
                .extractingJsonPathStringValue("$.status")
                .isEqualTo("APPROVED");
        assertThat(result)
                .extractingJsonPathNumberValue("$.booker.id")
                .isEqualTo(1);
        assertThat(result)
                .extractingJsonPathStringValue("$.booker.name")
                .isEqualTo("user");
        assertThat(result)
                .extractingJsonPathStringValue("$.booker.email")
                .isEqualTo("user@mail.ru");
        assertThat(result)
                .extractingJsonPathNumberValue("$.item.id")
                .isEqualTo(100);
        assertThat(result)
                .extractingJsonPathStringValue("$.item.name")
                .isEqualTo("item");
        assertThat(result)
                .extractingJsonPathStringValue("$.item.description")
                .isEqualTo("good item");
        assertThat(result)
                .extractingJsonPathBooleanValue("$.item.available")
                .isTrue();
        assertThat(result)
                .extractingJsonPathNumberValue("$.item.userId")
                .isEqualTo(2);

        assertThat(result).isStrictlyEqualToJson("{\"id\": 1000, \"start\": \"2023-10-15T10:00:00\", " +
                "\"end\": \"2023-10-20T18:00:00\", \"status\": \"APPROVED\", \"booker\": {\"id\": 1, " +
                "\"name\": \"user\", \"email\": \"user@mail.ru\"}, \"item\": {\"id\": 100, \"name\": \"item\", " +
                "\"description\": \"good item\", \"available\": true, \"userId\": 2}}");
    }

    @Test
    void testBookingDtoDeserializationFromJson() throws Exception {
        String jsonString = "{\"id\": 2000, \"start\": \"2023-11-01T09:00:00\", " +
                "\"end\": \"2023-11-10T17:00:00\", \"status\": \"WAITING\", \"booker\": {\"id\": 3, " +
                "\"name\": \"user\", \"email\": \"user@mail.ru\"}, \"item\": {\"id\": 200, \"name\": \"item\", " +
                "\"description\": \"good item\", \"available\": true, \"userId\": 4}}";

        BookingDto result = json.parseObject(jsonString);

        assertThat(result.getId()).isEqualTo(2000L);
        assertThat(result.getStart()).isEqualTo(LocalDateTime.of(2023, 11, 1, 9, 0, 0));
        assertThat(result.getEnd()).isEqualTo(LocalDateTime.of(2023, 11, 10, 17, 0, 0));
        assertThat(result.getStatus()).isEqualTo("WAITING");

        // Проверка вложенного объекта booker
        assertThat(result.getBooker().getId()).isEqualTo(3L);
        assertThat(result.getBooker().getName()).isEqualTo("user");
        assertThat(result.getBooker().getEmail()).isEqualTo("user@mail.ru");

        // Проверка вложенного объекта item
        assertThat(result.getItem().getId()).isEqualTo(200L);
        assertThat(result.getItem().getName()).isEqualTo("item");
        assertThat(result.getItem().getDescription()).isEqualTo("good item");
        assertThat(result.getItem().getAvailable()).isTrue();
        assertThat(result.getItem().getUserId()).isEqualTo(4L);
    }
}