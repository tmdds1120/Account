package com.example.account.domain;

import com.example.account.exception.AccountException;
import com.example.account.type.AccountStatus;
import com.example.account.type.ErrorCode;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.*;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity

public class Account extends BaseEntity{


    @ManyToOne
    private AccountUser accountUser;
    private String accountNumber;

    @Enumerated(EnumType.STRING)
    private AccountStatus accountStatus;
    private Long balance;


    private LocalDateTime registerAt;
    private LocalDateTime unRegisteredAt;





    public void useBalance(Long amount){
        if (amount> balance){
            throw new AccountException(ErrorCode.AMOUNT_EXCEED_BALANCE);

        }
        balance-= amount;
    }

    public void cancelBalance(Long amount){
        //0 보다 작은 경우
        if (amount<0){
            throw new AccountException(ErrorCode.INVALID_REQUEST);
        }
        balance+=amount;
    }
}
