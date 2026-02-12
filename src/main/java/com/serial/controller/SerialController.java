package com.serial.controller;

import com.serial.dto.request.*;
import com.serial.dto.response.*;
import com.serial.service.SerialService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
public class SerialController {

    private final SerialService serialService;

    public SerialController(SerialService serialService) {
        this.serialService = serialService;
    }

    @PostMapping("/serials_insert")
    public ResponseEntity<ApiResponse<SerialInsertResponseData>> insertSerials(
            @Valid @RequestBody SerialInsertRequest request) {
        SerialInsertResponseData data = serialService.insertSerials(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("活動與序號已成功產生", data));
    }

    @PostMapping("/serials_additional_insert")
    public ResponseEntity<ApiResponse<SerialInsertResponseData>> additionalInsertSerials(
            @Valid @RequestBody SerialAdditionalInsertRequest request) {
        SerialInsertResponseData data = serialService.additionalInsertSerials(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("序號已成功產生", data));
    }

    @PostMapping("/serials_redeem")
    public ResponseEntity<ApiResponse<SerialRedeemResponseData>> redeemSerial(
            @Valid @RequestBody SerialRedeemRequest request) {
        SerialRedeemResponseData data = serialService.redeemSerial(request);
        return ResponseEntity.ok(ApiResponse.success("核銷成功", data));
    }

    @PostMapping("/serials_cancel")
    public ResponseEntity<SerialCancelResponse> cancelSerials(
            @Valid @RequestBody SerialCancelRequest request) {
        SerialCancelResponse response = serialService.cancelSerials(request);
        return ResponseEntity.ok(response);
    }
}
