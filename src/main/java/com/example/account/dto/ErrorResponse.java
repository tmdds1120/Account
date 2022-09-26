package com.example.account.dto;

import com.example.account.type.ErrorCode;
import lombok.*;


@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
// 에러응답
public class ErrorResponse {
    private ErrorCode errorCode;
    private String errorMessage;
}
