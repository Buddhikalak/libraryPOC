package com.library.dto.response;

import lombok.Data;
import lombok.Builder;
import java.time.LocalDateTime;

@Data
@Builder
public class BookBorrowingResponse {
    private Long id;
    private BookResponse book;
    private BorrowerResponse borrower;
    private LocalDateTime borrowedAt;
    private LocalDateTime returnedAt;
} 