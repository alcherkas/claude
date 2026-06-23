package com.quickbite.driver.repository;

import com.quickbite.driver.domain.Driver;
import com.quickbite.driver.domain.DriverStatus;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface DriverRepository extends JpaRepository<Driver, UUID> {

    Optional<Driver> findByUserId(UUID userId);

    boolean existsByUserId(UUID userId);

    /**
     * Returns drivers in the given status ordered by squared planar distance from
     * (lat,lng). A squared Euclidean approximation is plenty for the example; a
     * production build would use PostGIS / the haversine formula.
     */
    @Query("""
            select d from Driver d
            where d.status = :status and d.lat is not null and d.lng is not null
            order by ((d.lat - :lat) * (d.lat - :lat) + (d.lng - :lng) * (d.lng - :lng)) asc
            """)
    List<Driver> findNearest(@Param("status") DriverStatus status,
                             @Param("lat") double lat,
                             @Param("lng") double lng,
                             Pageable pageable);
}
