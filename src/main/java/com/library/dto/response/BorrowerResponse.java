package com.library.dto.response;

import lombok.Data;
import lombok.Builder;
import java.util.List;

@Data
@Builder
public class BorrowerResponse {
    private Long id;
    private String name;
    private String email;
    private List<BookBorrowingResponse> currentBorrowings;
} 