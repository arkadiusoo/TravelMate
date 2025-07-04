# Created by tropolek at 03/07/2025
Feature: Participant management API
  As a user of the TravelMate application
  I want to manage trip participants via REST
  So that I can invite, list and manage participants in a BDD style

  Background:
    Given a current user is authenticated as an organizer
    And a sample trip exists with participants

  Scenario: List participants for a trip
    When I GET "/api/trips/{tripId}/participants"
    Then the HTTP status should be 200
    And the JSON array should contain a participant with email "organizer@example.com"

  Scenario: Invite a new participant by email
    Given the following participant invitation payload:
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