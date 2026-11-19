package com.campusbite.orders;

import com.campusbite.menu.MenuItem;
import com.campusbite.menu.MenuService;
import com.campusbite.users.AppUser;
import com.campusbite.users.UserRepository;
import com.campusbite.loyalty.LoyaltyService;
import org.springframework.data.mongodb.core.FindAndModifyOptions;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.List;
import java.time.LocalDate;
import java.time.ZoneId;

@Service
public class OrderService {
    private static final List<OrderStatus> ACTIVE = List.of(
            OrderStatus.PENDING, OrderStatus.ACCEPTED, OrderStatus.PREPARING, OrderStatus.READY);
    private static final int MINUTES_PER_ORDER = 5;
    private static final double MINIMUM_STAMP_ORDER_SUBTOTAL = 100.0;
    private static final ZoneId CAMPUS_TIME_ZONE = ZoneId.of("Asia/Kolkata");

    private final OrderRepository orderRepository;
    private final MenuService menuService;
    private final UserRepository userRepository;
    private final LoyaltyService loyaltyService;
    private final MongoTemplate mongoTemplate;

    public OrderService(OrderRepository orderRepository, MenuService menuService, UserRepository userRepository,
            LoyaltyService loyaltyService, MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
        this.orderRepository = orderRepository;
        this.menuService = menuService;
        this.userRepository = userRepository;
        this.loyaltyService = loyaltyService;
    }

