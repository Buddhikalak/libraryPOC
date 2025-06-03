package com.library.controller;

import com.library.dto.request.BorrowerRequest;
import com.library.dto.response.BorrowerResponse;
import com.library.service.BorrowerService;
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
@RequestMapping("/borrowers")
@RequiredArgsConstructor
@Tag(name = "Borrower Management", description = "APIs for managing library borrowers")
public class BorrowerController {

    private final BorrowerService borrowerService;

    @PostMapping
    @Operation(summary = "Register a new borrower", description = "Creates a new borrower in the library system")
    public ResponseEntity<BorrowerResponse> registerBorrower(@Valid @RequestBody BorrowerRequest request) {
        log.info("Received request to register new borrower: {}", request);
        BorrowerResponse response = borrowerService.registerBorrower(request);
        log.info("Successfully registered borrower with ID: {}", response.getId());
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get a borrower by ID", description = "Retrieves borrower details by their unique ID")
    public ResponseEntity<BorrowerResponse> getBorrower(@PathVariable Long id) {
        log.info("Received request to get borrower with ID: {}", id);
        BorrowerResponse response = borrowerService.getBorrower(id);
        log.info("Successfully retrieved borrower with ID: {}", id);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    @Operation(summary = "Get all borrowers", description = "Retrieves a list of all registered borrowers")
    public ResponseEntity<List<BorrowerResponse>> getAllBorrowers() {
        log.info("Received request to get all borrowers");
        List<BorrowerResponse> response = borrowerService.getAllBorrowers();
        log.info("Successfully retrieved {} borrowers", response.size());
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update a borrower", description = "Updates the details of an existing borrower")
    public ResponseEntity<BorrowerResponse> updateBorrower(
            @PathVariable Long id,
            @Valid @RequestBody BorrowerRequest request) {
        log.info("Received request to update borrower with ID: {}, request: {}", id, request);
        BorrowerResponse response = borrowerService.updateBorrower(id, request);
        log.info("Successfully updated borrower with ID: {}", id);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a borrower", description = "Removes a borrower from the library system")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteBorrower(@PathVariable Long id) {
        log.info("Received request to delete borrower with ID: {}", id);
        borrowerService.deleteBorrower(id);
        log.info("Successfully deleted borrower with ID: {}", id);
    }
}