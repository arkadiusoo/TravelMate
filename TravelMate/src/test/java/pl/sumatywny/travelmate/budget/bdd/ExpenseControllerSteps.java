package pl.sumatywny.travelmate.budget.bdd;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.cucumber.java.Before;
import io.cucumber.java.en.*;
import io.cucumber.datatable.DataTable;
import io.cucumber.spring.CucumberContextConfiguration;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.SecurityFilterAutoConfiguration;
import org.springframework.security.test.context.support.WithMockUser;
import pl.sumatywny.travelmate.budget.controller.ExpenseController;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import pl.sumatywny.travelmate.budget.dto.ExpenseDTO;
import pl.sumatywny.travelmate.budget.model.ExpenseCategory;
import pl.sumatywny.travelmate.budget.service.ExpenseService;
import pl.sumatywny.travelmate.participant.service.TripPermissionService;
import pl.sumatywny.travelmate.security.model.User;
import pl.sumatywny.travelmate.security.service.UserService;
import pl.sumatywny.travelmate.security.service.JwtService;
import pl.sumatywny.travelmate.trip.controller.TripController;
import pl.sumatywny.travelmate.trip.model.Trip;
import pl.sumatywny.travelmate.trip.service.TripService;
import pl.sumatywny.travelmate.participant.controller.ParticipantController;
import pl.sumatywny.travelmate.participant.dto.InvitationResponseDTO;
import pl.sumatywny.travelmate.participant.dto.ParticipantDTO;
import pl.sumatywny.travelmate.participant.model.InvitationStatus;
import pl.sumatywny.travelmate.participant.model.ParticipantRole;
import pl.sumatywny.travelmate.participant.service.ParticipantService;
import pl.sumatywny.travelmate.security.service.AuthService;
import java.time.LocalDateTime;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;

@CucumberContextConfiguration
@WebMvcTest(
        controllers = {ExpenseController.class, TripController.class, ParticipantController.class},
        excludeAutoConfiguration = {
                SecurityAutoConfiguration.class,
                SecurityFilterAutoConfiguration.class
        }
)
public class ExpenseControllerSteps {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ExpenseService expenseService;
    @MockBean
    private UserService userService;
    @MockBean
    private TripService tripService;
    @MockBean
    private JwtService jwtService;
    @MockBean
    private ParticipantService participantService;
    @MockBean
    private AuthService authService;
    @MockBean
    private TripPermissionService tripPermissionService;

    private UUID tripId;
    private UUID expenseId;
    private UUID payerId;
    private UUID currentUserId;
    private ExpenseDTO sampleExpense;
    private MvcResult result;
    private ExpenseDTO requestDto;
    private Trip requestTripDto;
    private Trip sampleTrip;
    private Trip sampleTripTest;
    private UUID userId;
    private UUID participantId;
    private UUID organizerId;
    private UUID newUserId;
    private ParticipantDTO organizerParticipant;
    private ParticipantDTO pendingParticipant;
    private ParticipantDTO participantRequestDto;
    // Stub for new description variable
    private String newDescription;

    @Given("I have an existing expense with id {string}")
    public void i_have_an_existing_expense_with_id(String id) {
        // Use placeholder or actual UUID
        if (!id.startsWith("<")) {
            expenseId = UUID.fromString(id);
        }
    }

    @Given("I want to change its description to {string}")
    public void i_want_to_change_its_description_to(String description) {
        this.newDescription = description;
    }

