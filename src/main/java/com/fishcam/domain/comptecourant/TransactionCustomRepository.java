package com.fishcam.domain.comptecourant;

import com.fishcam.adapter.web.dto.response.TransactionGlobalResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.time.LocalDate;

public interface TransactionCustomRepository {
    Page<TransactionGlobalResponse> findAllTransactionsDynamically(
            Long poissonnerieId, String type, String searchTerm, LocalDate date, Pageable pageable);
}