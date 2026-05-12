package com.liteflow.utils;

import com.liteflow.model.inventory.*;
import com.liteflow.model.auth.User;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Test Data Builder for creating test entities
 *
 * This class provides builder pattern methods to create test entities
 * with sensible defaults and customizable properties.
 */
public class TestDataBuilder {

    // Room Builder
    public static class RoomBuilder {
        private Room room = new Room();

        public RoomBuilder() {
            room.setRoomId(UUID.randomUUID());
            room.setName("Test Room");
            room.setDescription("Test room description");
            room.setTableCount(5);
            room.setTotalCapacity(20);
            room.setCreatedAt(LocalDateTime.now());
        }

        public RoomBuilder withId(UUID id) {
            room.setRoomId(id);
            return this;
        }

        public RoomBuilder withName(String name) {
            room.setName(name);
            return this;
        }

        public RoomBuilder withDescription(String description) {
            room.setDescription(description);
            return this;
        }

        public RoomBuilder withTableCount(Integer tableCount) {
            room.setTableCount(tableCount);
            return this;
        }

        public RoomBuilder withTotalCapacity(Integer totalCapacity) {
            room.setTotalCapacity(totalCapacity);
            return this;
        }

        public Room build() {
            return room;
        }
    }

    // Table Builder
    public static class TableBuilder {
        private Table table = new Table();

        public TableBuilder() {
            table.setTableId(UUID.randomUUID());
            table.setTableNumber("T001");
            table.setTableName("Table 1");
            table.setCapacity(4);
            table.setStatus("Available");
            table.setIsActive(true);
            table.setCreatedAt(LocalDateTime.now());
            table.setUpdatedAt(LocalDateTime.now());
        }

        public TableBuilder withId(UUID id) {
            table.setTableId(id);
            return this;
        }

        public TableBuilder withTableNumber(String tableNumber) {
            table.setTableNumber(tableNumber);
            return this;
        }

        public TableBuilder withTableName(String tableName) {
            table.setTableName(tableName);
            return this;
        }

        public TableBuilder withRoom(Room room) {
            table.setRoom(room);
            return this;
        }

        public TableBuilder withCapacity(Integer capacity) {
            table.setCapacity(capacity);
            return this;
        }

        public TableBuilder withStatus(String status) {
            table.setStatus(status);
            return this;
        }

        public TableBuilder withIsActive(Boolean isActive) {
            table.setIsActive(isActive);
            return this;
        }

        public Table build() {
            return table;
        }
    }

    // Reservation Builder
    public static class ReservationBuilder {
        private Reservation reservation = new Reservation();

        public ReservationBuilder() {
            reservation.setReservationId(UUID.randomUUID());
            reservation.setReservationCode("RES" + System.currentTimeMillis());
            reservation.setCustomerName("Test Customer");
            reservation.setCustomerPhone("0123456789");
            reservation.setCustomerEmail("test@example.com");
            reservation.setArrivalTime(LocalDateTime.now().plusHours(2));
            reservation.setNumberOfGuests(4);
            reservation.setStatus("PENDING");
            reservation.setCreatedAt(LocalDateTime.now());
            reservation.setUpdatedAt(LocalDateTime.now());
        }

        public ReservationBuilder withId(UUID id) {
            reservation.setReservationId(id);
            return this;
        }

        public ReservationBuilder withCode(String code) {
            reservation.setReservationCode(code);
            return this;
        }

        public ReservationBuilder withCustomerName(String name) {
            reservation.setCustomerName(name);
            return this;
        }

        public ReservationBuilder withCustomerPhone(String phone) {
            reservation.setCustomerPhone(phone);
            return this;
        }

        public ReservationBuilder withCustomerEmail(String email) {
            reservation.setCustomerEmail(email);
            return this;
        }

        public ReservationBuilder withArrivalTime(LocalDateTime arrivalTime) {
            reservation.setArrivalTime(arrivalTime);
            return this;
        }

        public ReservationBuilder withNumberOfGuests(Integer numberOfGuests) {
            reservation.setNumberOfGuests(numberOfGuests);
            return this;
        }

        public ReservationBuilder withTable(Table table) {
            reservation.setTable(table);
            return this;
        }

        public ReservationBuilder withRoom(Room room) {
            reservation.setRoom(room);
            return this;
        }

        public ReservationBuilder withStatus(String status) {
            reservation.setStatus(status);
            return this;
        }

