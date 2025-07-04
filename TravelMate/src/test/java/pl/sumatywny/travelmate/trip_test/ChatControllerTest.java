package pl.sumatywny.travelmate.trip_test;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import pl.sumatywny.travelmate.reports.dto.NoteDTO;
import pl.sumatywny.travelmate.reports.service.ReportService;
import pl.sumatywny.travelmate.security.config.JwtAuthenticationFilter;
import pl.sumatywny.travelmate.security.model.User;
import pl.sumatywny.travelmate.security.service.JwtService;
import pl.sumatywny.travelmate.security.service.UserService;
import pl.sumatywny.travelmate.trip.controller.ChatController;
import pl.sumatywny.travelmate.trip.dto.ChatNoteRequestDto;
import pl.sumatywny.travelmate.trip.dto.ChatRequestDto;
import pl.sumatywny.travelmate.trip.dto.PlaceVisitDto;
import pl.sumatywny.travelmate.trip.model.Point;
import pl.sumatywny.travelmate.trip.model.Trip;
import pl.sumatywny.travelmate.trip.service.OpenAiService;
import pl.sumatywny.travelmate.trip.service.PointService;
import pl.sumatywny.travelmate.trip.service.TripService;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;

@ExtendWith(SpringExtension.class)
@WebMvcTest(ChatController.class)
class ChatControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean private OpenAiService openAiService;
    @MockBean private PointService pointService;
    @MockBean private ReportService reportService;
    @MockBean private TripService tripService;

    @MockBean private JwtService jwtService;
    @MockBean private JwtAuthenticationFilter jwtAuthenticationFilter;
    @MockBean private UserService userService;

    @Autowired
    private ObjectMapper objectMapper;

    private static final UUID TRIP_ID = UUID.randomUUID();
    private static final UUID USER_ID = UUID.randomUUID();
    private static final String USER_EMAIL = "user@example.com";

    @BeforeEach
    void setUpFilter() throws Exception {
        doAnswer(invocation -> {
            ServletRequest req = invocation.getArgument(0);
            ServletResponse res = invocation.getArgument(1);
            FilterChain chain = invocation.getArgument(2);
            chain.doFilter(req, res);
            return null;
        }).when(jwtAuthenticationFilter).doFilter(any(), any(), any());
    }

    private User mockUser() {
        User user = new User();
        user.setId(USER_ID);
        user.setEmail(USER_EMAIL);
        return user;
    }

    @Test
    @WithMockUser(username = USER_EMAIL)
    void shouldReturnRecommendedPlacesAndSavePoints() throws Exception {
        ChatRequestDto request = new ChatRequestDto();
        request.setPrompt("Co warto zobaczyć w Gdańsku?");
        request.setTripId(TRIP_ID);

        PlaceVisitDto place = new PlaceVisitDto(
                "Stare Miasto",
                "ul. Długa, Gdańsk",
                54.3520,
                18.6466,
                "2025-08-12"
        );

        when(userService.findByEmail(USER_EMAIL)).thenReturn(Optional.of(mockUser()));
        when(tripService.canUserAccessTrip(TRIP_ID, USER_ID)).thenReturn(true);
        when(openAiService.askChatGpt("Co warto zobaczyć w Gdańsku?")).thenReturn(List.of(place));

        mockMvc.perform(post("/api/chat").with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$[0].name").value("Stare Miasto"))
                .andExpect(jsonPath("$[0].address").value("ul. Długa, Gdańsk"))
                .andExpect(jsonPath("$[0].lat").value(54.3520))
                .andExpect(jsonPath("$[0].lng").value(18.6466))
                .andExpect(jsonPath("$[0].date").value("2025-08-12"));

        verify(pointService, times(1)).create(eq(TRIP_ID), any(Point.class));
    }

    @Test
    @WithMockUser(username = USER_EMAIL)
    void shouldGeneratePdfFromNotes() throws Exception {
        ChatNoteRequestDto request = new ChatNoteRequestDto(TRIP_ID);

        NoteDTO note = NoteDTO.builder()
                .id(UUID.randomUUID())
                .date(LocalDateTime.of(2025, 5, 10, 12, 0))
                .pointName("Wawel")
                .pointId(1L)
                .content("Piękna architektura")
                .author(USER_EMAIL)
                .build();

        Point point = Point.builder()
                .id(1L)
                .title("Wawel")
                .date(LocalDate.of(2025, 5, 10))
                .build();

        Trip trip = Trip.builder()
                .id(TRIP_ID)
                .name("Kraków trip")
                .build();

        when(userService.findByEmail(USER_EMAIL)).thenReturn(Optional.of(mockUser()));
        when(tripService.canUserAccessTrip(TRIP_ID, USER_ID)).thenReturn(true);
        when(reportService.getTripNotes(TRIP_ID)).thenReturn(List.of(note));
        when(pointService.findByTripId(TRIP_ID)).thenReturn(List.of(point));
        when(tripService.findById(TRIP_ID)).thenReturn(trip);
        when(openAiService.askChatGptNote(anyString())).thenReturn("Odwiedziliśmy Wawel...");

        mockMvc.perform(post("/api/chat/note").with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Type", "application/pdf"));
    }
}
