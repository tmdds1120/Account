package com.example.account.service;

import com.example.account.domain.Account;
import com.example.account.domain.AccountUser;
import com.example.account.domain.Transaction;
import com.example.account.dto.TransactionDto;
import com.example.account.exception.AccountException;
import com.example.account.repository.AccountRepository;
import com.example.account.repository.AccountUserRepository;
import com.example.account.repository.TransactionRepository;
import com.example.account.type.AccountStatus;
import com.example.account.type.ErrorCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static com.example.account.type.AccountStatus.*;
import static com.example.account.type.TransactionResultType.F;
import static com.example.account.type.TransactionResultType.S;
import static com.example.account.type.TransactionType.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;


@ExtendWith(MockitoExtension.class)
class TransactionServiceTest {
    public static final long USE_AMOUNT = 200L;
    public static final long CANCEL_AMOUNT = 200L;
    @Mock
    private AccountRepository accountRepository;

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private AccountUserRepository accountUserRepository;

    @InjectMocks
    private TransactionService transactionService;

    @Test
    void successUseBalance(){
        //given
        AccountUser user = AccountUser.builder()
                .name("Pobi")
                .build();
                user.setId(12L);

        given(accountUserRepository.findById(anyLong()))
                .willReturn(Optional.of(user));

        Account account = Account.builder()
                .accountUser(user)
                .accountStatus(IN_USE)
                .balance(10000L)
                .accountNumber("1000000012")
                .build();
        given(accountRepository.findByAccountNumber(anyString()))
                .willReturn(Optional.of(account));

        given(transactionRepository.save(any()))
                .willReturn(Transaction.builder()
                        .account(account)
                        .transactionType(USE)
                        .transactionResultType(S)
                        .transactionId("transactionId")
                        .transactedAt(LocalDateTime.now())
                        .amount(1000L)
                        .balanceSnapshot(9000L)
                        .build());
        ArgumentCaptor<Transaction> captor = ArgumentCaptor.forClass(Transaction.class);

        //when
        TransactionDto transactionDto =transactionService.useBalance(1L,"1000000000"
                , USE_AMOUNT);
        //then
        // 결과로 나오는게 transactionDto
        verify(transactionRepository,times(1)).save(captor.capture());
        assertEquals(USE_AMOUNT, captor.getValue().getAmount());
        assertEquals(9800L, captor.getValue().getBalanceSnapshot());
        assertEquals(S,transactionDto.getTransactionResultType());
        assertEquals(USE,transactionDto.getTransactionType());
        assertEquals(9000L,transactionDto.getBalanceSnapshot());
        assertEquals(1000L,transactionDto.getAmount());
    }

