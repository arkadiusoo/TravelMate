package pl.sumatywny.travelmate;

import io.github.cdimascio.dotenv.Dotenv;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class TravelMateApplicationTests {

    @BeforeAll
    static void initEnv() {
        // Załaduj zmienne środowiskowe z pliku .env przed startem kontekstu
        Dotenv dotenv = Dotenv.load();
        dotenv.entries().forEach(entry ->
            System.setProperty(entry.getKey(), entry.getValue())
        );
    }

    @Test
    void contextLoads() {
        // Jeśli kontekst Spring się poprawnie załaduje, test przejdzie
    }
}