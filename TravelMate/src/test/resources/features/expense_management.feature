# Created by arkadiusz at 29/06/2025
Feature: Expense management API
  As a user of the TravelMate application
  I want to manage my trip expenses via REST
  So that I can list, add and update expenses in a BDD style

  Background:
    Given a current user is authenticated
    And a sample expense exists for a trip

  Scenario: List expenses for a trip
    When I GET "/api/trips/{tripId}/expenses"
    Then the HTTP status should be 200
    And the JSON array should contain an expense with name "Lunch"

  Scenario: Add a new expense
    Given the following expense payload:
      | name | amount | category | description      | date       | payerId   |
      | Lunch| 10.00  | FOOD     | Business lunch   | 2025-06-29 | <payerId> |
    When I POST "/api/trips/{tripId}/expenses" with that payload
    Then the HTTP status should be 200
    And the JSON object should have a field "name" equal to "Lunch"

  Scenario: Patch an expense description
    Given I have an existing expense with id "<expenseId>"
    And I want to change its description to "Updated lunch"
    When I PATCH "/api/trips/{tripId}/expenses/{expenseId}" with:
      | description |
      | Updated lunch |
    Then the HTTP status should be 200
    And the JSON object should have "description" equal to "Updated lunch"