    // 해당유저 x --> 잔액사용 x
    @Test
    @DisplayName("not found user ==> can't use last money")
    void useBalance_UserNotFound(){
        //given

        given(accountUserRepository.findById(anyLong()))
                .willReturn(Optional.empty());


        //when
        AccountException exception=assertThrows(AccountException.class,
                ()-> transactionService.useBalance(1L, "1000000000",1000L) );


        //then

        assertEquals(ErrorCode.USER_NOT_FOUND, exception.getErrorCode());


    }
    // 계좌 없음
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
                ()-> transactionService.useBalance(1L, "1000000000",1000L));

        //then
        assertEquals(ErrorCode.ACCOUNT_NOT_FOUND, exception.getErrorCode());
    }

    //계좌의 소유주가 다름 -> 잔액 사용 실패
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

        // 유저조회시에는 pobi
        given(accountUserRepository.findById(anyLong()))
                .willReturn(Optional.of(pobi));
        // account 조회시에는 harry
        given(accountRepository.findByAccountNumber(anyString()))
                .willReturn(Optional.of(Account.builder()
                        .accountUser(harry)
                        .balance(0L)
                        .accountNumber("1000000012")
                        .build()));

        AccountException exception = assertThrows(AccountException.class,
                () -> transactionService.useBalance(1L,"1234567890",1000L));

        //when

        assertEquals(ErrorCode.USER_ACCOUNT_UN_MATCH, exception.getErrorCode());
        //then
    }

    //이미 해지된 계좌는 사용불가
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
                () -> transactionService.useBalance(1L,"1234567890",1000L));
        //then

        assertEquals(ErrorCode.ACCOUNT_ALREADY_UNREGISTERED,exception.getErrorCode());
    }


    // 거래금액 > 잔액
    @Test
    void exceedAmount_UseBalance(){
        //given
        AccountUser user = AccountUser.builder()
                .name("Pobi")
                .build();
        user.setId(12L);



        Account account = Account.builder()
                .accountUser(user)
                .accountStatus(IN_USE)
                .balance(100L)
                .accountNumber("1000000012")
                .build();
        given(accountUserRepository.findById(anyLong()))
                .willReturn(Optional.of(user));
        given(accountRepository.findByAccountNumber(anyString()))
                .willReturn(Optional.of(account));




        //when

        AccountException exception = assertThrows(AccountException.class,
                () -> transactionService.useBalance(1L,"1234567890",1000L));
        //then
        assertEquals(ErrorCode.AMOUNT_EXCEED_BALANCE,exception.getErrorCode());

        // 결과로 나오는게 transactionDto
        verify(transactionRepository,times(0)).save(any());


    }

    @Test
    @DisplayName("실패 트랜잭션 저장")
    void saveFailedUseTransaction(){
        //given
        AccountUser user = AccountUser.builder()
                .name("Pobi")
                .build();
        user.setId(12L);

        Account account = Account.builder()
                .accountUser(user)
                .accountStatus(IN_USE)
                .balance(10000L)
                .accountNumber("1000000012")
                .build();

//        given(accountUserRepository.findById(anyLong()))
//                .willReturn(Optional.of(user));

        given(accountRepository.findByAccountNumber(anyString()))
                .willReturn(Optional.of(account));

        given(transactionRepository.save(any()))
                .willReturn(Transaction.builder()
                        .account(account)
                        .transactionType(USE)
                        .transactionResultType(S)
                        .transactionId("transactionId")
                        .transactedAt(LocalDateTime.now())
                        .amount(1000L)
                        .balanceSnapshot(9000L)
                        .build());

        ArgumentCaptor<Transaction> captor = ArgumentCaptor.forClass(Transaction.class);


        //when
        transactionService.saveFailedUseTransaction("1000000000",USE_AMOUNT);
        //then
        verify(transactionRepository,times(1)).save(captor.capture());
        assertEquals(USE_AMOUNT, captor.getValue().getAmount());
        assertEquals(10000L, captor.getValue().getBalanceSnapshot());
        assertEquals(F, captor.getValue().getTransactionResultType());


    }

    @Test
    void successCancelBalance(){
        //given
        AccountUser user = AccountUser.builder()
                .name("Pobi")
                .build();
        user.setId(12L);



        Account account = Account.builder()
                .accountUser(user)
                .accountStatus(IN_USE)
                .balance(10000L)
                .accountNumber("1000000012")
                .build();
        // 원래의 거래를 찾는?
        Transaction transaction = Transaction.builder()
                .account(account)
                .transactionType(USE)
                .transactionResultType(S)
                .transactionId("transactionId")
                .transactedAt(LocalDateTime.now())
                .amount(CANCEL_AMOUNT)
                .balanceSnapshot(9000L)
                .build();
        // 거래 조회

        given(transactionRepository.findByTransactionId(anyString()))
                .willReturn(Optional.of(transaction));
        //위의 account
        given(accountRepository.findByAccountNumber(anyString()))
                .willReturn(Optional.of(account));
        given(transactionRepository.save(any()))
                .willReturn(Transaction.builder()
                        .account(account)
                        .transactionType(CANCEL)
                        .transactionResultType(S)
                        .transactionId("transactionIdForCancel")
                        .transactedAt(LocalDateTime.now())
                        .amount(CANCEL_AMOUNT)
                        //취소 됬으니 10000
                        .balanceSnapshot(10000L)
                        .build());
        ArgumentCaptor<Transaction> captor = ArgumentCaptor.forClass(Transaction.class);

        //when
        TransactionDto transactionDto =transactionService.cancelBalance("transactionId","1000000000"
                , CANCEL_AMOUNT);
        //then
        // 결과로 나오는게 transactionDto
        verify(transactionRepository,times(1)).save(captor.capture());
        assertEquals(CANCEL_AMOUNT, captor.getValue().getAmount());
        assertEquals(10000L+CANCEL_AMOUNT, captor.getValue().getBalanceSnapshot());
        assertEquals(S,transactionDto.getTransactionResultType());
        assertEquals(CANCEL,transactionDto.getTransactionType());
        assertEquals(10000L,transactionDto.getBalanceSnapshot());
        assertEquals(CANCEL_AMOUNT,transactionDto.getAmount());
    }

    @Test
    @DisplayName("not found account ->  cancel fail")
    void cancelTransaction_AccountNotFound(){
        //given


        given(transactionRepository.findByTransactionId(anyString()))
                .willReturn(Optional.of(
                        Transaction.builder().build()
                ));
        given(accountRepository.findByAccountNumber(anyString()))
                .willReturn(Optional.empty());
        //when
        AccountException exception = assertThrows(AccountException.class,
                ()-> transactionService.cancelBalance("transactionId", "1000000000",1000L));

        //then
        assertEquals(ErrorCode.ACCOUNT_NOT_FOUND, exception.getErrorCode());
    }
    @Test
    @DisplayName("not found transaction ->  can't use ")
    void cancelTransaction_TransactionNotFound(){
        //given



        given(transactionRepository.findByTransactionId(anyString()))
                .willReturn(Optional.empty());

        //when
        AccountException exception = assertThrows(AccountException.class,
                ()-> transactionService.cancelBalance("transactionId", "1000000000",1000L));

        //then
        assertEquals(ErrorCode.TRANSACTION_NOT_FOUND, exception.getErrorCode());
    }
    @Test
    @DisplayName(" transaction, account unmatch")
    void cancelTransaction_TransactionAccountUnMatch(){
        //given

        AccountUser user = AccountUser.builder()
                .name("Pobi")
                .build();
        user.setId(12L);



        Account account = Account.builder()
                .accountUser(user)
                .accountStatus(IN_USE)
                .balance(10000L)
                .accountNumber("1000000012")
                .build();
        account.setId(1L);

        Account accountNotUse = Account.builder()
                .accountUser(user)
                .accountStatus(IN_USE)
                .balance(10000L)
                .accountNumber("1000000013")
                .build();
        accountNotUse.setId(2L);


        Transaction transaction = Transaction.builder()
                .account(account)
                .transactionType(USE)
                .transactionResultType(S)
                .transactionId("transactionId")
                .transactedAt(LocalDateTime.now())
                .amount(CANCEL_AMOUNT)
                .balanceSnapshot(9000L)
                .build();


        given(transactionRepository.findByTransactionId(anyString()))
                .willReturn(Optional.of(transaction));

        given(accountRepository.findByAccountNumber(anyString()))
                .willReturn(Optional.of(accountNotUse));
        //when
        AccountException exception = assertThrows(AccountException.class,
                ()-> transactionService.cancelBalance(
                        "transactionId",
                        "1000000000",
                        CANCEL_AMOUNT));

        //then
        assertEquals(ErrorCode.TRANSACTION_ACCOUNT_UN_MATCH, exception.getErrorCode());
    }
    @Test
    @DisplayName("cancelMoney,transaction UnMatch")
    void cancelTransaction_CancelMustFully(){
        //given

        AccountUser user = AccountUser.builder()
                .name("Pobi")
                .build();

        user.setId(12L);


        Account account = Account.builder()
                .accountUser(user)
                .accountStatus(IN_USE)
                .balance(10000L)
                .accountNumber("1000000012")
                .build();
        account.setId(1L);


        Transaction transaction = Transaction.builder()
                .account(account)
                .transactionType(USE)
                .transactionResultType(S)
                .transactionId("transactionId")
                .transactedAt(LocalDateTime.now())
                .amount(CANCEL_AMOUNT+1000L)
                .balanceSnapshot(9000L)
                .build();


        given(transactionRepository.findByTransactionId(anyString()))
                .willReturn(Optional.of(transaction));

        given(accountRepository.findByAccountNumber(anyString()))
                .willReturn(Optional.of(account));
        //when
        AccountException exception = assertThrows(AccountException.class,
                ()-> transactionService.cancelBalance(
                        "transactionId",
                        "1000000000",
                        CANCEL_AMOUNT));

        //then
        assertEquals(ErrorCode.CANCEL_MUST_FULLY, exception.getErrorCode());
    }

    //too old to cancel
    @Test
    @DisplayName("cancel can't access 1 year")
    void cancelTransaction_TooOldOrder(){
        //given

        AccountUser user = AccountUser.builder()
                .name("Pobi")
                .build();
        user.setId(12L);


        Account account = Account.builder()
                .accountUser(user)
                .accountStatus(IN_USE)
                .balance(10000L)
                .accountNumber("1000000012")
                .build();
        account.setId(1L);



        Transaction transaction = Transaction.builder()
                .account(account)
                .transactionType(USE)
                .transactionResultType(S)
                .transactionId("transactionId")
                .transactedAt(LocalDateTime.now().minusYears(1).minusDays(1))
                .amount(CANCEL_AMOUNT)
                .balanceSnapshot(9000L)
                .build();


        given(transactionRepository.findByTransactionId(anyString()))
                .willReturn(Optional.of(transaction));

        given(accountRepository.findByAccountNumber(anyString()))
                .willReturn(Optional.of(account));
        //when
        AccountException exception = assertThrows(AccountException.class,
                ()-> transactionService.cancelBalance(
                        "transactionId",
                        "1000000000",
                        CANCEL_AMOUNT));

        //then
        assertEquals(ErrorCode.TOO_OLD_ORDER_TO_CANCEL, exception.getErrorCode());
    }

    @Test
    void successQueryTransaction(){
        //given
        AccountUser user = AccountUser.builder()
                .name("Pobi")
                .build();
        user.setId(12L);

        Account account = Account.builder()
                .accountUser(user)
                .accountStatus(IN_USE)
                .balance(10000L)
                .accountNumber("1000000012")
                .build();

        account.setId(1L);



        Transaction transaction = Transaction.builder()
                .account(account)
                .transactionType(USE)
                .transactionResultType(S)
                .transactionId("transactionId")
                .transactedAt(LocalDateTime.now().minusYears(1).minusDays(1))
                .amount(CANCEL_AMOUNT)
                .balanceSnapshot(9000L)
                .build();

        given(transactionRepository.findByTransactionId(anyString()))
                .willReturn(Optional.of(transaction));


        //when
        TransactionDto transactionDto = transactionService.queryTransaction("trxId");


        //then
        assertEquals(USE, transactionDto.getTransactionType());
        assertEquals(S,transactionDto.getTransactionResultType());
        assertEquals(S,transactionDto.getTransactionResultType());
        assertEquals("transactionId",transactionDto.getTransactionId());
    }


    @Test
    @DisplayName("원거래 없음 - 거래 조회 실패")
    void queryTransaction_TransactionNotFound(){
        //given



        given(transactionRepository.findByTransactionId(anyString()))
                .willReturn(Optional.empty());

        //when
        AccountException exception = assertThrows(AccountException.class,
                ()-> transactionService.queryTransaction("transactionId"));

        //then
        assertEquals(ErrorCode.TRANSACTION_NOT_FOUND, exception.getErrorCode());
    }
}