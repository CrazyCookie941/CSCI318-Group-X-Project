package com.csci318.groupx.paymentservice.api;

import com.csci318.groupx.paymentservice.service.PaymentService;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/payments")
public class PaymentController {

    private final PaymentService paymentService;

    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @PostMapping("/simulate")
    public PaymentResponse simulate(
            @RequestBody PaymentRequest request
    ) {
        return paymentService.simulate(request);
    }

    @GetMapping("/{paymentId}")
    public PaymentResponse getById(
            @PathVariable Long paymentId
    ) {
        return paymentService.getById(paymentId);
    }
}