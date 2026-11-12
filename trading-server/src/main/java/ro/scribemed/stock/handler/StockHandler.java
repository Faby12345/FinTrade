package ro.scribemed.stock.handler;

import ro.scribemed.network.protocol.ApiResponse;
import ro.scribemed.stock.domain.Stock;
import ro.scribemed.stock.domain.StockResponse;
import ro.scribemed.stock.service.StockService;

import java.util.List;

public class StockHandler {
    private final StockService stockService;

    public StockHandler(StockService stockService) {
        this.stockService = stockService;
    }

    public ApiResponse findAll(
            String requestId
    ){
        try {
            List<Stock> stocks = stockService.fetchAll();
            List<StockResponse> responses = stocks
                    .stream()
                    .map(StockResponse::from)
                    .toList();

            return ApiResponse.success(requestId,
                    responses
                    );
        } catch (RuntimeException e) {
            return ApiResponse.error(
                    requestId,
                    "FAIL_TO_FETCH_STOCKS",
                    "An error occurred fetching the stocks"
            );
        }

    }
}