    @Before
    public void setup() {
        tripId = UUID.randomUUID();
        expenseId = UUID.randomUUID();
        payerId = UUID.randomUUID();
        currentUserId = UUID.randomUUID();
        // zawsze zwracajmy ten sam currentUserId
        when(userService.getCurrentUserId(any())).thenReturn(currentUserId);
        tripId = UUID.fromString("987e6543-e21b-12d3-a456-426614174111");

        sampleTripTest = new Trip();
        sampleTripTest.setId(tripId);
        sampleTripTest.setName("Old Trip");
        sampleTripTest.setStartDate(LocalDate.of(2025, 1, 1));
        sampleTripTest.setEndDate(LocalDate.of(2025, 1, 5));

        participantId = UUID.randomUUID();
        organizerId = UUID.randomUUID();
        newUserId = UUID.randomUUID();
        when(authService.getCurrentUserId()).thenReturn(currentUserId);

        // Mock permission service defaults
        when(tripPermissionService.canInviteParticipants(any(), any())).thenReturn(true);
        when(tripPermissionService.hasRoleOrHigher(any(), any(), any())).thenReturn(true);
        when(tripPermissionService.canManageParticipant(any(), any(), any())).thenReturn(true);
        when(tripPermissionService.isAcceptedParticipant(any(), any())).thenReturn(true);
    }

    @Given("a current user is authenticated")
    public void a_current_user_is_authenticated() {
        // już ustawione w @Before
    }

    @Given("the following trip payload:")
    public void the_following_trip_payload(DataTable table) {
        Map<String, String> map = table.asMaps().get(0);

        Trip trip = new Trip();
        trip.setName("Weekend w Warszawie");
        trip.setTripBudget(1000.0);

        requestTripDto = trip;
    }

    @Given("a trip with id {string} exists in the database")
    public void a_trip_exists_in_the_database(String id) {
        tripId = UUID.fromString(id);
        sampleTripTest = new Trip();
        sampleTripTest.setId(tripId);
        when(tripService.findById(tripId)).thenReturn(sampleTripTest);
        when(tripService.canUserAccessTrip(eq(tripId), any(UUID.class))).thenReturn(true);
    }

    @Given("a sample expense exists for a trip")
    public void a_sample_expense_exists_for_a_trip() {
        sampleExpense = ExpenseDTO.builder()
                .id(expenseId)
                .tripId(tripId)
                .name("Lunch")
                .amount(new BigDecimal("10.00"))
                .category(ExpenseCategory.FOOD)
                .description("Business lunch")
                .date(LocalDate.of(2025, 6, 29))
                .payerId(payerId)
                .participantShares(Map.of(payerId, new BigDecimal("1.0")))
                .participantPaymentStatus(Map.of(payerId, true))
                .participantNames(List.of("Alice"))
                .build();

        when(expenseService.getExpensesByTrip(tripId))
                .thenReturn(List.of(sampleExpense));
    }

    @Given("the following expense payload:")
    public void the_following_expense_payload(DataTable table) {
        Map<String, String> map = table.asMaps().get(0);
        requestDto = new ExpenseDTO();
        requestDto.setName(map.get("name"));
        requestDto.setAmount(new BigDecimal(map.get("amount")));
        requestDto.setCategory(ExpenseCategory.valueOf(map.get("category")));
        requestDto.setDescription(map.get("description"));
        requestDto.setDate(LocalDate.parse(map.get("date")));
        String payerIdStr = map.get("payerId");
        if (payerIdStr.startsWith("<") && payerIdStr.endsWith(">")) {
            payerIdStr = payerId.toString();
        }
        requestDto.setPayerId(UUID.fromString(payerIdStr));
        requestDto.setParticipantShares(Map.of(payerId, new BigDecimal("1.0")));
        requestDto.setParticipantPaymentStatus(Map.of(payerId, true));
        requestDto.setParticipantNames(List.of("Alice"));
    }

    @When("I GET {string}")
    public void i_get(String urlTemplate) throws Exception {
        String url = urlTemplate
                .replace("{tripId}", tripId.toString())
                .replace("{expenseId}", expenseId.toString())
                .replace("{payerId}", payerId.toString())
                .replace("{participantId}", participantId.toString());
        result = mockMvc.perform(get(url))
                .andReturn();
    }

    @When("user tries to get the trip by that ID")
    public void user_tries_to_get_the_trip_by_that_id() throws Exception {
        result = mockMvc.perform(get("/api/trips/" + tripId))
                .andReturn();
    }

