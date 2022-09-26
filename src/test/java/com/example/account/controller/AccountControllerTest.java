package com.example.account.controller;

import com.example.account.domain.Account;
import com.example.account.domain.AccountUser;
import com.example.account.dto.AccountDto;
import com.example.account.dto.CreateAccount;
import com.example.account.dto.DeleteAccount;
import com.example.account.exception.AccountException;
import com.example.account.type.AccountStatus;
import com.example.account.service.AccountService;

import com.example.account.type.ErrorCode;
import com.fasterxml.jackson.core.JacksonException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AccountController.class)
class AccountControllerTest {
    @MockBean
    private AccountService accountService;


    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void successCreateAccount() throws Exception {
        //given
        //createAccount 출력
        given(accountService.createAccount(anyLong(),anyLong()))
                .willReturn(AccountDto.builder()
                        .userId(1L)
                        .accountNumber("1234567890")
                        .registeredAt(LocalDateTime.now())
                        .unRegisteredAt(LocalDateTime.now())
                        .build());


        //when
        //then

        mockMvc.perform(post("/account")
                .contentType(MediaType.APPLICATION_JSON)
                // content 값 넣어주기 userid, init 쏘기
                //1. 자바문 그대로
                // 2. 자바내장된 오브젝트 매퍼이용 얘는 뭐시기지
                .content(objectMapper.writeValueAsString(
                        new CreateAccount.Request(3333L,1111L)
                ))).andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value(1))
                .andExpect(jsonPath("$.accountNumber").value("1234567890"))
                .andDo(print());

                //jsonpath 응답 바디에오는 제이슨 경로의 받았던 데이터
    }
    @Test
    void successDeleteAccount() throws Exception {
        //given
        // 유저 아이디 ,  accountNumber(String)
        given(accountService.deleteAccount(anyLong(), anyString()))
                .willReturn(AccountDto.builder()
                        .userId(1L)
                        .accountNumber("1234567890")
                        .registeredAt(LocalDateTime.now())
                        .unRegisteredAt(LocalDateTime.now())
                        .build());
        //when

        //then
        mockMvc.perform(delete("/account")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                // 어떤 값이 들어가든 동일한값이 나와서 의미가 없다는게 무슨 뜻이지?
                                new DeleteAccount.Request(3333L, "0987654321")
                        ))).andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value(1))
                .andExpect(jsonPath("$.accountNumber").value("1234567890"))
                .andDo(print());

    }


    @Test
    void successGetAccount() throws Exception {
        //given
        given(accountService.getAccount(anyLong()))
                .willReturn(Account.builder()
                        .accountNumber("3456")
                        .accountStatus(AccountStatus.IN_USE)
                        .build());

        //when
        //then
        mockMvc.perform(get("/account/876"))
                .andDo(print())
                .andExpect(jsonPath("$.accountNumber").value("3456"))
                .andExpect(jsonPath("$.accountStatus").value("IN_USE"))
                .andExpect(status().isOk());
    }

    @Test
    void successGetAccountByAccount() throws  Exception{
        //given
        List<AccountDto> accountDtos =
                Arrays.asList(AccountDto.builder()
                                .accountNumber("1234567890")
                                .balance(1000L).build(),
                        AccountDto.builder()
                                .accountNumber("111111111")
                                .balance(2000L)
                                .build(),

                        AccountDto.builder()
                                .accountNumber("222222222")
                                .balance(3000L)
                                .build()
                        );

        given(accountService.getAccountByUserId(anyLong()))
                //account dto
                .willReturn(accountDtos);
        //when
        //then
        mockMvc.perform(get("/account?user_id=1"))
                .andDo(print())
                .andExpect(jsonPath("$.[0].accountNumber").value("1234567890"))
                .andExpect(jsonPath("$.[0].balance").value(1000))
                .andExpect(jsonPath("$.[1].accountNumber").value("111111111"))
                .andExpect(jsonPath("$.[1].balance").value(2000))
                .andExpect(jsonPath("$.[2].accountNumber").value("222222222"))
                .andExpect(jsonPath("$.[2].balance").value(3000));
//                .andExpect(jsonPath("$.[0].balance").value("1000"))

    }

    @Test
    void failGetAccount() throws Exception {
        //given
        //이 요청이 예외를 발생
        given(accountService.getAccount(anyLong()))
                .willThrow(new AccountException(ErrorCode.ACCOUNT_NOT_FOUND));
        //when
        //then
        mockMvc.perform(get("/account/876"))
                .andDo(print())
                .andExpect(jsonPath("$.errorCode").value("ACCOUNT_NOT_FOUND"))
                .andExpect(jsonPath("$.errorMessage").value("존재하는 계좌가 없습니다"))
                .andExpect(status().isOk());
    }
}