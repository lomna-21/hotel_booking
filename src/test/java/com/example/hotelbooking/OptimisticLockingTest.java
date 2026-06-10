package com.example.hotelbooking;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class OptimisticLockingTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    public void testOptimisticLocking() throws InterruptedException {

        String token1 = "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJjdXN0b21lciIsImlhdCI6MTc4MDY3MTkwOSwiZXhwIjoxNzgwNzU4MzA5fQ._HAdu58W0cokaYGUA9PWCOXst6NWUeS82nakqmCGnOk";
        String token2 = "Bearer eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJuZXdjdXN0b21lciIsImlhdCI6MTc4MDY3MTk2MSwiZXhwIjoxNzgwNzU4MzYxfQ.qTcF0srfUZ9NSaaVCO8aRQELNqNdFh9HZaJM8CY31mY";

        String requestBody = "{"
                + "\"hotelPublicId\": \"HTL-BE54A15C\","
                + "\"roomType\": \"DOUBLE\","
                + "\"checkIn\": \"2026-07-01\","
                + "\"checkOut\": \"2026-07-03\""
                + "}";

        // two threads simulating two users
        Thread thread1 = new Thread(() -> {
            try {
                mockMvc.perform(MockMvcRequestBuilders
                                .post("/api/bookings")
                                .header("Authorization", token1)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(requestBody))
                        .andExpect(status().isOk());
                System.out.println("Thread 1 — booking successful");
            } catch (Exception e) {
                System.out.println("Thread 1 — booking failed: " + e.getMessage());
            }
        });

        Thread thread2 = new Thread(() -> {
            try {
                mockMvc.perform(MockMvcRequestBuilders
                                .post("/api/hotels/book-room")
                                .header("Authorization", token2)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(requestBody))
                        .andExpect(status().isOk());
                System.out.println("Thread 2 — booking successful");
            } catch (Exception e) {
                System.out.println("Thread 2 — booking failed: " + e.getMessage());
            }
        });

        // start both at the same time
        thread1.start();
        thread2.start();

        // wait for both to finish
        thread1.join();
        thread2.join();
    }
}