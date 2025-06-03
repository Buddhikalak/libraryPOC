package com.library.service.impl;

import com.library.dto.request.BookRequest;
import com.library.dto.response.BookResponse;
import com.library.dto.response.BorrowerResponse;
import com.library.exception.LibraryException;
import com.library.model.Book;
import com.library.model.Borrower;
import com.library.model.BookBorrowing;
import com.library.repository.BookRepository;
import com.library.repository.BorrowerRepository;
import com.library.repository.BookBorrowingRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BookServiceImplTest {

    @Mock
    private BookRepository bookRepository;

    @Mock
    private BorrowerRepository borrowerRepository;

    @Mock
    private BookBorrowingRepository bookBorrowingRepository;

    private BookServiceImpl bookService;
    private Book book;
    private BookRequest bookRequest;
    private Borrower borrower;
    private BookBorrowing borrowing;

    @BeforeEach
    void setUp() {
        bookService = new BookServiceImpl(bookRepository, borrowerRepository, bookBorrowingRepository);

        // Setup test book
        book = new Book();
        book.setId(1L);
        book.setTitle("Test Book");
        book.setAuthor("Test Author");
        book.setIsbn("1234567890");
        book.setAvailable(true);

        // Setup test book request
        bookRequest = new BookRequest();
        bookRequest.setTitle("Test Book");
        bookRequest.setAuthor("Test Author");
        bookRequest.setIsbn("1234567890");

        // Setup test borrower
        borrower = new Borrower();
        borrower.setId(1L);
        borrower.setName("Test Borrower");
        borrower.setEmail("test@example.com");

        // Setup test borrowing
        borrowing = new BookBorrowing();
        borrowing.setId(1L);
        borrowing.setBook(book);
        borrowing.setBorrower(borrower);
        borrowing.setBorrowedAt(LocalDateTime.now());
    }

    @Test
    void registerBook_Success() {
        when(bookRepository.save(any(Book.class))).thenReturn(book);

        BookResponse response = bookService.registerBook(bookRequest);

        assertNotNull(response);
        assertEquals(book.getTitle(), response.getTitle());
        assertEquals(book.getAuthor(), response.getAuthor());
        assertEquals(book.getIsbn(), response.getIsbn());
        assertTrue(response.isAvailable());
        verify(bookRepository, times(1)).save(any(Book.class));
    }

    @Test
    void getBook_Success() {
        when(bookRepository.findById(1L)).thenReturn(Optional.of(book));
        when(bookBorrowingRepository.findByBookIdAndReturnedAtIsNull(1L)).thenReturn(Optional.empty());

        BookResponse response = bookService.getBook(1L);

        assertNotNull(response);
        assertEquals(book.getId(), response.getId());
        assertEquals(book.getTitle(), response.getTitle());
        assertEquals(book.getAuthor(), response.getAuthor());
        assertNull(response.getCurrentBorrower());
        verify(bookRepository, times(1)).findById(1L);
    }

    @Test
    void getBook_NotFound() {
        when(bookRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(LibraryException.class, () -> bookService.getBook(1L));
        verify(bookRepository, times(1)).findById(1L);
    }

    @Test
    void getAllBooks_Success() {
        List<Book> books = Arrays.asList(book);
        when(bookRepository.findAll()).thenReturn(books);
        when(bookBorrowingRepository.findByBookIdAndReturnedAtIsNull(1L)).thenReturn(Optional.empty());

        List<BookResponse> responses = bookService.getAllBooks();

        assertNotNull(responses);
        assertEquals(1, responses.size());
        assertEquals(book.getTitle(), responses.get(0).getTitle());
        verify(bookRepository, times(1)).findAll();
    }

    @Test
    void getBooksByIsbn_Success() {
        List<Book> books = Arrays.asList(book);
        when(bookRepository.findByIsbn("1234567890")).thenReturn(books);
        when(bookBorrowingRepository.findByBookIdAndReturnedAtIsNull(1L)).thenReturn(Optional.empty());

        List<BookResponse> responses = bookService.getBooksByIsbn("1234567890");

        assertNotNull(responses);
        assertEquals(1, responses.size());
        assertEquals(book.getIsbn(), responses.get(0).getIsbn());
        verify(bookRepository, times(1)).findByIsbn("1234567890");
    }

    @Test
    void updateBook_Success_SameIsbn() {
        // Test updating book with same ISBN (no duplicate check needed)
        when(bookRepository.findById(1L)).thenReturn(Optional.of(book));
        when(bookRepository.save(any(Book.class))).thenReturn(book);

        BookResponse response = bookService.updateBook(1L, bookRequest);

        assertNotNull(response);
        assertEquals(book.getTitle(), response.getTitle());
        assertEquals(book.getAuthor(), response.getAuthor());
        assertEquals(book.getIsbn(), response.getIsbn());
        verify(bookRepository, times(1)).findById(1L);
        verify(bookRepository, never()).existsByIsbnAndTitleAndAuthor(anyString(), anyString(), anyString());
        verify(bookRepository, times(1)).save(any(Book.class));
    }

    @Test
    void updateBook_Success_DifferentIsbn() {
        // Test updating book with different ISBN (duplicate check needed)
        Book existingBook = new Book();
        existingBook.setId(1L);
        existingBook.setTitle("Test Book");
        existingBook.setAuthor("Test Author");
        existingBook.setIsbn("9876543210"); // Different from request
        existingBook.setAvailable(true);

        BookRequest newRequest = new BookRequest();
        newRequest.setTitle("Test Book");
        newRequest.setAuthor("Test Author");
        newRequest.setIsbn("1234567890"); // New ISBN

        when(bookRepository.findById(1L)).thenReturn(Optional.of(existingBook));
        when(bookRepository.findByIsbn("1234567890")).thenReturn(List.of()); // No existing books with this ISBN
        when(bookRepository.save(any(Book.class))).thenReturn(existingBook);

        BookResponse response = bookService.updateBook(1L, newRequest);

        assertNotNull(response);
        assertEquals(newRequest.getTitle(), response.getTitle());
        assertEquals(newRequest.getAuthor(), response.getAuthor());
        assertEquals(newRequest.getIsbn(), response.getIsbn());
        verify(bookRepository, times(1)).findById(1L);
        verify(bookRepository, times(1)).findByIsbn("1234567890");
        verify(bookRepository, times(1)).save(any(Book.class));
    }

    @Test
    void updateBook_DuplicateBook() {
        // Create a book with different ISBN to simulate duplicate check
        Book existingBook = new Book();
        existingBook.setId(1L);
        existingBook.setTitle("Test Book");
        existingBook.setAuthor("Test Author");
        existingBook.setIsbn("9876543210"); // Different ISBN
        existingBook.setAvailable(true);

        // Create another book with the ISBN we want to update to
        Book bookWithTargetIsbn = new Book();
        bookWithTargetIsbn.setId(2L);
        bookWithTargetIsbn.setTitle("Different Title");
        bookWithTargetIsbn.setAuthor("Different Author");
        bookWithTargetIsbn.setIsbn("1234567890"); // Target ISBN
        bookWithTargetIsbn.setAvailable(true);

        // Create a request with ISBN that matches another book
        BookRequest duplicateRequest = new BookRequest();
        duplicateRequest.setTitle("Test Book");
        duplicateRequest.setAuthor("Test Author");
        duplicateRequest.setIsbn("1234567890"); // This ISBN will be checked for duplicates

        when(bookRepository.findById(1L)).thenReturn(Optional.of(existingBook));
        when(bookRepository.findByIsbn("1234567890")).thenReturn(List.of(bookWithTargetIsbn));

        assertThrows(LibraryException.class, () -> bookService.updateBook(1L, duplicateRequest));
        verify(bookRepository, times(1)).findById(1L);
        verify(bookRepository, times(1)).findByIsbn("1234567890");
        verify(bookRepository, never()).save(any(Book.class));
    }

    @Test
    void updateBook_NotFound() {
        when(bookRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(LibraryException.class, () -> bookService.updateBook(1L, bookRequest));
        verify(bookRepository, times(1)).findById(1L);
        verify(bookRepository, never()).existsByIsbnAndTitleAndAuthor(anyString(), anyString(), anyString());
        verify(bookRepository, never()).save(any(Book.class));
    }

    @Test
    void deleteBook_Success() {
        when(bookRepository.existsById(1L)).thenReturn(true);
        doNothing().when(bookRepository).deleteById(1L);

        bookService.deleteBook(1L);

        verify(bookRepository, times(1)).existsById(1L);
        verify(bookRepository, times(1)).deleteById(1L);
    }

    @Test
    void deleteBook_NotFound() {
        when(bookRepository.existsById(1L)).thenReturn(false);

        assertThrows(LibraryException.class, () -> bookService.deleteBook(1L));
        verify(bookRepository, times(1)).existsById(1L);
        verify(bookRepository, never()).deleteById(anyLong());
    }

    @Test
    void borrowBook_Success() {
        when(bookRepository.findByIdAndAvailable(1L, true)).thenReturn(Optional.of(book));
        when(borrowerRepository.findById(1L)).thenReturn(Optional.of(borrower));
        when(bookBorrowingRepository.existsByBookIdAndReturnedAtIsNull(1L)).thenReturn(false);
        when(bookBorrowingRepository.save(any(BookBorrowing.class))).thenReturn(borrowing);
        when(bookRepository.save(any(Book.class))).thenReturn(book);

        BookResponse response = bookService.borrowBook(1L, 1L);

        assertNotNull(response);
        assertFalse(response.isAvailable());
        verify(bookRepository, times(1)).findByIdAndAvailable(1L, true);
        verify(borrowerRepository, times(1)).findById(1L);
        verify(bookBorrowingRepository, times(1)).save(any(BookBorrowing.class));
        verify(bookRepository, times(1)).save(any(Book.class));
    }

    @Test
    void borrowBook_BookNotFound() {
        when(bookRepository.findByIdAndAvailable(1L, true)).thenReturn(Optional.empty());

        assertThrows(LibraryException.class, () -> bookService.borrowBook(1L, 1L));
        verify(bookRepository, times(1)).findByIdAndAvailable(1L, true);
        verify(borrowerRepository, never()).findById(anyLong());
    }

    @Test
    void borrowBook_BorrowerNotFound() {
        when(bookRepository.findByIdAndAvailable(1L, true)).thenReturn(Optional.of(book));
        when(borrowerRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(LibraryException.class, () -> bookService.borrowBook(1L, 1L));
        verify(bookRepository, times(1)).findByIdAndAvailable(1L, true);
        verify(borrowerRepository, times(1)).findById(1L);
    }

    @Test
    void borrowBook_AlreadyBorrowed() {
        when(bookRepository.findByIdAndAvailable(1L, true)).thenReturn(Optional.of(book));
        when(borrowerRepository.findById(1L)).thenReturn(Optional.of(borrower));
        when(bookBorrowingRepository.existsByBookIdAndReturnedAtIsNull(1L)).thenReturn(true);

        assertThrows(LibraryException.class, () -> bookService.borrowBook(1L, 1L));
        verify(bookRepository, times(1)).findByIdAndAvailable(1L, true);
        verify(borrowerRepository, times(1)).findById(1L);
        verify(bookBorrowingRepository, times(1)).existsByBookIdAndReturnedAtIsNull(1L);
        verify(bookBorrowingRepository, never()).save(any(BookBorrowing.class));
    }

    @Test
    void returnBook_Success() {
        book.setAvailable(false);
        when(bookRepository.findById(1L)).thenReturn(Optional.of(book));
        when(bookBorrowingRepository.findByBookIdAndReturnedAtIsNull(1L)).thenReturn(Optional.of(borrowing));
        when(bookBorrowingRepository.save(any(BookBorrowing.class))).thenReturn(borrowing);
        when(bookRepository.save(any(Book.class))).thenReturn(book);

        BookResponse response = bookService.returnBook(1L);

        assertNotNull(response);
        assertTrue(response.isAvailable());
        verify(bookRepository, times(1)).findById(1L);
        verify(bookBorrowingRepository, atLeastOnce()).findByBookIdAndReturnedAtIsNull(1L);
        verify(bookBorrowingRepository, times(1)).save(any(BookBorrowing.class));
        verify(bookRepository, times(1)).save(any(Book.class));
    }

    @Test
    void returnBook_NotFound() {
        when(bookRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(LibraryException.class, () -> bookService.returnBook(1L));
        verify(bookRepository, times(1)).findById(1L);
        verify(bookBorrowingRepository, never()).findByBookIdAndReturnedAtIsNull(anyLong());
    }

    @Test
    void returnBook_NotBorrowed() {
        when(bookRepository.findById(1L)).thenReturn(Optional.of(book));
        when(bookBorrowingRepository.findByBookIdAndReturnedAtIsNull(1L)).thenReturn(Optional.empty());

        assertThrows(LibraryException.class, () -> bookService.returnBook(1L));
        verify(bookRepository, times(1)).findById(1L);
        verify(bookBorrowingRepository, times(1)).findByBookIdAndReturnedAtIsNull(1L);
        verify(bookBorrowingRepository, never()).save(any(BookBorrowing.class));
    }

    @Test
    void mapToBookResponse_WithCurrentBorrower() {
        when(bookRepository.findById(1L)).thenReturn(Optional.of(book));
        when(bookBorrowingRepository.findByBookIdAndReturnedAtIsNull(1L)).thenReturn(Optional.of(borrowing));

        BookResponse response = bookService.getBook(1L);

        assertNotNull(response);
        assertNotNull(response.getCurrentBorrower());
        assertEquals(borrower.getId(), response.getCurrentBorrower().getId());
        assertEquals(borrower.getName(), response.getCurrentBorrower().getName());
        assertEquals(borrower.getEmail(), response.getCurrentBorrower().getEmail());
        verify(bookRepository, times(1)).findById(1L);
        verify(bookBorrowingRepository, times(1)).findByBookIdAndReturnedAtIsNull(1L);
    }
} 