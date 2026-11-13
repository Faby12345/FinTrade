package ro.scribemed.tradingAccount.handler;


import ro.scribemed.network.protocol.ApiResponse;
import ro.scribemed.network.tcp.ClientSession;
import ro.scribemed.tradingAccount.domain.TradingAccount;
import ro.scribemed.tradingAccount.dto.TradingAccountResponse;
import ro.scribemed.tradingAccount.service.TradingAccountService;

import java.util.UUID;

public class TradingAccountHandler {

    private final TradingAccountService tradingAccountService;

    public TradingAccountHandler(TradingAccountService tradingAccountService) {
        this.tradingAccountService = tradingAccountService;
    }

    public ApiResponse getAccount(
            String requestId,
            ClientSession clientSession
    ) {
        try{
            if(clientSession.authenticatedUserId().isEmpty()){
                throw new IllegalStateException("User id is empty");
            }

            UUID userId = clientSession.authenticatedUserId().get();

            TradingAccount tradingAccount =
                    tradingAccountService.getAccount(userId);
            return ApiResponse.success(
                    requestId,
                    TradingAccountResponse.from(tradingAccount)
            );
        } catch (RuntimeException e) {
            return ApiResponse.error(
                    requestId,
                    "FAIL_TO_FETCH_TRADING_ACCOUNT",
                    "An error occurred fetching trading account"
            );
        }
    }


}
