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

  Scenario: Create a new trip
    Given the following trip payload:
      | name | budget |
      | Weekend w Warszawie | 1000 |
    When I POST "/api/trips" with the trip payload
    Then the HTTP status should be 200

  Scenario: User finds a trip by ID
    Given a trip with id "987e6543-e21b-12d3-a456-426614174111" exists in the database
    When user tries to get the trip by that ID
    Then the trip should be returned

  Scenario: User updates an existing trip
    Given a trip with id "987e6543-e21b-12d3-a456-426614174111" exists in the database
    When user updates the trip with name "Winter Trip" starting on "2025-12-01" and ending on "2025-12-10"
    Then the trip should be updated

  Scenario: User deletes a trip
    Given a trip with id "222e4444-e21b-12d3-a456-426614174999" exists in the database
    When user deletes the trip
    Then the trip should be removed

  Scenario: List participants for a trip
    Given a current user is authenticated as an organizer
    When I GET "/api/trips/{tripId}/participants"
    Then the HTTP status should be 200
    And the JSON array should contain a participant with email "organizer@example.com"

  Scenario: Invite a new participant by email
    Given a current user is authenticated as an organizer
    And the following participant invitation payload:
      | email                  | role   |
      | newuser@example.com    | MEMBER |
    When I POST "/api/trips/{tripId}/participants" with that payload
    Then the HTTP status should be 200
    And the JSON object should have a field "email" equal to "newuser@example.com"
    And the JSON object should have a field "status" equal to "PENDING"

  Scenario: Respond to invitation - Accept
    Given I have a pending invitation as participant
    When I PATCH "/api/trips/{tripId}/participants/{participantId}/respond" with:
      | status   |
      | ACCEPTED |
    Then the HTTP status should be 200
    And the JSON object should have "status" equal to "ACCEPTED"
    And the JSON object should have a field "joinedAt" that is not null