package com.library.service.impl;

import com.library.dto.request.BorrowerRequest;
import com.library.dto.response.BookResponse;
import com.library.dto.response.BorrowerResponse;
import com.library.dto.response.BookBorrowingResponse;
import com.library.exception.LibraryException;
import com.library.model.Book;
import com.library.model.Borrower;
import com.library.model.BookBorrowing;
import com.library.repository.BorrowerRepository;
import com.library.repository.BookBorrowingRepository;
import com.library.service.BorrowerService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class BorrowerServiceImpl implements BorrowerService {

    private final BorrowerRepository borrowerRepository;
    private final BookBorrowingRepository bookBorrowingRepository;

    @Override
    public BorrowerResponse registerBorrower(BorrowerRequest request) {
        if (borrowerRepository.existsByEmail(request.getEmail())) {
            throw new LibraryException("Email already registered");
        }

        Borrower borrower = new Borrower();
        borrower.setName(request.getName());
        borrower.setEmail(request.getEmail());

        Borrower savedBorrower = borrowerRepository.save(borrower);
        return mapToBorrowerResponse(savedBorrower);
    }

    @Override
    @Transactional(readOnly = true)
    public BorrowerResponse getBorrower(Long id) {
        Borrower borrower = borrowerRepository.findById(id)
                .orElseThrow(() -> new LibraryException("Borrower not found"));
        return mapToBorrowerResponse(borrower);
    }

    @Override
    @Transactional(readOnly = true)
    public List<BorrowerResponse> getAllBorrowers() {
        return borrowerRepository.findAll().stream()
                .map(this::mapToBorrowerResponse)
                .collect(Collectors.toList());
    }

    @Override
    public BorrowerResponse updateBorrower(Long id, BorrowerRequest request) {
        Borrower borrower = borrowerRepository.findById(id)
                .orElseThrow(() -> new LibraryException("Borrower not found"));

        if (!borrower.getEmail().equals(request.getEmail()) &&
            borrowerRepository.existsByEmail(request.getEmail())) {
            throw new LibraryException("Email already registered");
        }

        borrower.setName(request.getName());
        borrower.setEmail(request.getEmail());

        Borrower updatedBorrower = borrowerRepository.save(borrower);
        return mapToBorrowerResponse(updatedBorrower);
    }

    @Override
    public void deleteBorrower(Long id) {
        if (!borrowerRepository.existsById(id)) {
            throw new LibraryException("Borrower not found");
        }
        borrowerRepository.deleteById(id);
    }

    private BorrowerResponse mapToBorrowerResponse(Borrower borrower) {
        List<BookBorrowing> currentBorrowings = bookBorrowingRepository.findByBorrowerId(borrower.getId())
                .stream()
                .filter(borrowing -> borrowing.getReturnedAt() == null)
                .collect(Collectors.toList());

        List<BookBorrowingResponse> borrowingResponses = currentBorrowings.stream()
                .map(this::mapToBorrowingResponse)
                .collect(Collectors.toList());

        return BorrowerResponse.builder()
                .id(borrower.getId())
                .name(borrower.getName())
                .email(borrower.getEmail())
                .currentBorrowings(borrowingResponses)
                .build();
    }

    private BookBorrowingResponse mapToBorrowingResponse(BookBorrowing borrowing) {
        Borrower borrower = borrowing.getBorrower();
        BorrowerResponse borrowerResponse = BorrowerResponse.builder()
                .id(borrower.getId())
                .name(borrower.getName())
                .email(borrower.getEmail())
                .currentBorrowings(Collections.emptyList())  // Don't include borrowings to avoid recursion
                .build();

        return BookBorrowingResponse.builder()
                .id(borrowing.getId())
                .book(mapToBookResponse(borrowing.getBook()))
                .borrower(borrowerResponse)
                .borrowedAt(borrowing.getBorrowedAt())
                .returnedAt(borrowing.getReturnedAt())
                .build();
    }

    private BookResponse mapToBookResponse(Book book) {
        return BookResponse.builder()
                .id(book.getId())
                .isbn(book.getIsbn())
                .title(book.getTitle())
                .author(book.getAuthor())
                .available(book.isAvailable())
                .build();
    }
} 