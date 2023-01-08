package cn.ponfee.commons.event;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;

public class EventBusTest {

    public static class OrderEvent {
        private String message;

        public OrderEvent(String message) {
            this.message = message;
        }

        public String getMessage() {
            return message;
        }
    }

    public static class OrderEventListener {
        @Subscribe
        public void listen(OrderEvent event) {
            System.out.println("OrderEventListener receive msg: " + event.getMessage());
        }

        @Subscribe
        public void listen(String event) {
            System.out.println("OrderEventListener receive msg: " + event);
        }
    }

    public static void main(String[] args) {
        /*
         * 通过EventBus.register(Object object)方法来注册订阅者（subscriber），
         * 使用EventBus.post(Object event)方法来发布事件。
         */
        //1.Creates a new EventBus with the given identifier.
        EventBus eventBus = new EventBus("jackson");

        //2.register all subscriber
        eventBus.register(new OrderEventListener());
        //eventBus.register(new HelloListener());

        //publish event
        eventBus.post(new OrderEvent("order-event-message"));
        eventBus.post("string-event-message");
    }
}
