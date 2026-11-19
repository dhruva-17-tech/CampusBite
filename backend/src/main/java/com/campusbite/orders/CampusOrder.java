package com.campusbite.orders;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.List;

@Document(collection = "orders")
public class CampusOrder {
    @Id
    private String id;
    @Indexed
    private String customerEmail;
    private String customerName;
    @Indexed
    private String outlet;
    @Indexed
    private String queueDate;
    @Indexed
    private long queueNumber;
    private List<OrderLine> items;
    private double subtotal;
    private int discountRate;
    private double discountAmount;
    private double total;
    private String paymentMethod;
    private String paymentStatus;
    @Indexed
    private OrderStatus status;
    private Instant createdAt;
    private Instant updatedAt;

    public CampusOrder() {
    }

    public CampusOrder(String customerEmail, String customerName, String outlet, long queueNumber,
            List<OrderLine> items, double subtotal, int discountRate, double discountAmount, double total,
            String paymentMethod) {
        this(customerEmail, customerName, outlet, queueNumber, items, subtotal, discountRate, discountAmount, total,
            paymentMethod, java.time.LocalDate.now(java.time.ZoneId.of("Asia/Kolkata")).toString());
        }

        public CampusOrder(String customerEmail, String customerName, String outlet, long queueNumber,
            List<OrderLine> items, double subtotal, int discountRate, double discountAmount, double total,
            String paymentMethod, String queueDate) {
        this.customerEmail = customerEmail;
        this.customerName = customerName;
        this.outlet = outlet;
        this.queueDate = queueDate;
        this.queueNumber = queueNumber;
        this.items = items;
        this.subtotal = subtotal;
        this.discountRate = discountRate;
        this.discountAmount = discountAmount;
        this.total = total;
        this.paymentMethod = paymentMethod;
        this.paymentStatus = "UNPAID";
        this.status = OrderStatus.PENDING;
        this.createdAt = Instant.now();
        this.updatedAt = this.createdAt;
    }

    public String getId() {
        return id;
    }

    public String getCustomerEmail() {
        return customerEmail;
    }

    public String getCustomerName() {
        return customerName;
    }

    public String getOutlet() {
        return outlet;
    }

    public String getQueueDate() {
        return queueDate;
    }

    public long getQueueNumber() {
        return queueNumber;
    }

    public List<OrderLine> getItems() {
        return items;
    }

    public double getSubtotal() {
        return subtotal;
    }

    public int getDiscountRate() {
        return discountRate;
    }

    public double getDiscountAmount() {
        return discountAmount;
    }

    public double getTotal() {
        return total;
    }

    public String getPaymentMethod() {
        return paymentMethod;
    }

    public String getPaymentStatus() {
        return paymentStatus;
    }

    public void setPaymentStatus(String paymentStatus) {
        this.paymentStatus = paymentStatus;
        this.updatedAt = Instant.now();
    }

    public OrderStatus getStatus() {
        return status;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void setStatus(OrderStatus status) {
        this.status = status;
        this.updatedAt = Instant.now();
    }
}
