//package pl.sumatywny.travelmate.trip_test;
//
//import com.fasterxml.jackson.databind.ObjectMapper;
//import org.junit.jupiter.api.Test;
//import org.mockito.Mockito;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
//import org.springframework.boot.test.mock.mockito.MockBean;
//import org.springframework.http.MediaType;
//import org.springframework.test.web.servlet.MockMvc;
//import pl.sumatywny.travelmate.trip.controller.ChatController;
//import pl.sumatywny.travelmate.trip.dto.ChatRequestDto;
//import pl.sumatywny.travelmate.trip.dto.PlaceVisitDto;
//import pl.sumatywny.travelmate.trip.service.OpenAiService;
//
//import java.util.List;
//
//import static org.mockito.ArgumentMatchers.anyString;
//import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
//import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
//
//@WebMvcTest(ChatController.class)
//class ChatControllerTest {
//
//    @Autowired
//    private MockMvc mockMvc;
//
//    @MockBean
//    private OpenAiService openAiService;
//
//    @Autowired
//    private ObjectMapper objectMapper;
//
//    @Test
//    void shouldReturnListOfPlacesFromOpenAi() throws Exception {
//        // Arrange
//        ChatRequestDto request = new ChatRequestDto();
//        request.setPrompt("Co warto zobaczyć w Krakowie?");
//
//        PlaceVisitDto place1 = new PlaceVisitDto("Wawel", "Adres", 51.2,32.1,"2025-07-01");
//        PlaceVisitDto place2 = new PlaceVisitDto("Rynek Główny", "Centrum Krakowa",51.0,23, "2025-07-02");
//
//        List<PlaceVisitDto> responseList = List.of(place1, place2);
//
//        Mockito.when(openAiService.askChatGpt(anyString())).thenReturn(responseList);
//
//        mockMvc.perform(post("/api/chat")
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content(objectMapper.writeValueAsString(request)))
//                .andExpect(status().isOk())
//                .andExpect(jsonPath("$.length()").value(2))
//                .andExpect(jsonPath("$[0].name").value("Wawel"))
//                .andExpect(jsonPath("$[1].name").value("Rynek Główny"));
//    }
//}

