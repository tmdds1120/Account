package com.example.account.domain;

import com.example.account.type.TransactionResultType;
import com.example.account.type.TransactionType;
import lombok.*;
import org.apache.tomcat.jni.Local;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.*;
import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
public class Transaction extends BaseEntity{

    @Enumerated(EnumType.STRING)
    private TransactionType transactionType;
    @Enumerated(EnumType.STRING)
    private TransactionResultType transactionResultType;

    // account 계좌에  트랜잭션 n개가 특정 계좌에 연결되도록
    @ManyToOne
    private Account account;
    private Long amount;
    private  Long balanceSnapshot;

    private String transactionId;
    private LocalDateTime transactedAt;


}
