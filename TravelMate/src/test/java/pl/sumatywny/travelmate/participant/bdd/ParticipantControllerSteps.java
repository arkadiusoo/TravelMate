package pl.sumatywny.travelmate.participant.bdd;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.cucumber.java.Before;
import io.cucumber.java.en.*;
import io.cucumber.datatable.DataTable;
import io.cucumber.spring.CucumberContextConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.SecurityFilterAutoConfiguration;
import pl.sumatywny.travelmate.participant.controller.ParticipantController;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import pl.sumatywny.travelmate.participant.dto.InvitationResponseDTO;
import pl.sumatywny.travelmate.participant.dto.ParticipantDTO;
import pl.sumatywny.travelmate.participant.model.InvitationStatus;
import pl.sumatywny.travelmate.participant.model.ParticipantRole;
import pl.sumatywny.travelmate.participant.service.ParticipantService;
import pl.sumatywny.travelmate.security.service.UserService;
import pl.sumatywny.travelmate.security.service.JwtService;
import pl.sumatywny.travelmate.security.service.AuthService;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.HashMap;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;

@CucumberContextConfiguration
@WebMvcTest(
        controllers = ParticipantController.class,
        excludeAutoConfiguration = {
                SecurityAutoConfiguration.class,
                SecurityFilterAutoConfiguration.class
        }
)
public class ParticipantControllerSteps {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ParticipantService participantService;
    @MockBean
    private UserService userService;
    @MockBean
    private JwtService jwtService;
    @MockBean
    private AuthService authService;

    private UUID tripId;
    private UUID participantId;
    private UUID organizerId;
    private UUID newUserId;
    private UUID currentUserId;
    private ParticipantDTO organizerParticipant;
    private ParticipantDTO pendingParticipant;
    private MvcResult result;
    private ParticipantDTO requestDto;

    @Before
    public void setup() {
        tripId = UUID.randomUUID();
        participantId = UUID.randomUUID();
        organizerId = UUID.randomUUID();
        newUserId = UUID.randomUUID();
        currentUserId = organizerId; // Default to organizer as current user

        // Mock AuthService to return current user ID
        when(authService.getCurrentUserId()).thenReturn(currentUserId);
    }

    @Given("a current user is authenticated as an organizer")
    public void a_current_user_is_authenticated_as_an_organizer() {
        // Set up organizer participant
        organizerParticipant = ParticipantDTO.builder()
                .id(UUID.randomUUID())
                .tripId(tripId)
                .userId(organizerId)
                .email("organizer@example.com")
                .firstName("John")
                .lastName("Organizer")
                .role(ParticipantRole.ORGANIZER)
                .status(InvitationStatus.ACCEPTED)
                .createdAt(LocalDateTime.now().minusDays(1))
                .joinedAt(LocalDateTime.now().minusDays(1))
                .build();
    }

    @Given("a sample trip exists with participants")
    public void a_sample_trip_exists_with_participants() {
        // Create a pending participant for invitation scenarios
        pendingParticipant = ParticipantDTO.builder()
                .id(participantId)
                .tripId(tripId)
                .userId(newUserId)
                .email("newuser@example.com")
                .firstName("Jane")
                .lastName("Member")
                .role(ParticipantRole.MEMBER)
                .status(InvitationStatus.PENDING)
                .createdAt(LocalDateTime.now())
                .build();

        // Mock the service to return existing participants
        when(participantService.getParticipantsByTrip(tripId))
                .thenReturn(List.of(organizerParticipant, pendingParticipant));
    }

    @Given("the following participant invitation payload:")
    public void the_following_participant_invitation_payload(DataTable table) {
        Map<String, String> map = table.asMaps().get(0);
        requestDto = new ParticipantDTO();
        requestDto.setEmail(map.get("email"));
        requestDto.setRole(ParticipantRole.valueOf(map.get("role")));
        requestDto.setTripId(tripId);
    }

