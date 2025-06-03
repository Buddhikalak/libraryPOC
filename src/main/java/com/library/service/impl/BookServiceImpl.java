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
import com.library.service.BookService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class BookServiceImpl implements BookService {

    private final BookRepository bookRepository;
    private final BorrowerRepository borrowerRepository;
    private final BookBorrowingRepository bookBorrowingRepository;

    @Override
    public BookResponse registerBook(BookRequest request) {
        // Check if a book with the same ISBN but different title/author exists
        List<Book> existingBooks = bookRepository.findByIsbn(request.getIsbn());
        if (!existingBooks.isEmpty()) {
            Book existingBook = existingBooks.get(0);
            if (!existingBook.getTitle().equals(request.getTitle()) || 
                !existingBook.getAuthor().equals(request.getAuthor())) {
                throw new LibraryException("A book with this ISBN already exists with different title or author");
            }
        }

        Book book = new Book();
        book.setIsbn(request.getIsbn());
        book.setTitle(request.getTitle());
        book.setAuthor(request.getAuthor());
        book.setAvailable(true);

        Book savedBook = bookRepository.save(book);
        return mapToBookResponse(savedBook);
    }

    @Override
    @Transactional(readOnly = true)
    public BookResponse getBook(Long id) {
        Book book = bookRepository.findById(id)
                .orElseThrow(() -> new LibraryException("Book not found"));
        return mapToBookResponse(book);
    }

    @Override
    @Transactional(readOnly = true)
    public List<BookResponse> getAllBooks() {
        return bookRepository.findAll().stream()
                .map(this::mapToBookResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<BookResponse> getBooksByIsbn(String isbn) {
        return bookRepository.findByIsbn(isbn).stream()
                .map(this::mapToBookResponse)
                .collect(Collectors.toList());
    }

    @Override
    public BookResponse updateBook(Long id, BookRequest request) {
        Book book = bookRepository.findById(id)
                .orElseThrow(() -> new LibraryException("Book not found"));

        // If ISBN is being changed, check if it conflicts with existing books
        if (!book.getIsbn().equals(request.getIsbn())) {
            List<Book> existingBooks = bookRepository.findByIsbn(request.getIsbn());
            if (!existingBooks.isEmpty()) {
                Book existingBook = existingBooks.get(0);
                if (!existingBook.getTitle().equals(request.getTitle()) || 
                    !existingBook.getAuthor().equals(request.getAuthor())) {
                    throw new LibraryException("A book with this ISBN already exists with different title or author");
                }
            }
        } else {
            // If ISBN is not changing, check if title/author is being changed
            // which would violate the ISBN uniqueness rule
            if (!book.getTitle().equals(request.getTitle()) || 
                !book.getAuthor().equals(request.getAuthor())) {
                List<Book> otherBooksWithSameIsbn = bookRepository.findByIsbn(book.getIsbn())
                    .stream()
                    .filter(b -> !b.getId().equals(book.getId()))
                    .toList();
                if (!otherBooksWithSameIsbn.isEmpty()) {
                    throw new LibraryException("Cannot change title or author as other books with this ISBN exist");
                }
            }
        }

        book.setIsbn(request.getIsbn());
        book.setTitle(request.getTitle());
        book.setAuthor(request.getAuthor());

        Book updatedBook = bookRepository.save(book);
        return mapToBookResponse(updatedBook);
    }

    @Override
    public void deleteBook(Long id) {
        if (!bookRepository.existsById(id)) {
            throw new LibraryException("Book not found");
        }
        bookRepository.deleteById(id);
    }

    @Override
    public BookResponse borrowBook(Long bookId, Long borrowerId) {
        Book book = bookRepository.findByIdAndAvailable(bookId, true)
                .orElseThrow(() -> new LibraryException("Book not found or not available"));

        Borrower borrower = borrowerRepository.findById(borrowerId)
                .orElseThrow(() -> new LibraryException("Borrower not found"));

        if (bookBorrowingRepository.existsByBookIdAndReturnedAtIsNull(bookId)) {
            throw new LibraryException("Book is already borrowed");
        }

        BookBorrowing borrowing = new BookBorrowing();
        borrowing.setBook(book);
        borrowing.setBorrower(borrower);
        bookBorrowingRepository.save(borrowing);

        book.setAvailable(false);
        Book updatedBook = bookRepository.save(book);

        return mapToBookResponse(updatedBook);
    }

    @Override
    public BookResponse returnBook(Long bookId) {
        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new LibraryException("Book not found"));

        BookBorrowing borrowing = bookBorrowingRepository.findByBookIdAndReturnedAtIsNull(bookId)
                .orElseThrow(() -> new LibraryException("Book is not currently borrowed"));

        borrowing.setReturnedAt(LocalDateTime.now());
        bookBorrowingRepository.save(borrowing);

        book.setAvailable(true);
        Book updatedBook = bookRepository.save(book);

        return mapToBookResponse(updatedBook);
    }

    private BookResponse mapToBookResponse(Book book) {
        BookBorrowing currentBorrowing = bookBorrowingRepository.findByBookIdAndReturnedAtIsNull(book.getId())
                .orElse(null);

        BorrowerResponse currentBorrower = null;
        if (currentBorrowing != null) {
            Borrower borrower = currentBorrowing.getBorrower();
            currentBorrower = BorrowerResponse.builder()
                    .id(borrower.getId())
                    .name(borrower.getName())
                    .email(borrower.getEmail())
                    .build();
        }

        return BookResponse.builder()
                .id(book.getId())
                .isbn(book.getIsbn())
                .title(book.getTitle())
                .author(book.getAuthor())
                .available(book.isAvailable())
                .currentBorrower(currentBorrower)
                .build();
    }
} 