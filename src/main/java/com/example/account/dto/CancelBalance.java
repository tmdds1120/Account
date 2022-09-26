package com.example.account.dto;

import com.example.account.app.AccountLockIdInterface;
import com.example.account.type.TransactionResultType;
import lombok.*;

import javax.validation.constraints.*;
import java.time.LocalDateTime;

//결국에는 create account 랑 비슷 한구조 ㄱㅆ지
public class CancelBalance {

    /**
     * {
     *     "transactionId" : absdfwelgfkdlds,
     *     "accountNumber": "1000000000",
     *     "amount" : 1000
     * }
     */
    @Getter
    @Setter
    @AllArgsConstructor
    public static class Request{
        @NotBlank

        private String transactionId;

        @NotBlank
        @Size(min = 9, max = 10)
        private String accountNumber;

//        @NotNull
//        @Min(0)
//        private Long initialBalance;

        @NotNull
        @Min(10)
        @Max(1000_000_000)
        private Long amount;
    }

    /**
     * {
     * "accountNumber":"1234567890",
     * "transactionResult":"S",
     * "transactionId":"c2033bb6d82a4250aecf8e27c49b63f6",
     * "amount":1000,
     * "transactedAt":"2022-06-01T23:26:14.671859"
     * }
     */
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Response implements AccountLockIdInterface {


        private Long amount;
        private String accountNumber;
        private TransactionResultType transactionResult;
        private String transactionId;
        private LocalDateTime transactedAt;

        public static Response from(TransactionDto transactionDto) {
            return Response.builder()
                    .accountNumber(transactionDto.getAccountNumber())
                    .transactionResult(transactionDto.getTransactionResultType())
                    .transactionId(transactionDto.getTransactionId())
                    .amount(transactionDto.getAmount())
                    .transactedAt(transactionDto.getTransactedAt())
                    .build();
        }
    }
}
