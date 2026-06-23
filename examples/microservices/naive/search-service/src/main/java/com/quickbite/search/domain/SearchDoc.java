package com.quickbite.search.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

/**
 * Denormalized search document. One row per restaurant or menu item.
 *
 * <p>The natural key is ({@code type}, {@code refId}); {@code id} is a deterministic
 * surrogate so that repeated upserts of the same source entity overwrite in place.</p>
 */
@Entity
@Table(
        name = "search_doc",
        uniqueConstraints = @UniqueConstraint(name = "uk_search_doc_type_ref", columnNames = {"type", "ref_id"}),
        indexes = {
                @Index(name = "idx_search_doc_type", columnList = "type"),
                @Index(name = "idx_search_doc_restaurant", columnList = "restaurant_id"),
                @Index(name = "idx_search_doc_cuisine", columnList = "cuisine")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SearchDoc {

    @Id
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false, length = 16)
    private SearchDocType type;

    /** Id of the source entity (restaurant id or menu item id) in its owning service. */
    @Column(name = "ref_id", nullable = false)
    private UUID refId;

    /** Owning restaurant. For RESTAURANT docs this equals {@code refId}. */
    @Column(name = "restaurant_id", nullable = false)
    private UUID restaurantId;

    @Column(name = "name", nullable = false, length = 256)
    private String name;

    @Column(name = "cuisine", length = 128)
    private String cuisine;

    /** Price in minor units; null for RESTAURANT docs. */
    @Column(name = "price_cents")
    private Long priceCents;

    @Column(name = "lat")
    private Double lat;

    @Column(name = "lng")
    private Double lng;

    @Column(name = "available", nullable = false)
    private boolean available;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    /**
     * Deterministic surrogate id derived from (type, refId) so upserts are idempotent
     * across event redeliveries.
     */
    public static UUID deterministicId(SearchDocType type, UUID refId) {
        return UUID.nameUUIDFromBytes((type.name() + ":" + refId).getBytes());
    }
}
