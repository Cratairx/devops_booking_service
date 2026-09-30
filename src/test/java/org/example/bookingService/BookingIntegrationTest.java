package org.example.bookingService;

import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.example.bookingService.ENUMS.RoomType;
import org.example.bookingService.Models.Room;
import org.example.bookingService.Repositories.BookingRepository;
import org.example.bookingService.Repositories.RoomRepository;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.resttestclient.TestRestTemplate;
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureTestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.mysql.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;

@Testcontainers
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureTestRestTemplate
class BookingIntegrationTest {
//skit kommentar
    @Container
    static final MySQLContainer MYSQL = new MySQLContainer("mysql:8.0")
            .withDatabaseName("bookingdb")
            .withUsername("root")
            .withPassword("root");

    static MockWebServer customerServiceMock;

    @DynamicPropertySource
    static void overrideProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", MYSQL::getJdbcUrl);
        registry.add("spring.datasource.username", MYSQL::getUsername);
        registry.add("spring.datasource.password", MYSQL::getPassword);
        registry.add("customer-service.base-url", () -> customerServiceMock.url("/").toString());
    }

    @BeforeAll
    static void startMockCustomerService() throws IOException {
        customerServiceMock = new MockWebServer();
        customerServiceMock.start();
    }

    @AfterAll
    static void stopMockCustomerService() throws IOException {
        customerServiceMock.shutdown();
    }

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private RoomRepository roomRepository;

    @Autowired
    private BookingRepository bookingRepository;

    private Room room;

    @BeforeEach
    void setUp() {
        bookingRepository.deleteAll();
        roomRepository.deleteAll();
        room = roomRepository.save(new Room(null, "101", RoomType.SINGLE));
    }

    @AfterEach
    void cleanUp() {
        bookingRepository.deleteAll();
        roomRepository.deleteAll();
    }

    @Test
    void createBookingPersistsToDatabaseWhenCustomerExistsAndRoomIsFree() {
        customerServiceMock.enqueue(new MockResponse().setResponseCode(200));

        ResponseEntity<Void> response = restTemplate.postForEntity(
                "/api/bookings?customerId=1&roomId=" + room.getId()
                        + "&startDate=2026-09-10&endDate=2026-09-12",
                null, Void.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(bookingRepository.findAll()).hasSize(1);
        assertThat(bookingRepository.findAll().get(0).getRoom().getId()).isEqualTo(room.getId());
    }

    @Test
    void createBookingReturnsConflictWhenCustomerServiceReports404() {
        customerServiceMock.enqueue(new MockResponse().setResponseCode(404));

        ResponseEntity<String> response = restTemplate.postForEntity(
                "/api/bookings?customerId=999&roomId=" + room.getId()
                        + "&startDate=2026-09-10&endDate=2026-09-12",
                null, String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(bookingRepository.findAll()).isEmpty();
    }

    @Test
    void createBookingReturnsConflictWhenRoomAlreadyBookedForOverlappingDates() {
        customerServiceMock.enqueue(new MockResponse().setResponseCode(200));
        restTemplate.postForEntity(
                "/api/bookings?customerId=1&roomId=" + room.getId()
                        + "&startDate=2026-09-10&endDate=2026-09-15",
                null, Void.class);

        customerServiceMock.enqueue(new MockResponse().setResponseCode(200));
        ResponseEntity<String> response = restTemplate.postForEntity(
                "/api/bookings?customerId=2&roomId=" + room.getId()
                        + "&startDate=2026-09-12&endDate=2026-09-18",
                null, String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(bookingRepository.findAll()).hasSize(1);
    }

    @Test
    void availableRoomsExcludesBookedRoom() {
        customerServiceMock.enqueue(new MockResponse().setResponseCode(200));
        restTemplate.postForEntity(
                "/api/bookings?customerId=1&roomId=" + room.getId()
                        + "&startDate=2026-09-10&endDate=2026-09-15",
                null, Void.class);

        ResponseEntity<Room[]> response = restTemplate.getForEntity(
                "/api/bookings/available?startDate=2026-09-12&endDate=2026-09-14", Room[].class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEmpty();
    }
}