package com.parkit.parkingsystem.acceptance;

import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.cucumber.spring.CucumberContextConfiguration;

@CucumberContextConfiguration
public class FreeThirtyMinutesOrLessSteps {
	
	@Given("utilisateur {string} est garé depuis {int} minutes;")
	public void untilisateur(String regNum, int min) {}
	
	@When("il sort;")
	public void ilSort() {}
	
	@Then("le ticket persisté a une plaque {string}, un tarif à {double} et la place persistée a une disponibilité {string};")
	public void persistence(String regNum, double fare, String availability) {}
}
