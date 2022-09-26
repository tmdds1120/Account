package com.example.account.dto;

import com.example.account.domain.Account;
import lombok.*;

import java.time.LocalDateTime;


@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AccountDto {
    //엔티티 클래스와 비슷한데 단순/
    private Long userId;
    private String accountNumber;
    private Long balance;
    private LocalDateTime registeredAt;
    private LocalDateTime unRegisteredAt;


    //생성자를 통해서?
    public static AccountDto fromEntity(Account account){
        return AccountDto.builder()
                .userId(account.getAccountUser().getId())
                .accountNumber(account.getAccountNumber())
                .registeredAt(account.getRegisterAt())
                .balance(account.getBalance())
                .unRegisteredAt(account.getUnRegisteredAt())
                .build();
    }

}
