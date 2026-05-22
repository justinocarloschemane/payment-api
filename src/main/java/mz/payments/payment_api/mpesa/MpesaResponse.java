package mz.payments.payment_api.mpesa;

public class MpesaResponse {

    private String transactionId;
    private String status;
    private String message;

    public MpesaResponse(String transactionId, String status, String message) {
        this.transactionId = transactionId;
        this.status = status;
        this.message = message;
    }

    public String getTransactionId() { return transactionId; }
    public String getStatus() { return status; }
    public String getMessage() { return message; }
}