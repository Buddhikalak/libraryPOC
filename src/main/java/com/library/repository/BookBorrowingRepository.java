package com.library.repository;

import com.library.model.BookBorrowing;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface BookBorrowingRepository extends JpaRepository<BookBorrowing, Long> {
    List<BookBorrowing> findByBorrowerId(Long borrowerId);
    Optional<BookBorrowing> findByBookIdAndReturnedAtIsNull(Long bookId);
    List<BookBorrowing> findByBookId(Long bookId);
    boolean existsByBookIdAndReturnedAtIsNull(Long bookId);
} 