    public OrderView create(String email, CreateOrderRequest request) {
        AppUser user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User account not found"));
        if (!"DEMO".equalsIgnoreCase(request.paymentMethod()))
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Only DEMO payment is enabled for this project");

        if (request.items() == null || request.items().isEmpty())
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "At least one item is required");
        List<OrderLine> lines = new ArrayList<>();
        java.util.Map<String, Integer> quantities = new java.util.LinkedHashMap<>();
        double total = 0;
        for (CreateOrderRequest.ItemRequest requested : request.items()) {
            if (requested.quantity() == null || requested.quantity() <= 0)
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Quantity must be positive");
            quantities.merge(requested.menuItemId(), requested.quantity(), Integer::sum);
        }
        for (var entry : quantities.entrySet()) {
            MenuItem item = menuService.get(entry.getKey());
            int quantity = entry.getValue();
            if (!item.getOutlet().equalsIgnoreCase(request.outlet().trim()))
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "All items must belong to the selected outlet");
            if (!item.isAvailable() || item.getStock() < quantity)
                throw new ResponseStatusException(HttpStatus.CONFLICT, item.getName() + " is unavailable or has insufficient stock");
            OrderLine line = new OrderLine(item.getId(), item.getName(), quantity, item.getPrice());
            lines.add(line);
            total += line.getLineTotal();
        }
        int discountRate = request.useLoyaltyCoupon() ? loyaltyService.currentDiscountRate(email) : 0;
        if (request.useLoyaltyCoupon() && discountRate == 0)
            throw new ResponseStatusException(HttpStatus.CONFLICT, "No loyalty coupon is available to apply");
        // Validate every line before applying any stock deductions.
        for (OrderLine line : lines) menuService.deductStock(menuService.get(line.getMenuItemId()), line.getQuantity());

        String outlet = request.outlet().trim();
        String queueDate = LocalDate.now(CAMPUS_TIME_ZONE).toString();
        long queueNumber = nextQueueNumber(outlet, queueDate);
        double discountAmount = Math.round(total * discountRate) / 100.0;
        double payableTotal = Math.max(0, total - discountAmount);
        CampusOrder order = new CampusOrder(email, user.getName(), outlet, queueNumber, lines, total, discountRate,
            discountAmount, payableTotal, "DEMO", queueDate);
        CampusOrder saved = orderRepository.save(order);
        if (discountRate > 0) loyaltyService.redeemCoupon(email);
        return view(saved);
    }

    public List<OrderView> myOrders(String email) {
        return orderRepository.findByCustomerEmailOrderByCreatedAtDesc(email).stream().map(this::view).toList();
    }

    public List<OrderView> queue(String outlet) {
        return orderRepository.findByOutletAndQueueDateAndStatusInOrderByQueueNumberAsc(
            outlet, LocalDate.now(CAMPUS_TIME_ZONE).toString(), ACTIVE).stream().map(this::view).toList();
    }
    /** The whole live queue of one outlet (all customers), anonymised. Position 1 = served first. */
    public List<QueueEntry> board(String outlet, String email) {
        List<CampusOrder> active = orderRepository.findByOutletAndQueueDateAndStatusInOrderByQueueNumberAsc(
            outlet.trim(), LocalDate.now(CAMPUS_TIME_ZONE).toString(), ACTIVE);
        List<QueueEntry> board = new java.util.ArrayList<>();
        for (int i = 0; i < active.size(); i++) {
            CampusOrder o = active.get(i);
            board.add(new QueueEntry(o.getQueueNumber(), i + 1L, o.getStatus().name(),
                    o.getCustomerEmail().equalsIgnoreCase(email), i * MINUTES_PER_ORDER));
        }
        return board;
    }

    public List<OrderView> queueForStaff(String outlet, String assignedOutlet) {
        if (assignedOutlet == null || !assignedOutlet.equalsIgnoreCase(outlet))
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You can access only your assigned outlet queue");
        return queue(outlet);
    }
    public OrderView updateStatusForStaff(String id, OrderStatus next, String assignedOutlet) {
        CampusOrder existing = find(id);
        if (assignedOutlet == null || !assignedOutlet.equalsIgnoreCase(existing.getOutlet()))
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You can update orders only for your assigned outlet");
        if (next == OrderStatus.DELIVERED)
            throw new ResponseStatusException(HttpStatus.CONFLICT, "The customer must confirm collection after payment is marked PAID");
        return updateStatus(id, next);
    }

    public OrderView markPaymentPaid(String id, String assignedOutlet) {
        CampusOrder order = find(id);
        if (assignedOutlet != null && !assignedOutlet.equalsIgnoreCase(order.getOutlet()))
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You can update payment only for your assigned outlet");
        if (order.getStatus() == OrderStatus.CANCELLED || order.getStatus() == OrderStatus.DELIVERED)
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Payment cannot be changed for a completed or cancelled order");
        order.setPaymentStatus("PAID");
        return view(orderRepository.save(order));
    }

    public List<OrderView> allOrders() {
        return orderRepository.findAllByOrderByCreatedAtDesc().stream().map(this::view).toList();
    }

    public OrderView get(String id, String email, boolean privileged) {
        CampusOrder order = find(id);
        if (!privileged && !order.getCustomerEmail().equalsIgnoreCase(email))
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You cannot view another customer's order");
        return view(order);
    }

    public OrderView updateStatus(String id, OrderStatus next) {
        CampusOrder order = find(id);
        OrderStatus current = order.getStatus();
        boolean valid = switch (current) {
            case PENDING -> next == OrderStatus.ACCEPTED || next == OrderStatus.CANCELLED;
            case ACCEPTED -> next == OrderStatus.PREPARING || next == OrderStatus.CANCELLED;
            case PREPARING -> next == OrderStatus.READY;
            case READY -> next == OrderStatus.DELIVERED;
            case DELIVERED, CANCELLED -> false;
        };
        if (!valid)
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Invalid order status change: " + current + " to " + next);
        order.setStatus(next);
        CampusOrder saved = orderRepository.save(order);
        if (next == OrderStatus.CANCELLED) {
            for (OrderLine line : order.getItems()) {
                menuService.addStock(line.getMenuItemId(), line.getQuantity());
            }
        }
        if (next == OrderStatus.DELIVERED && saved.getSubtotal() > MINIMUM_STAMP_ORDER_SUBTOTAL)
            loyaltyService.awardStamp(saved.getCustomerEmail(), saved.getId());
        return view(saved);
    }

    public OrderView markCollected(String id, String email) {
        CampusOrder order = find(id);
        if (!order.getCustomerEmail().equalsIgnoreCase(email))
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You cannot collect another customer's order");
        if (order.getStatus() != OrderStatus.READY)
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Only a ready order can be marked collected");
        if (!"PAID".equalsIgnoreCase(order.getPaymentStatus()))
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Payment must be marked PAID by canteen staff before collection");
        return updateStatus(id, OrderStatus.DELIVERED);
    }

    public OrderView cancelOwn(String id, String email) {
        CampusOrder order = find(id);
        if (!order.getCustomerEmail().equalsIgnoreCase(email))
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You cannot cancel another customer's order");
        if (order.getStatus() != OrderStatus.PENDING)
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Only pending orders can be cancelled");
        order.setStatus(OrderStatus.CANCELLED);
        CampusOrder saved = orderRepository.save(order);
        for (OrderLine line : order.getItems()) menuService.addStock(line.getMenuItemId(), line.getQuantity());
        return view(saved);
    }

    /**
     * Atomic, never-reused queue number per outlet. The old "count + 1" approach gave two customers the
     * same number when they ordered at the same moment, and repeated numbers after orders were deleted.
     */
    static String queueCounterId(String outlet, String queueDate) {
        return queueDate + ":" + outlet.trim().toLowerCase(java.util.Locale.ROOT);
    }

    private long nextQueueNumber(String outlet, String queueDate) {
        Query byOutletAndDate = Query.query(Criteria.where("_id").is(queueCounterId(outlet, queueDate)));
        mongoTemplate.upsert(byOutletAndDate, new Update().setOnInsert("seq", 0), QueueCounter.class);
        QueueCounter counter = mongoTemplate.findAndModify(byOutletAndDate, new Update().inc("seq", 1),
                FindAndModifyOptions.options().returnNew(true), QueueCounter.class);
        if (counter == null)
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Could not allocate a queue number");
        return counter.getSeq();
    }

    private CampusOrder find(String id) {
        return orderRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Order not found"));
    }

    private OrderView view(CampusOrder order) {
        boolean inTodayQueue = LocalDate.now(CAMPUS_TIME_ZONE).toString().equals(order.getQueueDate());
        long ahead = inTodayQueue ? orderRepository
            .findByOutletAndQueueDateAndQueueNumberLessThanAndStatusInOrderByQueueNumberAsc(
                order.getOutlet(), order.getQueueDate(), order.getQueueNumber(), ACTIVE).size() : 0;
        long position = inTodayQueue && ACTIVE.contains(order.getStatus()) ? ahead + 1 : 0;
        int wait = position > 0 ? (int) (ahead * MINUTES_PER_ORDER) : 0;
        return new OrderView(order, position, wait);
    }
}
