package com.quickbite.search.web;

import com.quickbite.search.domain.SearchDocType;
import com.quickbite.search.dto.SearchResponse;
import com.quickbite.search.dto.SearchResultDto;
import com.quickbite.search.service.SearchIndexService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * MockMvc slice test for the public search endpoints. The service layer is mocked so the
 * test exercises request mapping, parameter binding and JSON serialization only.
 */
@WebMvcTest(SearchController.class)
class SearchControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private SearchIndexService searchService;

    @Test
    void search_returnsHits() throws Exception {
        UUID id = UUID.randomUUID();
        SearchResultDto hit = new SearchResultDto(
                id, "RESTAURANT", id, id, "Pizza Palace", "ITALIAN",
                null, 51.5, -0.12, true, 0.4, Instant.parse("2026-06-22T10:00:00Z"));
        when(searchService.search(eq("pizza"), isNull(), any(), any(), isNull()))
                .thenReturn(new SearchResponse("pizza", null, 1, List.of(hit)));

        mockMvc.perform(get("/api/search").param("q", "pizza").param("lat", "51.51").param("lng", "-0.13"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.count").value(1))
                .andExpect(jsonPath("$.results[0].name").value("Pizza Palace"))
                .andExpect(jsonPath("$.results[0].cuisine").value("ITALIAN"));
    }

    @Test
    void searchMenuItems_passesMenuItemType() throws Exception {
        when(searchService.search(any(), any(), any(), any(), eq(SearchDocType.MENU_ITEM)))
                .thenReturn(new SearchResponse(null, null, 0, List.of()));

        mockMvc.perform(get("/api/search/menu-items").param("q", "burger"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.count").value(0));
    }
}
