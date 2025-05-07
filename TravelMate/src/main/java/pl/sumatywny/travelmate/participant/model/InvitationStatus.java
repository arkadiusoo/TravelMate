package pl.sumatywny.travelmate.participant.model;

/**
 * Represents the current status of a trip invitation.
 * Tracks whether an invitation is waiting for response or has been acted upon.
 */
public enum InvitationStatus {
    PENDING,
    ACCEPTED,
    DECLINED
}