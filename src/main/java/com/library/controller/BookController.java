package com.library.controller;

import com.library.dto.request.BookRequest;
import com.library.dto.response.BookResponse;
import com.library.service.BookService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/books")
@RequiredArgsConstructor
@Tag(name = "Book Management", description = "APIs for managing library books")
public class BookController {

    private final BookService bookService;

    @PostMapping
    @Operation(summary = "Register a new book", description = "Adds a new book to the library system")
    public ResponseEntity<BookResponse> registerBook(@Valid @RequestBody BookRequest request) {
        log.info("Received request to register new book: {}", request);
        BookResponse response = bookService.registerBook(request);
        log.info("Successfully registered book with ID: {}", response.getId());
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get a book by ID", description = "Retrieves book details by its unique ID")
    public ResponseEntity<BookResponse> getBook(@PathVariable Long id) {
        log.info("Received request to get book with ID: {}", id);
        BookResponse response = bookService.getBook(id);
        log.info("Successfully retrieved book with ID: {}", id);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    @Operation(summary = "Get all books", description = "Retrieves a list of all books in the library")
    public ResponseEntity<List<BookResponse>> getAllBooks() {
        log.info("Received request to get all books");
        List<BookResponse> response = bookService.getAllBooks();
        log.info("Successfully retrieved {} books", response.size());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/isbn/{isbn}")
    @Operation(summary = "Get books by ISBN", description = "Retrieves all books with the specified ISBN number")
    public ResponseEntity<List<BookResponse>> getBooksByIsbn(@PathVariable String isbn) {
        log.info("Received request to get books with ISBN: {}", isbn);
        List<BookResponse> response = bookService.getBooksByIsbn(isbn);
        log.info("Successfully retrieved {} books with ISBN: {}", response.size(), isbn);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update a book", description = "Updates the details of an existing book")
    public ResponseEntity<BookResponse> updateBook(
            @PathVariable Long id,
            @Valid @RequestBody BookRequest request) {
        log.info("Received request to update book with ID: {}, request: {}", id, request);
        BookResponse response = bookService.updateBook(id, request);
        log.info("Successfully updated book with ID: {}", id);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a book", description = "Removes a book from the library system")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteBook(@PathVariable Long id) {
        log.info("Received request to delete book with ID: {}", id);
        bookService.deleteBook(id);
        log.info("Successfully deleted book with ID: {}", id);
    }

    @PostMapping("/{bookId}/borrow/{borrowerId}")
    @Operation(summary = "Borrow a book", description = "Allows a borrower to borrow a book from the library")
    public ResponseEntity<BookResponse> borrowBook(
            @PathVariable Long bookId,
            @PathVariable Long borrowerId) {
        log.info("Received request to borrow book with ID: {} by borrower with ID: {}", bookId, borrowerId);
        BookResponse response = bookService.borrowBook(bookId, borrowerId);
        log.info("Successfully borrowed book with ID: {} by borrower with ID: {}", bookId, borrowerId);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{bookId}/return")
    @Operation(summary = "Return a book", description = "Records the return of a borrowed book")
    public ResponseEntity<BookResponse> returnBook(@PathVariable Long bookId) {
        log.info("Received request to return book with ID: {}", bookId);
        BookResponse response = bookService.returnBook(bookId);
        log.info("Successfully returned book with ID: {}", bookId);
        return ResponseEntity.ok(response);
    }
} 