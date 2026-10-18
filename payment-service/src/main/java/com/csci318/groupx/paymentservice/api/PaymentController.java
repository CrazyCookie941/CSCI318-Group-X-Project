package com.csci318.groupx.paymentservice.api;

import com.csci318.groupx.paymentservice.service.PaymentService;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/payments")
public class PaymentController {
    private final PaymentService paymentService;

    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @PostMapping("/simulate")
    public PaymentResponse simulate(@RequestBody PaymentRequest request) {
        return paymentService.simulate(request);
    }

    @GetMapping("/{paymentId}")
    public PaymentResponse getById(@PathVariable Long paymentId) {
        return paymentService.getById(paymentId);
    }

    @GetMapping
    public List<PaymentResponse> getByAccountId(@RequestParam Long accountId) {
        return paymentService.getByAccountId(accountId);
    }

    /** Internal, simulated-only refund endpoint. Requires a shared service token. */
    @PostMapping("/events/{eventId}/refunds")
    public PaymentService.RefundSummary refundEvent(
            @PathVariable Long eventId,
            @RequestHeader(value = "X-Internal-Token", required = false) String token) {
        return paymentService.refundEvent(eventId, token);
    }
}
