package com.example.account.service;

import com.example.account.domain.Account;
import com.example.account.domain.AccountUser;
import com.example.account.dto.AccountDto;
import com.example.account.exception.AccountException;
import com.example.account.repository.AccountUserRepository;
import com.example.account.repository.AccountRepository;
import com.example.account.type.AccountStatus;
import com.example.account.type.ErrorCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class AccountServiceTest {
    @Mock
    private AccountRepository accountRepository;

    @Mock
    private AccountUserRepository accountUserRepository;

    @InjectMocks
    private AccountService accountService;


    @Test
    void createAccountSuccess(){
        //given
        AccountUser user = AccountUser.builder()
                .name("Pobi").build();
        user.setId(12L);
        given(accountUserRepository.findById(anyLong()))
                .willReturn(Optional.of(user));

        given(accountRepository.findFirstByOrderByIdDesc())
                .willReturn(Optional.of(Account.builder()
                                .accountUser(user)
                                .accountNumber("1000000012")
                        .build()));

        given(accountRepository.save(any()))
                .willReturn(Account.builder()
                                .accountUser(user)
                        .accountNumber("1000000012").build());


        // 문제 -- 원래 마지막 파람에 +1 해서 하는건데
        // 10012 -> 13 이 와야되는데 15로 바꿔도 성공이 되는데 이거를 제대로 작동한다는걸 증명 할려면 ?

        ArgumentCaptor<Account> captor = ArgumentCaptor.forClass(Account.class);



        //when

        AccountDto accountDto = accountService.createAccount(1L, 1000L);

        //then
        // 넘어가는 거에서 캡쳐하는 거
        verify(accountRepository, times(1)).save(captor.capture());
        assertEquals(12L,accountDto.getUserId());
        assertEquals("1000000013",captor.getValue().getAccountNumber());
    }
    @Test
    @DisplayName("delete success!!")
    void deleteAccountSuccess(){
        //given
        AccountUser user = AccountUser.builder()
                .name("Pobi").build();

        user.setId(12L);
        given(accountUserRepository.findById(anyLong()))
                .willReturn(Optional.of(user));

        given(accountRepository.findByAccountNumber(anyString()))
                .willReturn(Optional.of(Account.builder()
                        .accountUser(user)
                                .balance(0L)
                        .accountNumber("1000000012")
                        .build()));




        // 문제 -- 원래 마지막 파람에 +1 해서 하는건데
        // 10012 -> 13 이 와야되는데 15로 바꿔도 성공이 되는데 이거를 제대로 작동한다는걸 증명 할려면 ?

        ArgumentCaptor<Account> captor = ArgumentCaptor.forClass(Account.class);



        //when

        AccountDto accountDto = accountService.deleteAccount(1L, "1234567890");

        //then
        // 넘어가는 거에서 캡쳐하는 거
        verify(accountRepository, times(1)).save(captor.capture());
        assertEquals(12L,accountDto.getUserId());
        assertEquals("1000000012",captor.getValue().getAccountNumber());
        assertEquals(AccountStatus.UNREGISTERED, captor.getValue().getAccountStatus());
    }

    @Test
    @DisplayName("not found match Account- > fail to del account")
    void deleteAccount_UserNotFound(){
        //given
        given(accountUserRepository.findById(anyLong()))
                .willReturn(Optional.empty());
        //when
        AccountException exception = assertThrows(AccountException.class,
                () -> accountService.deleteAccount(1L, "1234567890"));
        //then
        assertEquals(ErrorCode.USER_NOT_FOUND, exception.getErrorCode());
    }

    @Test
    @DisplayName("not found account -> fail")
    void deleteAccount_AccountNotFound(){
        //given
        AccountUser user = AccountUser.builder()
                .name("Pobi")
                .build();
        user.setId(12L);
        given(accountUserRepository.findById(anyLong()))
                .willReturn(Optional.of(user));
        given(accountRepository.findByAccountNumber(anyString()))
                .willReturn(Optional.empty());
        //when
        AccountException exception = assertThrows(AccountException.class,
                ()-> accountService.deleteAccount(1L, "1234567890"));

        //then
        assertEquals(ErrorCode.ACCOUNT_NOT_FOUND, exception.getErrorCode());
    }

    @Test
    @DisplayName("accountUser MisMatch")
    void deleteAccountFailed_userMisMatch(){
        //given
        AccountUser pobi=AccountUser.builder()
                .name("Pobi")
                .build();
        pobi.setId(12L);
        AccountUser harry = AccountUser.builder()

                .name("harry")
                .build();
        harry.setId(13L);
        given(accountUserRepository.findById(anyLong()))
                .willReturn(Optional.of(pobi));

        given(accountRepository.findByAccountNumber(anyString()))
                .willReturn(Optional.of(Account.builder()
                                .accountUser(harry)
                                .balance(0L)
                                .accountNumber("1000000012")
                        .build()));

        AccountException exception = assertThrows(AccountException.class,
                () -> accountService.deleteAccount(1L,"1234567890"));
        //when

        assertEquals(ErrorCode.USER_ACCOUNT_UN_MATCH, exception.getErrorCode());
        //then
    }

    @Test
    @DisplayName("Balance must empty to delete")
    void balance_Not_Empty(){
        //given
        AccountUser pobi=AccountUser.builder()
                .name("Pobi")
                .build();
        pobi.setId(13L);


        given(accountUserRepository.findById(anyLong()))
                .willReturn(Optional.of(pobi));

        given(accountRepository.findByAccountNumber(anyString()))
                .willReturn(Optional.of(Account.builder()
                        .accountUser(pobi)
                        .balance(100L)
                        .accountNumber("1000000012")
                        .build()));

        AccountException exception = assertThrows(AccountException.class,
                () -> accountService.deleteAccount(1L,"1234567890"));
        //when

        assertEquals(ErrorCode.BALANCE_NOT_EMPTY, exception.getErrorCode());
        //then
    }

    @Test
    void createFirstSuccess(){
        //given
        AccountUser user = AccountUser.builder()
                .name("Pobi").build();

                        user.setId(15L);

        given(accountUserRepository.findById(anyLong()))
                .willReturn(Optional.of(user));

        given(accountRepository.findFirstByOrderByIdDesc())
                .willReturn(Optional.empty());

        given(accountRepository.save(any()))
                .willReturn(Account.builder()
                        .accountUser(user)
                        .accountNumber("1000000012").build());


        // 문제 -- 원래 마지막 파람에 +1 해서 하는건데
        // 10012 -> 13 이 와야되는데 15로 바꿔도 성공이 되는데 이거를 제대로 작동한다는걸 증명 할려면 ?

        ArgumentCaptor<Account> captor = ArgumentCaptor.forClass(Account.class);



        //when

        AccountDto accountDto = accountService.createAccount(1L, 1000L);

        //then
        // 넘어가는 거에서 캡쳐하는 거
        verify(accountRepository, times(1)).save(captor.capture());
        assertEquals(15L,accountDto.getUserId());
        assertEquals("1000000000",captor.getValue().getAccountNumber());
    }

    @Test
    @DisplayName("already unregistered")
    void delAccountFailed_Already_Unregistered(){
        //given
        AccountUser pobi = AccountUser.builder()
                .name("Pobi")
                .build();
        pobi.setId(12L);

        given(accountUserRepository.findById(anyLong()))
                .willReturn(Optional.of(pobi));

        given(accountRepository.findByAccountNumber(anyString()))
                .willReturn(Optional.of(Account.builder()
                                .accountUser(pobi)
                                .accountStatus(AccountStatus.UNREGISTERED)
                                .balance(0L)
                                .accountNumber("1000000012")
                                .build()));
        //when
        AccountException exception = assertThrows(AccountException.class,
        () -> accountService.deleteAccount(1L,"1234567890"));
        //then

        assertEquals(ErrorCode.ACCOUNT_ALREADY_UNREGISTERED,exception.getErrorCode());
    }



    @Test
    @DisplayName("not found user ==> fail create account")
    void createAccount_UserNotFound(){
        //given

        given(accountUserRepository.findById(anyLong()))
                .willReturn(Optional.empty());





        //when
         AccountException exception=assertThrows(AccountException.class,
                 ()-> accountService.createAccount(1L, 1000L) );


        //then

        assertEquals(ErrorCode.USER_NOT_FOUND, exception.getErrorCode());


    }
    @Test
    @DisplayName("user per max account is 10")
    void createAccount_maxAccountIs10(){
        //given
        AccountUser user =AccountUser.builder()
                .name("Pobi")
                .build();
                user.setId(15L);

        given(accountUserRepository.findById(anyLong()))
                .willReturn(Optional.of(user));

        given(accountRepository.countByAccountUser(any()))
                .willReturn(10);
        //when

        AccountException exception = assertThrows(AccountException.class,
                () -> accountService.createAccount(1L, 1000L));

        //then

        assertEquals(ErrorCode.MAX_ACCOUNT_PER_USER_10, exception.getErrorCode());
    }

    @Test
    void successGetAccountsByUserId(){
        //given
        AccountUser pobi =AccountUser.builder()
                .name("Pobi")
                .build();
                pobi.setId(15L);

        List<Account> accounts = Arrays.asList(
                Account.builder()
                        .accountUser(pobi)
                        .accountNumber("1111111111")
                        .balance(1000L)
                        .build(),
                Account.builder()
                        .accountUser(pobi)
                        .accountNumber("2222222222")
                        .balance(2000L)
                        .build(),
                Account.builder()
                        .accountUser(pobi)
                        .accountNumber("3333333333")
                        .balance(3000L)
                        .build()
        );

        given(accountUserRepository.findById(anyLong()))
                .willReturn(Optional.of(pobi));
        given(accountRepository.findByAccountUser(any()))
                .willReturn(accounts);
        //when

        List<AccountDto> accountDtos = accountService.getAccountByUserId(1L);
        //then
        // account 타입의  리스트인 dtos 의 길이가 3
        assertEquals(3, accountDtos.size());
        assertEquals("1111111111", accountDtos.get(0).getAccountNumber());
        assertEquals(1000, accountDtos.get(0).getBalance());
        assertEquals("2222222222", accountDtos.get(1).getAccountNumber());
        assertEquals(2000, accountDtos.get(1).getBalance());
        assertEquals("3333333333", accountDtos.get(2).getAccountNumber());
        assertEquals(3000, accountDtos.get(2).getBalance());
    }

    @Test
    void failedToGetAccounts(){
        //given
        // 아이디 요청시 만약 빈값이 온다면
        given(accountUserRepository.findById(anyLong()))
                .willReturn(Optional.empty());
        //when
        // 예외객체를 만들어서
        AccountException exception =assertThrows(AccountException.class,
                ()-> accountService.getAccountByUserId(1L));
        //then
        // 유저없음과 , 자동생성된 에러코드를 비교해서 제대로 에러코드가 생긴지 확인
        assertEquals(ErrorCode.USER_NOT_FOUND, exception.getErrorCode());


    }
}