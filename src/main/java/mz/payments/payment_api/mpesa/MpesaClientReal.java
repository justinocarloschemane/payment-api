package mz.payments.payment_api.mpesa;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;
import java.math.BigDecimal;
import java.util.Base64;
import java.util.Map;

// @Component  ← uncomment this when you have real credentials
public class MpesaClientReal implements MpesaClient {

    @Value("${mpesa.consumer-key}")
    private String consumerKey;

    @Value("${mpesa.consumer-secret}")
    private String consumerSecret;

    @Value("${mpesa.service-provider-code}")
    private String serviceProviderCode;

    private final RestTemplate restTemplate = new RestTemplate();

    @Override
    public MpesaResponse initiatePayment(String phoneNumber, BigDecimal amount) {
        String token = getAccessToken();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(token);

        Map<String, Object> body = Map.of(
                "input_TransactionReference", "REF-" + System.currentTimeMillis(),
                "input_CustomerMSISDN", "258" + phoneNumber,
                "input_Amount", amount.toPlainString(),
                "input_ThirdPartyReference", "PAY-" + System.currentTimeMillis(),
                "input_ServiceProviderCode", serviceProviderCode
        );

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);

        ResponseEntity<Map> response = restTemplate.postForEntity(
                "https://api.sandbox.vm.co.mz/ipg/v1x/c2bPayment/singleStage/",
                entity,
                Map.class
        );

        String transactionId = (String) response.getBody().get("output_TransactionID");
        return new MpesaResponse(transactionId, "ACCEPTED", "Payment initiated");
    }

    private String getAccessToken() {
        String credentials = Base64.getEncoder()
                .encodeToString((consumerKey + ":" + consumerSecret).getBytes());

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Basic " + credentials);

        HttpEntity<Void> entity = new HttpEntity<>(headers);

        ResponseEntity<Map> response = restTemplate.exchange(
                "https://api.sandbox.vm.co.mz/oauth/v1/generate?grant_type=client_credentials",
                HttpMethod.GET,
                entity,
                Map.class
        );

        return (String) response.getBody().get("access_token");
    }
}