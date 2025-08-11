package com.depth.learningcrew.system.limiter.llm;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.concurrent.atomic.AtomicLong;

@Component
public class TpmRateLimiter {

    private final long capacityPerMinute;
    private final AtomicLong available;   // 현재 윈도우에서 남은 토큰
    private volatile long windowStartMs;  // 현재 윈도우 시작 시각(ms)

    public TpmRateLimiter(@Value("${llm.tpm.capacity:1000000}") long capacityPerMinute) {
        this.capacityPerMinute = capacityPerMinute;
        this.available = new AtomicLong(capacityPerMinute);
        this.windowStartMs = System.currentTimeMillis();
    }

    public void acquire(long tokens) {
        if (tokens <= 0) return;

        // 요청 토큰이 분당 용량보다 큰 경우: 여러 분에 걸쳐 소비
        long remaining = tokens;
        while (remaining > 0) {
            refillIfNeeded();
            long canTake = Math.min(remaining, available.get());
            if (canTake > 0) {
                long before = available.get();
                if (before >= canTake && available.compareAndSet(before, before - canTake)) {
                    remaining -= canTake;
                    continue;
                }
                // CAS 경합 시 반복
                continue;
            }

            // 현재 윈도우에 토큰이 없으므로 다음 윈도우까지 대기
            sleepUntilNextWindow();
        }
    }

    private synchronized void refillIfNeeded() {
        long now = System.currentTimeMillis();
        long elapsed = now - windowStartMs;
        if (elapsed >= 60_000) {
            windowStartMs = now - (elapsed % 60_000); // 새 윈도우 시작: 단순히 남은 토큰을 capacity로 리필
            available.set(capacityPerMinute);
        }
    }

    private void sleepUntilNextWindow() {
        long now = System.currentTimeMillis();
        long remain = 60_000 - (now - windowStartMs);
        if (remain < 5) remain = 5;
        try { Thread.sleep(Math.min(remain, 1_000)); } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException(e);
        }
    }
}
