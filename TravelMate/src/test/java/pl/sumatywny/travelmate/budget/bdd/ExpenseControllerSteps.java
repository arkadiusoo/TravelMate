package pl.sumatywny.travelmate.budget.bdd;

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
import pl.sumatywny.travelmate.budget.controller.ExpenseController;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import pl.sumatywny.travelmate.budget.dto.ExpenseDTO;
import pl.sumatywny.travelmate.budget.model.ExpenseCategory;
import pl.sumatywny.travelmate.budget.service.ExpenseService;
import pl.sumatywny.travelmate.security.service.UserService;
import pl.sumatywny.travelmate.security.service.JwtService;

import java.math.BigDecimal;
import java.time.LocalDate;
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
    controllers = ExpenseController.class,
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
    private JwtService jwtService;

    private UUID tripId;
    private UUID expenseId;
    private UUID payerId;
    private UUID currentUserId;
    private ExpenseDTO sampleExpense;
    private MvcResult result;
    private ExpenseDTO requestDto;
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
    }

    @Given("a current user is authenticated")
    public void a_current_user_is_authenticated() {
        // już ustawione w @Before
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
            .replace("{payerId}", payerId.toString());
        result = mockMvc.perform(get(url))
                        .andReturn();
    }

    @When("I POST {string} with that payload")
    public void i_post_with_that_payload(String urlTemplate) throws Exception {
        when(expenseService.addExpense(any(), eq(currentUserId)))
            .thenReturn(sampleExpense);

        String url = urlTemplate.replace("{tripId}", tripId.toString());
        result = mockMvc.perform(post(url)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestDto)))
            .andReturn();
    }

    @When("I PATCH {string} with:")
    public void i_patch_with(String urlTemplate, io.cucumber.datatable.DataTable table) throws Exception {
        Map<String, String> stringUpdates = table.asMaps().get(0);
        Map<String, Object> updates = new HashMap<>();
        stringUpdates.forEach((k, v) -> updates.put(k, v));
        when(expenseService.patchExpense(eq(expenseId), eq(updates), eq(currentUserId)))
            .thenReturn(sampleExpense.toBuilder()
                .description((String) updates.get("description"))
                .build());

        String url = urlTemplate
            .replace("{tripId}", tripId.toString())
            .replace("{expenseId}", expenseId.toString());
        result = mockMvc.perform(patch(url)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updates)))
            .andReturn();
    }

    @Then("the HTTP status should be {int}")
    public void the_http_status_should_be(Integer expectedStatus) {
        assertEquals(expectedStatus.intValue(), result.getResponse().getStatus());
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
}