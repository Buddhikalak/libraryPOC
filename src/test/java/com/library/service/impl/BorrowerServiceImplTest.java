package com.library.service.impl;

import com.library.dto.request.BorrowerRequest;
import com.library.dto.response.BookBorrowingResponse;
import com.library.dto.response.BookResponse;
import com.library.dto.response.BorrowerResponse;
import com.library.exception.LibraryException;
import com.library.model.Book;
import com.library.model.Borrower;
import com.library.model.BookBorrowing;
import com.library.repository.BorrowerRepository;
import com.library.repository.BookBorrowingRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BorrowerServiceImplTest {

    @Mock
    private BorrowerRepository borrowerRepository;

    @Mock
    private BookBorrowingRepository bookBorrowingRepository;

    private BorrowerServiceImpl borrowerService;
    private Borrower borrower;
    private BorrowerRequest borrowerRequest;
    private Book book;
    private BookBorrowing borrowing;

    @BeforeEach
    void setUp() {
        borrowerService = new BorrowerServiceImpl(borrowerRepository, bookBorrowingRepository);

        // Setup test borrower
        borrower = new Borrower();
        borrower.setId(1L);
        borrower.setName("Test Borrower");
        borrower.setEmail("test@example.com");

        // Setup test borrower request
        borrowerRequest = new BorrowerRequest();
        borrowerRequest.setName("Test Borrower");
        borrowerRequest.setEmail("test@example.com");

        // Setup test book
        book = new Book();
        book.setId(1L);
        book.setTitle("Test Book");
        book.setAuthor("Test Author");
        book.setIsbn("1234567890");
        book.setAvailable(true);

        // Setup test borrowing
        borrowing = new BookBorrowing();
        borrowing.setId(1L);
        borrowing.setBook(book);
        borrowing.setBorrower(borrower);
        borrowing.setBorrowedAt(LocalDateTime.now());
    }

    @Test
    void registerBorrower_Success() {
        when(borrowerRepository.existsByEmail(borrowerRequest.getEmail())).thenReturn(false);
        when(borrowerRepository.save(any(Borrower.class))).thenReturn(borrower);
        when(bookBorrowingRepository.findByBorrowerId(borrower.getId())).thenReturn(Collections.emptyList());

        BorrowerResponse response = borrowerService.registerBorrower(borrowerRequest);

        assertNotNull(response);
        assertEquals(borrower.getName(), response.getName());
        assertEquals(borrower.getEmail(), response.getEmail());
        assertTrue(response.getCurrentBorrowings().isEmpty());
        verify(borrowerRepository, times(1)).existsByEmail(borrowerRequest.getEmail());
        verify(borrowerRepository, times(1)).save(any(Borrower.class));
    }

    @Test
    void registerBorrower_DuplicateEmail() {
        when(borrowerRepository.existsByEmail(borrowerRequest.getEmail())).thenReturn(true);

        assertThrows(LibraryException.class, () -> borrowerService.registerBorrower(borrowerRequest));
        verify(borrowerRepository, times(1)).existsByEmail(borrowerRequest.getEmail());
        verify(borrowerRepository, never()).save(any(Borrower.class));
    }

    @Test
    void getBorrower_Success() {
        when(borrowerRepository.findById(1L)).thenReturn(Optional.of(borrower));
        when(bookBorrowingRepository.findByBorrowerId(borrower.getId())).thenReturn(Collections.emptyList());

        BorrowerResponse response = borrowerService.getBorrower(1L);

        assertNotNull(response);
        assertEquals(borrower.getId(), response.getId());
        assertEquals(borrower.getName(), response.getName());
        assertEquals(borrower.getEmail(), response.getEmail());
        assertTrue(response.getCurrentBorrowings().isEmpty());
        verify(borrowerRepository, times(1)).findById(1L);
    }

    @Test
    void getBorrower_NotFound() {
        when(borrowerRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(LibraryException.class, () -> borrowerService.getBorrower(1L));
        verify(borrowerRepository, times(1)).findById(1L);
    }

    @Test
    void getBorrower_WithCurrentBorrowings() {
        when(borrowerRepository.findById(1L)).thenReturn(Optional.of(borrower));
        when(bookBorrowingRepository.findByBorrowerId(borrower.getId())).thenReturn(Arrays.asList(borrowing));

        BorrowerResponse response = borrowerService.getBorrower(1L);

        assertNotNull(response);
        assertEquals(borrower.getId(), response.getId());
        assertEquals(1, response.getCurrentBorrowings().size());
        BookBorrowingResponse borrowingResponse = response.getCurrentBorrowings().get(0);
        assertEquals(borrowing.getId(), borrowingResponse.getId());
        assertEquals(book.getId(), borrowingResponse.getBook().getId());
        verify(borrowerRepository, times(1)).findById(1L);
        verify(bookBorrowingRepository, times(1)).findByBorrowerId(borrower.getId());
    }

    @Test
    void getAllBorrowers_Success() {
        List<Borrower> borrowers = Arrays.asList(borrower);
        when(borrowerRepository.findAll()).thenReturn(borrowers);
        when(bookBorrowingRepository.findByBorrowerId(borrower.getId())).thenReturn(Collections.emptyList());

        List<BorrowerResponse> responses = borrowerService.getAllBorrowers();

        assertNotNull(responses);
        assertEquals(1, responses.size());
        assertEquals(borrower.getName(), responses.get(0).getName());
        assertEquals(borrower.getEmail(), responses.get(0).getEmail());
        verify(borrowerRepository, times(1)).findAll();
    }

    @Test
    void updateBorrower_Success_SameEmail() {
        when(borrowerRepository.findById(1L)).thenReturn(Optional.of(borrower));
        when(borrowerRepository.save(any(Borrower.class))).thenReturn(borrower);
        when(bookBorrowingRepository.findByBorrowerId(borrower.getId())).thenReturn(Collections.emptyList());

        BorrowerResponse response = borrowerService.updateBorrower(1L, borrowerRequest);

        assertNotNull(response);
        assertEquals(borrower.getName(), response.getName());
        assertEquals(borrower.getEmail(), response.getEmail());
        verify(borrowerRepository, times(1)).findById(1L);
        verify(borrowerRepository, never()).existsByEmail(anyString());
        verify(borrowerRepository, times(1)).save(any(Borrower.class));
    }

    @Test
    void updateBorrower_Success_DifferentEmail() {
        Borrower existingBorrower = new Borrower();
        existingBorrower.setId(1L);
        existingBorrower.setName("Old Name");
        existingBorrower.setEmail("old@example.com");

        BorrowerRequest newRequest = new BorrowerRequest();
        newRequest.setName("New Name");
        newRequest.setEmail("new@example.com");

        when(borrowerRepository.findById(1L)).thenReturn(Optional.of(existingBorrower));
        when(borrowerRepository.existsByEmail(newRequest.getEmail())).thenReturn(false);
        when(borrowerRepository.save(any(Borrower.class))).thenReturn(existingBorrower);
        when(bookBorrowingRepository.findByBorrowerId(existingBorrower.getId())).thenReturn(Collections.emptyList());

        BorrowerResponse response = borrowerService.updateBorrower(1L, newRequest);

        assertNotNull(response);
        assertEquals(newRequest.getName(), response.getName());
        assertEquals(newRequest.getEmail(), response.getEmail());
        verify(borrowerRepository, times(1)).findById(1L);
        verify(borrowerRepository, times(1)).existsByEmail(newRequest.getEmail());
        verify(borrowerRepository, times(1)).save(any(Borrower.class));
    }

    @Test
    void updateBorrower_NotFound() {
        when(borrowerRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(LibraryException.class, () -> borrowerService.updateBorrower(1L, borrowerRequest));
        verify(borrowerRepository, times(1)).findById(1L);
        verify(borrowerRepository, never()).save(any(Borrower.class));
    }

    @Test
    void updateBorrower_DuplicateEmail() {
        Borrower existingBorrower = new Borrower();
        existingBorrower.setId(1L);
        existingBorrower.setName("Old Name");
        existingBorrower.setEmail("old@example.com");

        BorrowerRequest newRequest = new BorrowerRequest();
        newRequest.setName("New Name");
        newRequest.setEmail("existing@example.com");

        when(borrowerRepository.findById(1L)).thenReturn(Optional.of(existingBorrower));
        when(borrowerRepository.existsByEmail(newRequest.getEmail())).thenReturn(true);

        assertThrows(LibraryException.class, () -> borrowerService.updateBorrower(1L, newRequest));
        verify(borrowerRepository, times(1)).findById(1L);
        verify(borrowerRepository, times(1)).existsByEmail(newRequest.getEmail());
        verify(borrowerRepository, never()).save(any(Borrower.class));
    }

    @Test
    void deleteBorrower_Success() {
        when(borrowerRepository.existsById(1L)).thenReturn(true);
        doNothing().when(borrowerRepository).deleteById(1L);

        borrowerService.deleteBorrower(1L);

        verify(borrowerRepository, times(1)).existsById(1L);
        verify(borrowerRepository, times(1)).deleteById(1L);
    }

    @Test
    void deleteBorrower_NotFound() {
        when(borrowerRepository.existsById(1L)).thenReturn(false);

        assertThrows(LibraryException.class, () -> borrowerService.deleteBorrower(1L));
        verify(borrowerRepository, times(1)).existsById(1L);
        verify(borrowerRepository, never()).deleteById(anyLong());
    }
} 