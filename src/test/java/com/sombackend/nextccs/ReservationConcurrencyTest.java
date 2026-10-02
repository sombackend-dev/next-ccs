package com.sombackend.nextccs;

import com.sombackend.nextccs.entity.Vehicle;
import com.sombackend.nextccs.exception.ReservationConflictException;
import com.sombackend.nextccs.repository.VehicleRepository;
import com.sombackend.nextccs.service.ReservationService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDateTime;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
class ReservationConcurrencyTest {

    @Autowired
    private ReservationService reservationService;

    @Autowired
    private VehicleRepository vehicleRepository;

    @Test
    void 동일_차량에_동시에_예약하면_하나만_성공한다() throws InterruptedException {

        // given
        String vehicleNumber = "TEST-" + UUID.randomUUID();

        Vehicle vehicle = vehicleRepository.save(
                new Vehicle(vehicleNumber, "Concurrency Test Vehicle")
        );

        Long vehicleId = vehicle.getId();

        LocalDateTime startTime =
                LocalDateTime.of(2030, 1, 1, 10, 0);

        LocalDateTime endTime =
                LocalDateTime.of(2030, 1, 1, 12, 0);

        int threadCount = 10;

        ExecutorService executorService =
                Executors.newFixedThreadPool(threadCount);

        CountDownLatch readyLatch =
                new CountDownLatch(threadCount);

        CountDownLatch startLatch =
                new CountDownLatch(1);

        CountDownLatch doneLatch =
                new CountDownLatch(threadCount);

        AtomicInteger successCount =
                new AtomicInteger(0);

        AtomicInteger conflictCount =
                new AtomicInteger(0);

        AtomicReference<Throwable> unexpectedException =
                new AtomicReference<>();

        try {

            // when
            for (int i = 0; i < threadCount; i++) {

                executorService.submit(() -> {

                    try {
                        readyLatch.countDown();

                        startLatch.await();

                        reservationService.create(
                                vehicleId,
                                startTime,
                                endTime
                        );

                        successCount.incrementAndGet();

                    } catch (ReservationConflictException e) {

                        conflictCount.incrementAndGet();

                    } catch (InterruptedException e) {

                        Thread.currentThread().interrupt();
                        unexpectedException.compareAndSet(null, e);

                    } catch (Throwable e) {

                        unexpectedException.compareAndSet(null, e);

                    } finally {

                        doneLatch.countDown();
                    }
                });
            }

            boolean allWorkersReady =
                    readyLatch.await(10, TimeUnit.SECONDS);

            assertTrue(
                    allWorkersReady,
                    "모든 Worker가 제한 시간 안에 준비되지 않았습니다."
            );

            startLatch.countDown();

            boolean allWorkersFinished =
                    doneLatch.await(20, TimeUnit.SECONDS);

            assertTrue(
                    allWorkersFinished,
                    "모든 Worker가 제한 시간 안에 종료되지 않았습니다."
            );

            // then
            assertNull(
                    unexpectedException.get(),
                    () -> "예상하지 못한 예외가 발생했습니다: "
                            + unexpectedException.get()
            );

            assertEquals(1, successCount.get());
            assertEquals(9, conflictCount.get());

        } finally {

            // 테스트가 실패하더라도 Thread Pool 종료
            startLatch.countDown();
            executorService.shutdownNow();
        }
    }
}