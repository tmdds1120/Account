package com.example.account.service;

import com.example.account.domain.Account;
import com.example.account.domain.AccountUser;
import com.example.account.dto.AccountDto;
import com.example.account.exception.AccountException;
import com.example.account.repository.AccountRepository;
import com.example.account.repository.AccountUserRepository;
import com.example.account.type.AccountStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static com.example.account.type.AccountStatus.IN_USE;
import static com.example.account.type.ErrorCode.*;

@Service
@RequiredArgsConstructor
public class AccountService {
    private final AccountRepository accountRepository;
    private final AccountUserRepository accountUserRepository;

    /**
     *
     *   사용자가 있는지 조회, --> 계좌의 번호를 생성
     *
     *   계좌를 저장하고, 그정보를 넘기기
     * @param userId
     * @param initialBalance
     */

    @Transactional
    public AccountDto createAccount(Long userId, Long initialBalance) {

        AccountUser accountUser = getAccountUser(userId);




        // 개인당 소유 계좌수 검증
        validateCreateAccount(accountUser);





        String newAccountNumber = accountRepository.findFirstByOrderByIdDesc()
                .map(account -> (Integer.parseInt(account.getAccountNumber()))+1+"")
                .orElse("1000000000");




        return  AccountDto.fromEntity(accountRepository.save(
                Account.builder()
                        .accountUser(accountUser)
                        .accountStatus(IN_USE)
                        .accountNumber(newAccountNumber)
                        .balance(initialBalance)
                        .registerAt(LocalDateTime.now())
                        .build()
        ));


    }

    private void  validateCreateAccount(AccountUser accountUser){
        if(accountRepository.countByAccountUser(accountUser) >= 10)
        {
            throw new AccountException(MAX_ACCOUNT_PER_USER_10);
        }
    }
    @Transactional
    public Account getAccount(Long id) {
        if(id < 0){
            throw new RuntimeException("Minus");
        }
        return accountRepository.findById(id).get();
    }


    /**
     * 1. userId 가 없을 떄
     * 2. accountNumber 가 없을 떄
     * 3. 계좌의 소유주와 이용자가 다를 떄
     * 4. 계좌가 이미 해지 되었을 때
     * 5. 계좌의 잔액이 이미 남아 있을 떄
     */
    @Transactional
    public AccountDto deleteAccount(Long userId,String accountNumber){


        AccountUser accountUser = getAccountUser(userId);


        /**
         *  해지 실패 case
         * 1. 사용자 없는 경우
         * 2. 사용자 아이디와 계좌 소유주가 다른 경우
         * 3. 계좌가 이미 해지 상태인 경우
         * 4. 잔액이 있는 경우 실패 응답
         */

        Account account = accountRepository.findByAccountNumber(accountNumber)
                .orElseThrow(()->new AccountException(ACCOUNT_NOT_FOUND));

        validateDeleteAccount(accountUser, account);


        /**
         * 해지 작업
         * 1. 계좌상태 --> 미등록
         * 2. 해지 시간 --> 현재
         * 3. Dto 타입으로 entity 반환
         */
        account.setAccountStatus(AccountStatus.UNREGISTERED);
        account.setUnRegisteredAt(LocalDateTime.now());
        accountRepository.save(account);

        return AccountDto.fromEntity(account);
    }



    private void validateDeleteAccount(AccountUser accountUser, Account account) {

         if (!Objects.equals(accountUser.getId(), account.getAccountUser().getId())){
                throw   new AccountException(USER_ACCOUNT_UN_MATCH);
        }
         if (account.getAccountStatus() == AccountStatus.UNREGISTERED){
             throw new AccountException(ACCOUNT_ALREADY_UNREGISTERED);
         }
         if (account.getBalance() > 0){
             throw new AccountException(BALANCE_NOT_EMPTY);
         }

    }



    @Transactional
    public List<AccountDto> getAccountByUserId(Long userId) {
        AccountUser accountUser = getAccountUser(userId);

        List<Account> accounts = accountRepository.findByAccountUser(accountUser);

        return accounts.stream()
                .map(AccountDto::fromEntity)

                .collect(Collectors.toList());
    }

    private AccountUser getAccountUser(Long userId) {
        AccountUser accountUser = accountUserRepository.findById(userId)
                .orElseThrow(() -> new AccountException(USER_NOT_FOUND));
        return accountUser;
    }
}
