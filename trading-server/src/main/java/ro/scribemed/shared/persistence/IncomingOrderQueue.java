package ro.scribemed.shared.persistence;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ro.scribemed.order.command.OrderCommand;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

public class IncomingOrderQueue {
    public static int QUEUE_CAPACITY = 10000;
    BlockingQueue<OrderCommand> queue;
    private static final Logger LOGGER =
            LoggerFactory.getLogger(IncomingOrderQueue.class);

    public IncomingOrderQueue(){
        this.queue = new ArrayBlockingQueue<>(QUEUE_CAPACITY);
    }

    public void submit(OrderCommand orderCommand) throws InterruptedException{
        queue.put(orderCommand);
      LOGGER.info("QUEUE: {}", queue.toString());
    }

    public OrderCommand take() throws InterruptedException{
        return queue.take();
    }

    public int size(){
        return queue.size();
    }



}
