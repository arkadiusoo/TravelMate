package pl.sumatywny.travelmate.budget.bdd;

import io.cucumber.junit.Cucumber;
import io.cucumber.junit.CucumberOptions;
import org.junit.runner.RunWith;

@RunWith(Cucumber.class)
@CucumberOptions(
  features = "classpath:features",
  glue     = "pl.sumatywny.travelmate.budget.bdd",
  plugin   = {"pretty", "summary"}
)
public class CucumberTestRunner {
}