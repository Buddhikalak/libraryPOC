package com.library.service;

import com.library.dto.request.BookRequest;
import com.library.dto.response.BookResponse;
import java.util.List;

public interface BookService {
    BookResponse registerBook(BookRequest request);
    BookResponse getBook(Long id);
    List<BookResponse> getAllBooks();
    List<BookResponse> getBooksByIsbn(String isbn);
    BookResponse updateBook(Long id, BookRequest request);
    void deleteBook(Long id);
    BookResponse borrowBook(Long bookId, Long borrowerId);
    BookResponse returnBook(Long bookId);
} 