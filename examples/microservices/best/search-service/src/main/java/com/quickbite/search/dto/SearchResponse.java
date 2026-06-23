package com.quickbite.search.dto;

import java.util.List;

/** Envelope returned by the public search endpoints. */
public record SearchResponse(
        String query,
        String cuisine,
        int count,
        List<SearchResultDto> results
) {
}