    @When("I POST {string} with the trip payload")
    public void i_post_trip_with_that_payload(String urlTemplate) throws Exception {
        when(tripService.create(any(), eq(currentUserId)))
                .thenReturn(sampleTrip);

        result = mockMvc.perform(post(urlTemplate)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestTripDto)))
                .andReturn();
    }

    @When("I POST {string} with that payload")
    public void i_post_with_that_payload(String urlTemplate) throws Exception {
        String url = urlTemplate.replace("{tripId}", tripId.toString());

        if (url.contains("/participants")) {
            // Handle participant invitation
            ParticipantDTO createdParticipant = ParticipantDTO.builder()
                    .id(UUID.randomUUID())
                    .tripId(tripId)
                    .userId(newUserId)
                    .email(participantRequestDto.getEmail())
                    .role(participantRequestDto.getRole())
                    .status(InvitationStatus.PENDING)
                    .createdAt(LocalDateTime.now())
                    .build();

            when(participantService.addParticipant(any(ParticipantDTO.class), eq(organizerId)))
                    .thenReturn(createdParticipant);

            result = mockMvc.perform(post(url)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(participantRequestDto)))
                    .andReturn();
        } else {
            // Handle expense (existing logic)
            when(expenseService.addExpense(any(), eq(currentUserId)))
                    .thenReturn(sampleExpense);

            result = mockMvc.perform(post(url)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(requestDto)))
                    .andReturn();
        }
    }

    @When("user updates the trip with name {string} starting on {string} and ending on {string}")
    public void user_updates_the_trip(String name, String start, String end) throws Exception {
        Trip updated = new Trip();
        updated.setId(tripId);
        updated.setName(name);
        updated.setStartDate(LocalDate.parse(start));
        updated.setEndDate(LocalDate.parse(end));

        when(tripService.update(eq(tripId), any())).thenReturn(updated);

        result = mockMvc.perform(put("/api/trips/" + tripId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updated)))
                .andReturn();
    }

    @When("user deletes the trip")
    public void user_deletes_the_trip() throws Exception {
        doNothing().when(tripService).delete(tripId);
        result = mockMvc.perform(delete("/api/trips/" + tripId))
                .andReturn();
    }

    @When("I PATCH {string} with:")
    public void i_patch_with(String urlTemplate, io.cucumber.datatable.DataTable table) throws Exception {
        String url = urlTemplate
                .replace("{tripId}", tripId.toString())
                .replace("{expenseId}", expenseId.toString())
                .replace("{participantId}", participantId.toString());

        if (url.contains("/participants/") && url.contains("/respond")) {
            // Handle participant invitation response
            Map<String, String> updates = table.asMaps().get(0);
            InvitationResponseDTO response = new InvitationResponseDTO();
            response.setStatus(InvitationStatus.valueOf(updates.get("status")));

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
                    .build();

            when(participantService.respondToInvitation(eq(participantId),
                    eq(InvitationStatus.ACCEPTED), eq(currentUserId)))
                    .thenReturn(acceptedParticipant);

            result = mockMvc.perform(patch(url)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(response)))
                    .andReturn();
        } else {
            // Handle expense patch (existing logic)
            Map<String, String> stringUpdates = table.asMaps().get(0);
            Map<String, Object> updates = new HashMap<>();
            stringUpdates.forEach((k, v) -> updates.put(k, v));
            when(expenseService.patchExpense(eq(expenseId), eq(updates), eq(currentUserId)))
                    .thenReturn(sampleExpense.toBuilder()
                            .description((String) updates.get("description"))
                            .build());

            result = mockMvc.perform(patch(url)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(updates)))
                    .andReturn();
        }
    }

    @Then("the HTTP status should be {int}")
    public void the_http_status_should_be(Integer expectedStatus) {
        assertEquals(expectedStatus.intValue(), result.getResponse().getStatus());
    }

    @Then("the trip should be returned")
    public void the_trip_should_be_returned() throws Exception {
        assertEquals(200, result.getResponse().getStatus());
        Trip returned = objectMapper.readValue(result.getResponse().getContentAsString(), Trip.class);
        assertEquals(sampleTripTest.getId(), returned.getId());
    }

    @Then("the trip should be updated")
    public void the_trip_should_be_updated() throws Exception {
        assertEquals(200, result.getResponse().getStatus());
        Trip returned = objectMapper.readValue(result.getResponse().getContentAsString(), Trip.class);
        assertEquals("Winter Trip", returned.getName());
    }

    @Then("the JSON array should contain an expense with name {string}")
    public void the_json_array_should_contain_an_expense_with_name(String expectedName) throws Exception {
        String json = result.getResponse().getContentAsString();
        List<ExpenseDTO> list = objectMapper.readValue(json, new TypeReference<>() {});
        assertTrue(list.stream().anyMatch(e -> expectedName.equals(e.getName())));
    }

    @Then("the JSON object should have a field {string} equal to {string}")
    @Then("the JSON object should have {string} equal to {string}")
    public void the_json_object_should_have_field_equal(String field, String expected) throws Exception {
        String actual = objectMapper.readTree(result.getResponse().getContentAsString())
                .get(field).asText();
        assertEquals(expected, actual);
    }

    @Then("the trip should be removed")
    public void the_trip_should_be_removed() {
        assertEquals(200, result.getResponse().getStatus());
        verify(tripService).delete(tripId);
    }

    // PARTICIPANT STEP DEFINITIONS
    @Given("a current user is authenticated as an organizer")
    public void a_current_user_is_authenticated_as_an_organizer() {
        currentUserId = organizerId;
        when(authService.getCurrentUserId()).thenReturn(organizerId);

        // Mock permission service methods for organizer
        when(tripPermissionService.canInviteParticipants(tripId, organizerId)).thenReturn(true);
        when(tripPermissionService.hasRoleOrHigher(tripId, organizerId, ParticipantRole.ORGANIZER)).thenReturn(true);
        when(tripPermissionService.canManageParticipant(eq(tripId), eq(organizerId), any())).thenReturn(true);
        when(tripPermissionService.isAcceptedParticipant(tripId, organizerId)).thenReturn(true);

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

        when(participantService.getParticipantsByTrip(tripId))
                .thenReturn(List.of(organizerParticipant));
    }

    @Given("a sample trip exists with participants")
    public void a_sample_trip_exists_with_participants() {
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

        when(participantService.getParticipantsByTrip(tripId))
                .thenReturn(List.of(organizerParticipant, pendingParticipant));
    }

    @Given("the following participant invitation payload:")
    public void the_following_participant_invitation_payload(DataTable table) {
        Map<String, String> map = table.asMaps().get(0);
        participantRequestDto = new ParticipantDTO();
        participantRequestDto.setEmail(map.get("email"));
        participantRequestDto.setRole(ParticipantRole.valueOf(map.get("role")));
        participantRequestDto.setTripId(tripId);

        // Mock user service for email lookup
        when(userService.findUserIdByEmail("newuser@example.com")).thenReturn(newUserId);
        when(userService.findEmailByUserId(newUserId)).thenReturn("newuser@example.com");
        when(userService.isRegisteredUser("newuser@example.com")).thenReturn(true);
    }

    @Given("I have a pending invitation as participant")
    public void i_have_a_pending_invitation_as_participant() {
        currentUserId = newUserId;
        when(authService.getCurrentUserId()).thenReturn(currentUserId);

        // Create pendingParticipant if it doesn't exist
        if (pendingParticipant == null) {
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
        }
    }

    @Then("the JSON array should contain a participant with email {string}")
    public void the_json_array_should_contain_a_participant_with_email(String expectedEmail) throws Exception {
        String json = result.getResponse().getContentAsString();
        List<ParticipantDTO> list = objectMapper.readValue(json, new TypeReference<>() {});
        assertTrue(list.stream().anyMatch(p -> expectedEmail.equals(p.getEmail())));
    }

    @Then("the JSON object should have a field {string} that is not null")
    public void the_json_object_should_have_field_that_is_not_null(String field) throws Exception {
        assertFalse(objectMapper.readTree(result.getResponse().getContentAsString())
                .get(field).isNull());
    }
}