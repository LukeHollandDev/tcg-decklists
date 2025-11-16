package dev.lukeholland.tcg.decklists.api.common.exception;

/**
 * Exception thrown when an entity is not found in the database.
 * Results in a 404 Not Found HTTP response with RFC 7807 Problem Details format.
 */
public class EntityNotFoundException extends RuntimeException {
    private final String entityType;
    private final String entityId;

    /**
     * Creates a new EntityNotFoundException.
     *
     * @param entityType the type of entity that was not found (e.g., "Card", "Decklist")
     * @param entityId   the ID of the entity that was not found
     */
    public EntityNotFoundException(String entityType, String entityId) {
        super(String.format("%s not found with id: %s", entityType, entityId));
        this.entityType = entityType;
        this.entityId = entityId;
    }

    public String getEntityType() {
        return entityType;
    }

    public String getEntityId() {
        return entityId;
    }
}
