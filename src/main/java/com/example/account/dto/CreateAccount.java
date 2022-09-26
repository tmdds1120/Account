package com.example.account.dto;

import lombok.*;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;

public class CreateAccount {

    @Getter@Setter
    @AllArgsConstructor
    //inner 로 새 클래스를? -> 명시적으로 보기 좋다고 ?
    public static class Request{
        @NotNull
        @Min(1)
        private Long userId;


        @NotNull
        @Min(0)
        private Long initialBalance;


    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Response{
        private Long userId;
        private String accountNumber;
        private LocalDateTime registeredAt;


        public static Response from(AccountDto accountDto){
            return Response.builder()
                    .userId(accountDto.getUserId())
                    .accountNumber(accountDto.getAccountNumber())
                    .registeredAt(accountDto.getRegisteredAt())
                    .build();
        }
    }
}
