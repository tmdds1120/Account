package com.example.account.dto;

import lombok.*;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.time.LocalDateTime;

public class DeleteAccount {

    @Getter@Setter
    @AllArgsConstructor
    //inner 로 새 클래스를? -> 명시적으로 보기 좋다고 ?
    public static class Request{
        //검증
        @NotNull
        @Min(1)
        private Long userId;


        @NotBlank
        @Size(min =9, max = 10) // 문자열의 길이를 확인해줌
        private String accountNumber;

//        private String accountNumber;



    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Response{
        private Long userId;
        private String accountNumber;
        private LocalDateTime unRegisteredAt;
//        private Long initialBalance;

        public static Response from(AccountDto accountDto){
            return Response.builder()
                    .userId(accountDto.getUserId())
                    .accountNumber(accountDto.getAccountNumber())
                    .unRegisteredAt(accountDto.getUnRegisteredAt())
                    .build();
        }
    }
}
