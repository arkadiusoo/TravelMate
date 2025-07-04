package pl.sumatywny.travelmate.participant.bdd;

import io.cucumber.junit.Cucumber;
import io.cucumber.junit.CucumberOptions;
import org.junit.runner.RunWith;

@RunWith(Cucumber.class)
@CucumberOptions(
        features = "classpath:features/participant_management.feature",
        glue     = "pl.sumatywny.travelmate.participant.bdd",
        plugin   = {"pretty", "summary"}
)
public class ParticipantCucumberTestRunner {
}