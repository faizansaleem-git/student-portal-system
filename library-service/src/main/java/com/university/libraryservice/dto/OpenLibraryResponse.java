package com.university.libraryservice.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import java.util.List;

/**
 * OpenLibraryResponse — DTO for deserialising the Open Library Books API response.
 *
 * The API returns a map keyed by "ISBN:<isbn>". Each value is a book detail object.
 * We only need title and authors[0].name.
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class OpenLibraryResponse {
    private String title;
    private List<Author> authors;

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Author {
        private String name;
    }

    public String getFirstAuthor() {
        if (authors != null && !authors.isEmpty()) {
            return authors.get(0).getName();
        }
        return "Unknown Author";
    }
}
