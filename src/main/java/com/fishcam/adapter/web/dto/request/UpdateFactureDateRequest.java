package com.fishcam.adapter.web.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;

@Data
public class UpdateFactureDateRequest {

    @NotNull
    private LocalDate dateAchat;
}
