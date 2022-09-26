package com.example.account.exception;

import com.example.account.type.ErrorCode;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AccountException extends RuntimeException{

    private ErrorCode errorCode;
    private  String errorMessage;


    // errorCode를 받에서 에러코드와 세부사항을 생성자로 주입
    public AccountException(ErrorCode errorCode){
        this.errorCode = errorCode;
        this.errorMessage = errorCode.getDescription();
    }


}
