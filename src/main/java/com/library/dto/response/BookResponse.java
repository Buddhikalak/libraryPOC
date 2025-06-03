package com.library.dto.response;

import lombok.Data;
import lombok.Builder;

@Data
@Builder
public class BookResponse {
    private Long id;
    private String isbn;
    private String title;
    private String author;
    private boolean available;
    private BorrowerResponse currentBorrower;
} 