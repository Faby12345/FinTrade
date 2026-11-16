package ro.scribemed.shared.persistence;

import java.util.PriorityQueue;

public final class OrderBook {
    private final PriorityQueue<BookOrder> buyOrders;
    private final PriorityQueue<BookOrder> sellOrders;

    public OrderBook(PriorityQueue<BookOrder> buyOrders, PriorityQueue<BookOrder> sellOrders) {
        this.buyOrders = buyOrders;
        this.sellOrders = sellOrders;
    }
}
