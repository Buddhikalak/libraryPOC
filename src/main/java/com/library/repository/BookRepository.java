package com.library.repository;

import com.library.model.Book;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface BookRepository extends JpaRepository<Book, Long> {
    List<Book> findByIsbn(String isbn);
    List<Book> findByAvailable(boolean available);
    Optional<Book> findByIdAndAvailable(Long id, boolean available);
    boolean existsByIsbnAndTitleAndAuthor(String isbn, String title, String author);
} 