        public ReservationBuilder withNotes(String notes) {
            reservation.setNotes(notes);
            return this;
        }

        public Reservation build() {
            return reservation;
        }
    }

    // Order Builder
    public static class OrderBuilder {
        private Order order = new Order();

        public OrderBuilder() {
            order.setOrderId(UUID.randomUUID());
            order.setOrderNumber("ORD" + System.currentTimeMillis());
            order.setOrderDate(LocalDateTime.now());
            order.setSubTotal(BigDecimal.valueOf(100.00));
            order.setVat(BigDecimal.valueOf(10.00));
            order.setDiscount(BigDecimal.ZERO);
            order.setTotalAmount(BigDecimal.valueOf(110.00));
            order.setStatus("Pending");
            order.setPaymentMethod("Cash");
            order.setPaymentStatus("Unpaid");
            order.setUpdatedAt(LocalDateTime.now());
        }

        public OrderBuilder withId(UUID id) {
            order.setOrderId(id);
            return this;
        }

        public OrderBuilder withOrderNumber(String orderNumber) {
            order.setOrderNumber(orderNumber);
            return this;
        }

        public OrderBuilder withSession(TableSession session) {
            order.setSession(session);
            return this;
        }

        public OrderBuilder withOrderDate(LocalDateTime orderDate) {
            order.setOrderDate(orderDate);
            return this;
        }

        public OrderBuilder withSubTotal(BigDecimal subTotal) {
            order.setSubTotal(subTotal);
            return this;
        }

        public OrderBuilder withVat(BigDecimal vat) {
            order.setVat(vat);
            return this;
        }

        public OrderBuilder withDiscount(BigDecimal discount) {
            order.setDiscount(discount);
            return this;
        }

        public OrderBuilder withTotalAmount(BigDecimal totalAmount) {
            order.setTotalAmount(totalAmount);
            return this;
        }

        public OrderBuilder withStatus(String status) {
            order.setStatus(status);
            return this;
        }

        public OrderBuilder withPaymentMethod(String paymentMethod) {
            order.setPaymentMethod(paymentMethod);
            return this;
        }

        public OrderBuilder withPaymentStatus(String paymentStatus) {
            order.setPaymentStatus(paymentStatus);
            return this;
        }

        public OrderBuilder withNotes(String notes) {
            order.setNotes(notes);
            return this;
        }

        public OrderBuilder withCreatedBy(User user) {
            order.setCreatedBy(user);
            return this;
        }

        public Order build() {
            return order;
        }
    }

    // Factory methods for quick creation
    public static RoomBuilder createTestRoom() {
        return new RoomBuilder();
    }

    public static TableBuilder createTestTable() {
        return new TableBuilder();
    }

    public static ReservationBuilder createTestReservation() {
        return new ReservationBuilder();
    }

    public static OrderBuilder createTestOrder() {
        return new OrderBuilder();
    }

    // Helper methods for common test scenarios
    public static Room createRoomWithTables(String roomName, int tableCount) {
        Room room = createTestRoom()
                .withName(roomName)
                .withTableCount(tableCount)
                .build();

        for (int i = 1; i <= tableCount; i++) {
            Table table = createTestTable()
                    .withTableNumber("T" + String.format("%03d", i))
                    .withTableName("Table " + i)
                    .withRoom(room)
                    .build();
            room.addTable(table);
        }

        return room;
    }

    public static Reservation createReservationWithTable(Table table) {
        return createTestReservation()
                .withTable(table)
                .withRoom(table.getRoom())
                .build();
    }

    public static Reservation createPendingReservation() {
        return createTestReservation()
                .withStatus("PENDING")
                .withArrivalTime(LocalDateTime.now().plusHours(2))
                .build();
    }

    public static Reservation createConfirmedReservation() {
        return createTestReservation()
                .withStatus("CONFIRMED")
                .withArrivalTime(LocalDateTime.now().plusHours(2))
                .build();
    }

    public static Table createAvailableTable() {
        return createTestTable()
                .withStatus("Available")
                .withIsActive(true)
                .build();
    }

    public static Table createOccupiedTable() {
        return createTestTable()
                .withStatus("Occupied")
                .withIsActive(true)
                .build();
    }

    public static Table createReservedTable() {
        return createTestTable()
                .withStatus("Reserved")
                .withIsActive(true)
                .build();
    }
}
