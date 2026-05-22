package mz.payments.payment_api;

import com.fasterxml.jackson.databind.ObjectMapper;
import mz.payments.payment_api.model.Payment;
import mz.payments.payment_api.repository.PaymentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import java.math.BigDecimal;
import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class PaymentApiApplicationTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    void setup() {
        paymentRepository.deleteAll();
    }

    @Test
    void shouldCreatePaymentSuccessfully() throws Exception {
        Map<String, Object> request = Map.of(
                "phoneNumber", "841234567",
                "amount", 150.00
        );

        mockMvc.perform(post("/payments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.status").value("PENDING"))
                .andExpect(jsonPath("$.mpesaTransactionId").exists())
                .andExpect(jsonPath("$.phoneNumber").value("841234567"));
    }

    @Test
    void shouldRejectInvalidPhoneNumber() throws Exception {
        Map<String, Object> request = Map.of(
                "phoneNumber", "123",
                "amount", 150.00
        );

        mockMvc.perform(post("/payments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.phoneNumber").value("Invalid Mozambican phone number"));
    }

    @Test
    void shouldRejectAmountBelowMinimum() throws Exception {
        Map<String, Object> request = Map.of(
                "phoneNumber", "841234567",
                "amount", 0
        );

        mockMvc.perform(post("/payments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.amount").value("Minimum amount is 1.00 MZN"));
    }

    @Test
    void shouldFetchPaymentById() throws Exception {
        Map<String, Object> request = Map.of(
                "phoneNumber", "841234567",
                "amount", 200.00
        );

        String response = mockMvc.perform(post("/payments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        String id = objectMapper.readTree(response).get("id").asText();

        mockMvc.perform(get("/payments/" + id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id))
                .andExpect(jsonPath("$.status").value("PENDING"));
    }

    @Test
    void shouldReturnNotFoundForInvalidId() throws Exception {
        mockMvc.perform(get("/payments/00000000-0000-0000-0000-000000000000"))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldUpdatePaymentStatusOnWebhook() throws Exception {
        Map<String, Object> request = Map.of(
                "phoneNumber", "841234567",
                "amount", 300.00
        );

        String response = mockMvc.perform(post("/payments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        String id = objectMapper.readTree(response).get("id").asText();
        String transactionId = objectMapper.readTree(response).get("mpesaTransactionId").asText();

        Map<String, Object> webhook = Map.of(
                "transactionId", transactionId,
                "status", "COMPLETED"
        );

        mockMvc.perform(post("/webhooks/mpesa")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(webhook)))
                .andExpect(status().isOk());

        mockMvc.perform(get("/payments/" + id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("COMPLETED"));
    }
}