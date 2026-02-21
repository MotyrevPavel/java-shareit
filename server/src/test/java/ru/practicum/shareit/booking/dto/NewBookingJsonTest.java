package ru.practicum.shareit.booking.dto;

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
class NewBookingJsonTest {
    private final JacksonTester<NewBooking> json;

    @Test
    void testNewBookingFullSerialization() throws Exception {
        LocalDateTime startTime = LocalDateTime.of(2023, 12, 1, 10, 30, 0);
        LocalDateTime endTime = LocalDateTime.of(2023, 12, 5, 18, 45, 0);

        NewBooking newBooking = NewBooking.builder()
                .itemId(100L)
                .start(startTime)
                .end(endTime)
                .build();

        JsonContent<NewBooking> result = json.write(newBooking);

        assertThat(result).extractingJsonPathNumberValue("$.itemId").isEqualTo(100);
        assertThat(result)
                .extractingJsonPathStringValue("$.start")
                .isEqualTo("2023-12-01T10:30:00");
        assertThat(result)
                .extractingJsonPathStringValue("$.end")
                .isEqualTo("2023-12-05T18:45:00");

        assertThat(result).isStrictlyEqualToJson(
                "{\"itemId\":100,\"start\":\"2023-12-01T10:30:00\",\"end\":\"2023-12-05T18:45:00\"}"
        );
    }

    @Test
    void testNewBookingDeserializationFromJson() throws Exception {
        String jsonString = "{\"itemId\": 200, \"start\": \"2023-11-15T14:20:30\", \"end\": \"2023-11-20T16:15:45\"}";

        NewBooking result = json.parseObject(jsonString);

        assertThat(result.getItemId()).isEqualTo(200L);
        assertThat(result.getStart()).isEqualTo(LocalDateTime.of(2023, 11, 15, 14, 20, 30));
        assertThat(result.getEnd()).isEqualTo(LocalDateTime.of(2023, 11, 20, 16, 15, 45));
    }
}