    @Given("I have a pending invitation as participant")
    public void i_have_a_pending_invitation_as_participant() {
        // Switch current user to the pending participant
        currentUserId = newUserId;
        when(authService.getCurrentUserId()).thenReturn(currentUserId);
    }

    @When("I GET {string}")
    public void i_get(String urlTemplate) throws Exception {
        String url = urlTemplate
                .replace("{tripId}", tripId.toString())
                .replace("{participantId}", participantId.toString());

        result = mockMvc.perform(get(url))
                .andReturn();
    }

    @When("I POST {string} with that payload")
    public void i_post_with_that_payload(String urlTemplate) throws Exception {
        // Mock successful participant invitation
        ParticipantDTO createdParticipant = ParticipantDTO.builder()
                .id(UUID.randomUUID())
                .tripId(tripId)
                .userId(newUserId)
                .email(requestDto.getEmail())
                .role(requestDto.getRole())
                .status(InvitationStatus.PENDING)
                .createdAt(LocalDateTime.now())
                .build();

        when(participantService.addParticipant(any(ParticipantDTO.class), eq(organizerId)))
                .thenReturn(createdParticipant);

        String url = urlTemplate.replace("{tripId}", tripId.toString());
        result = mockMvc.perform(post(url)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andReturn();
    }

    @When("I PATCH {string} with:")
    public void i_patch_with(String urlTemplate, DataTable table) throws Exception {
        Map<String, String> updates = table.asMaps().get(0);
        InvitationResponseDTO response = new InvitationResponseDTO();
        response.setStatus(InvitationStatus.valueOf(updates.get("status")));

        // Mock successful invitation response
        ParticipantDTO acceptedParticipant = ParticipantDTO.builder()
                .id(pendingParticipant.getId())
                .tripId(pendingParticipant.getTripId())
                .userId(pendingParticipant.getUserId())
                .email(pendingParticipant.getEmail())
                .firstName(pendingParticipant.getFirstName())
                .lastName(pendingParticipant.getLastName())
                .role(pendingParticipant.getRole())
                .status(InvitationStatus.ACCEPTED)
                .createdAt(pendingParticipant.getCreatedAt())
                .joinedAt(LocalDateTime.now())
                .updatedAt(pendingParticipant.getUpdatedAt())
                .build();

        when(participantService.respondToInvitation(eq(participantId),
                eq(InvitationStatus.ACCEPTED), eq(currentUserId)))
                .thenReturn(acceptedParticipant);

        String url = urlTemplate
                .replace("{tripId}", tripId.toString())
                .replace("{participantId}", participantId.toString());

        result = mockMvc.perform(patch(url)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(response)))
                .andReturn();
    }

    @Then("the HTTP status should be {int}")
    public void the_http_status_should_be(Integer expectedStatus) {
        assertEquals(expectedStatus.intValue(), result.getResponse().getStatus());
    }

    @Then("the JSON array should contain a participant with email {string}")
    public void the_json_array_should_contain_a_participant_with_email(String expectedEmail) throws Exception {
        String json = result.getResponse().getContentAsString();
        List<ParticipantDTO> list = objectMapper.readValue(json, new TypeReference<>() {});
        assertTrue(list.stream().anyMatch(p -> expectedEmail.equals(p.getEmail())));
    }

    @Then("the JSON object should have a field {string} equal to {string}")
    @Then("the JSON object should have {string} equal to {string}")
    public void the_json_object_should_have_field_equal(String field, String expected) throws Exception {
        String actual = objectMapper.readTree(result.getResponse().getContentAsString())
                .get(field).asText();
        assertEquals(expected, actual);
    }

    @Then("the JSON object should have a field {string} that is not null")
    public void the_json_object_should_have_field_that_is_not_null(String field) throws Exception {
        assertFalse(objectMapper.readTree(result.getResponse().getContentAsString())
                .get(field).isNull());
    }
}