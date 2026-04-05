package com.delivery;

import com.delivery.exception.DeliveryNotFoundException;
import com.delivery.exception.InvalidStatusTransitionException;
import com.delivery.model.Delivery;
import com.delivery.model.DeliveryStatus;
import com.delivery.service.DeliveryRepository;
import com.delivery.service.DeliveryService;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@DisplayName("Delivery Tracking System - Unit Tests")
class DeliveryServiceTest {

    @Mock
    private DeliveryRepository deliveryRepository;

    @InjectMocks
    private DeliveryService deliveryService;

    private AutoCloseable closeable;

    @BeforeEach
    void setUp() {
        closeable = MockitoAnnotations.openMocks(this);
    }

    @AfterEach
    void tearDown() throws Exception {
        closeable.close();
    }

    // ============================================================
    //  SECTION 1: Status Update Validation Tests
    // ============================================================

    @Nested
    @DisplayName("Status Update Validation")
    class StatusUpdateValidationTests {

        @Test
        @DisplayName("Should update status from PENDING to PICKED_UP")
        void testValidTransition_PENDING_to_PICKED_UP() {
            Delivery delivery = makeDelivery("TRK001", DeliveryStatus.PENDING);
            when(deliveryRepository.findByTrackingNumber("TRK001")).thenReturn(Optional.of(delivery));
            when(deliveryRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

            Delivery result = deliveryService.updateStatus("TRK001", DeliveryStatus.PICKED_UP, "Picked from warehouse");

            assertEquals(DeliveryStatus.PICKED_UP, result.getStatus());
            assertEquals("Picked from warehouse", result.getRemarks());
            verify(deliveryRepository).save(delivery);
        }

        @Test
        @DisplayName("Should update status from PICKED_UP to IN_TRANSIT")
        void testValidTransition_PICKED_UP_to_IN_TRANSIT() {
            Delivery delivery = makeDelivery("TRK002", DeliveryStatus.PICKED_UP);
            when(deliveryRepository.findByTrackingNumber("TRK002")).thenReturn(Optional.of(delivery));
            when(deliveryRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

            Delivery result = deliveryService.updateStatus("TRK002", DeliveryStatus.IN_TRANSIT, null);

            assertEquals(DeliveryStatus.IN_TRANSIT, result.getStatus());
        }

        @Test
        @DisplayName("Should update status from IN_TRANSIT to OUT_FOR_DELIVERY")
        void testValidTransition_IN_TRANSIT_to_OUT_FOR_DELIVERY() {
            Delivery delivery = makeDelivery("TRK003", DeliveryStatus.IN_TRANSIT);
            when(deliveryRepository.findByTrackingNumber("TRK003")).thenReturn(Optional.of(delivery));
            when(deliveryRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

            Delivery result = deliveryService.updateStatus("TRK003", DeliveryStatus.OUT_FOR_DELIVERY, "Out for delivery");

            assertEquals(DeliveryStatus.OUT_FOR_DELIVERY, result.getStatus());
        }

        @Test
        @DisplayName("Should update status from OUT_FOR_DELIVERY to DELIVERED")
        void testValidTransition_OUT_FOR_DELIVERY_to_DELIVERED() {
            Delivery delivery = makeDelivery("TRK004", DeliveryStatus.OUT_FOR_DELIVERY);
            when(deliveryRepository.findByTrackingNumber("TRK004")).thenReturn(Optional.of(delivery));
            when(deliveryRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

            Delivery result = deliveryService.updateStatus("TRK004", DeliveryStatus.DELIVERED, "Delivered successfully");

            assertEquals(DeliveryStatus.DELIVERED, result.getStatus());
        }

        @Test
        @DisplayName("Should allow PENDING to be CANCELLED")
        void testValidTransition_PENDING_to_CANCELLED() {
            Delivery delivery = makeDelivery("TRK005", DeliveryStatus.PENDING);
            when(deliveryRepository.findByTrackingNumber("TRK005")).thenReturn(Optional.of(delivery));
            when(deliveryRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

            Delivery result = deliveryService.updateStatus("TRK005", DeliveryStatus.CANCELLED, "Customer cancelled");

            assertEquals(DeliveryStatus.CANCELLED, result.getStatus());
        }

        @Test
        @DisplayName("Should allow PICKED_UP to FAILED")
        void testValidTransition_PICKED_UP_to_FAILED() {
            Delivery delivery = makeDelivery("TRK006", DeliveryStatus.PICKED_UP);
            when(deliveryRepository.findByTrackingNumber("TRK006")).thenReturn(Optional.of(delivery));
            when(deliveryRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

            Delivery result = deliveryService.updateStatus("TRK006", DeliveryStatus.FAILED, "Damaged in transit");

            assertEquals(DeliveryStatus.FAILED, result.getStatus());
        }

        @Test
        @DisplayName("Should throw exception for non-existent tracking number")
        void testUpdateStatus_TrackingNumberNotFound() {
            when(deliveryRepository.findByTrackingNumber("INVALID")).thenReturn(Optional.empty());

            assertThrows(DeliveryNotFoundException.class,
                () -> deliveryService.updateStatus("INVALID", DeliveryStatus.PICKED_UP, null));
        }

        @Test
        @DisplayName("Should not update remarks when remarks is blank")
        void testUpdateStatus_BlankRemarks_NoChange() {
            Delivery delivery = makeDelivery("TRK007", DeliveryStatus.PENDING);
            delivery.setRemarks("original remark");
            when(deliveryRepository.findByTrackingNumber("TRK007")).thenReturn(Optional.of(delivery));
            when(deliveryRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

            Delivery result = deliveryService.updateStatus("TRK007", DeliveryStatus.PICKED_UP, "   ");

            assertEquals("original remark", result.getRemarks()); // unchanged
        }
    }

    // ============================================================
    //  SECTION 2: Invalid Status Transition Tests
    // ============================================================

    @Nested
    @DisplayName("Invalid Status Transitions")
    class InvalidStatusTransitionTests {

        @ParameterizedTest(name = "INVALID: {0} → {1}")
        @CsvSource({
            "PENDING, IN_TRANSIT",
            "PENDING, OUT_FOR_DELIVERY",
            "PENDING, DELIVERED",
            "PENDING, FAILED",
            "PICKED_UP, OUT_FOR_DELIVERY",
            "PICKED_UP, DELIVERED",
            "PICKED_UP, PENDING",
            "IN_TRANSIT, PENDING",
            "IN_TRANSIT, PICKED_UP",
            "IN_TRANSIT, DELIVERED",
            "IN_TRANSIT, CANCELLED",
            "OUT_FOR_DELIVERY, IN_TRANSIT",
            "OUT_FOR_DELIVERY, PENDING",
            "OUT_FOR_DELIVERY, CANCELLED",
            "DELIVERED, PENDING",
            "DELIVERED, IN_TRANSIT",
            "DELIVERED, FAILED",
            "FAILED, PENDING",
            "FAILED, IN_TRANSIT",
            "CANCELLED, PENDING",
            "CANCELLED, PICKED_UP"
        })
        @DisplayName("Should reject invalid transitions")
        void testInvalidTransition(String fromStr, String toStr) {
            DeliveryStatus from = DeliveryStatus.valueOf(fromStr);
            DeliveryStatus to = DeliveryStatus.valueOf(toStr);

            Delivery delivery = makeDelivery("TRK-INVALID", from);
            when(deliveryRepository.findByTrackingNumber("TRK-INVALID")).thenReturn(Optional.of(delivery));

            InvalidStatusTransitionException ex = assertThrows(
                InvalidStatusTransitionException.class,
                () -> deliveryService.updateStatus("TRK-INVALID", to, null));

            assertTrue(ex.getMessage().contains(from.name()));
            assertTrue(ex.getMessage().contains(to.name()));
        }

        @Test
        @DisplayName("DELIVERED is a terminal state — no further transitions allowed")
        void testTerminalState_DELIVERED() {
            Delivery delivery = makeDelivery("TRK100", DeliveryStatus.DELIVERED);
            when(deliveryRepository.findByTrackingNumber("TRK100")).thenReturn(Optional.of(delivery));

            assertThrows(InvalidStatusTransitionException.class,
                () -> deliveryService.updateStatus("TRK100", DeliveryStatus.FAILED, null));
        }

        @Test
        @DisplayName("CANCELLED is a terminal state — no further transitions allowed")
        void testTerminalState_CANCELLED() {
            Delivery delivery = makeDelivery("TRK101", DeliveryStatus.CANCELLED);
            when(deliveryRepository.findByTrackingNumber("TRK101")).thenReturn(Optional.of(delivery));

            assertThrows(InvalidStatusTransitionException.class,
                () -> deliveryService.updateStatus("TRK101", DeliveryStatus.PENDING, null));
        }

        @Test
        @DisplayName("FAILED is a terminal state — no further transitions allowed")
        void testTerminalState_FAILED() {
            Delivery delivery = makeDelivery("TRK102", DeliveryStatus.FAILED);
            when(deliveryRepository.findByTrackingNumber("TRK102")).thenReturn(Optional.of(delivery));

            assertThrows(InvalidStatusTransitionException.class,
                () -> deliveryService.updateStatus("TRK102", DeliveryStatus.IN_TRANSIT, null));
        }
    }

    // ============================================================
    //  SECTION 3: Create / Fetch Delivery Tests
    // ============================================================

    @Nested
    @DisplayName("Create and Fetch Deliveries")
    class CreateAndFetchTests {

        @Test
        @DisplayName("Should create a new delivery with PENDING status")
        void testCreateDelivery_defaultPendingStatus() {
            Delivery input = new Delivery("TRK200", "Alice", "123 Main St");
            when(deliveryRepository.existsByTrackingNumber("TRK200")).thenReturn(false);
            when(deliveryRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

            Delivery result = deliveryService.createDelivery(input);

            assertEquals(DeliveryStatus.PENDING, result.getStatus());
            assertEquals("TRK200", result.getTrackingNumber());
        }

        @Test
        @DisplayName("Should throw exception for duplicate tracking number")
        void testCreateDelivery_duplicateTrackingNumber() {
            Delivery input = new Delivery("TRK200", "Alice", "123 Main St");
            when(deliveryRepository.existsByTrackingNumber("TRK200")).thenReturn(true);

            assertThrows(IllegalArgumentException.class,
                () -> deliveryService.createDelivery(input));
        }

        @Test
        @DisplayName("Should return all deliveries")
        void testGetAllDeliveries() {
            List<Delivery> mockList = Arrays.asList(
                makeDelivery("T1", DeliveryStatus.PENDING),
                makeDelivery("T2", DeliveryStatus.IN_TRANSIT)
            );
            when(deliveryRepository.findAll()).thenReturn(mockList);

            List<Delivery> result = deliveryService.getAllDeliveries();
            assertEquals(2, result.size());
        }

        @Test
        @DisplayName("Should get delivery by tracking number")
        void testGetByTrackingNumber_found() {
            Delivery delivery = makeDelivery("TRK300", DeliveryStatus.PENDING);
            when(deliveryRepository.findByTrackingNumber("TRK300")).thenReturn(Optional.of(delivery));

            Delivery result = deliveryService.getByTrackingNumber("TRK300");
            assertEquals("TRK300", result.getTrackingNumber());
        }

        @Test
        @DisplayName("Should throw DeliveryNotFoundException for unknown tracking number")
        void testGetByTrackingNumber_notFound() {
            when(deliveryRepository.findByTrackingNumber("UNKNOWN")).thenReturn(Optional.empty());

            assertThrows(DeliveryNotFoundException.class,
                () -> deliveryService.getByTrackingNumber("UNKNOWN"));
        }
    }

    // ============================================================
    //  Helper
    // ============================================================

    private Delivery makeDelivery(String trackingNumber, DeliveryStatus status) {
        Delivery d = new Delivery(trackingNumber, "Test Recipient", "Test Address");
        d.setStatus(status);
        return d;
    }
}
