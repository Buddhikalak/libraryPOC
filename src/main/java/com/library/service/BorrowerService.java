package com.library.service;

import com.library.dto.request.BorrowerRequest;
import com.library.dto.response.BorrowerResponse;
import java.util.List;

public interface BorrowerService {
    BorrowerResponse registerBorrower(BorrowerRequest request);
    BorrowerResponse getBorrower(Long id);
    List<BorrowerResponse> getAllBorrowers();
    BorrowerResponse updateBorrower(Long id, BorrowerRequest request);
    void deleteBorrower(Long id);
} 