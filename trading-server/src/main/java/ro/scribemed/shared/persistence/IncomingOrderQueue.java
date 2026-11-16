package ro.scribemed.shared.persistence;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ro.scribemed.order.command.OrderCommand;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

public class IncomingOrderQueue {
    private static final int QUEUE_CAPACITY = 10_000;

    private final BlockingQueue<OrderCommand> queue;
    private static final Logger LOGGER =
            LoggerFactory.getLogger(IncomingOrderQueue.class);

    public IncomingOrderQueue(){
        this.queue = new ArrayBlockingQueue<>(QUEUE_CAPACITY);
    }

    public void submit(OrderCommand orderCommand) throws InterruptedException{
        queue.put(orderCommand);
        LOGGER.debug(
                "Order {} queued; queue size is {}",
                orderCommand.orderId(),
                queue.size()
        );
    }

    public OrderCommand take() throws InterruptedException{
        return queue.take();
    }

    public int size(){
        return queue.size();
    }



}
