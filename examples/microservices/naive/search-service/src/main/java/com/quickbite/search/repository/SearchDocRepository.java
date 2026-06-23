package com.quickbite.search.repository;

import com.quickbite.search.domain.SearchDoc;
import com.quickbite.search.domain.SearchDocType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface SearchDocRepository extends JpaRepository<SearchDoc, UUID> {

    Optional<SearchDoc> findByTypeAndRefId(SearchDocType type, UUID refId);

    /**
     * Free-text-ish search over the read model.
     *
     * <p>The example uses Postgres {@code ILIKE} matching on name + cuisine. In production
     * this query would be served by OpenSearch with proper relevance scoring, geo distance
     * sorting and typo tolerance; the SQL below is the local stand-in.</p>
     *
     * @param q       optional free text (matched against name/cuisine), null = match all
     * @param cuisine optional exact cuisine filter, null = any
     * @param type    optional doc type filter, null = both
     */
    @Query("""
            select d from SearchDoc d
            where d.available = true
              and (:q is null
                   or lower(d.name) like lower(concat('%', :q, '%'))
                   or lower(d.cuisine) like lower(concat('%', :q, '%')))
              and (:cuisine is null or lower(d.cuisine) = lower(:cuisine))
              and (:type is null or d.type = :type)
            """)
    Page<SearchDoc> search(@Param("q") String q,
                           @Param("cuisine") String cuisine,
                           @Param("type") SearchDocType type,
                           Pageable pageable);
}
