package com.bookstore.orderservice.order.web;

import com.bookstore.orderservice.order.domain.VNPayService;
import com.bookstore.orderservice.order.web.dto.VNPayDto;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

@RestController
@RequestMapping(value = "/payment", produces = "application/json")
public class VnpayController {

    private final VNPayService vnPayService;
    private final ObjectMapper objectMapper;

    public VnpayController(VNPayService vnPayService, ObjectMapper objectMapper) {
        this.vnPayService = vnPayService;
        this.objectMapper = objectMapper;
    }

    @PostMapping("/vnpay")
    public ResponseEntity<?> pay(HttpServletRequest req, @RequestParam(value = "price") Long price) throws JsonProcessingException {
        return ResponseEntity.ok(
                objectMapper.writeValueAsString(Map.of("paymentUrl", vnPayService.generatePaymentUrl(req, price)))
        );
    }

    @GetMapping("/vnpay-return")
    public VNPayDto payCallbackHandler(HttpServletRequest request) {
        String status = request.getParameter("vnp_ResponseCode");
        if (status.equals("00")) {
            return new VNPayDto(0, "Success", "");
        } else {
            return new VNPayDto(400, "Bad request", null);
        }
    }

}
