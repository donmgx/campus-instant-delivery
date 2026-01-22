package com.campus.service;

import java.math.BigDecimal;
import java.time.LocalDate;

public interface RiderStatsService {
    BigDecimal getStats(Long riderId, LocalDate begin, LocalDate end);
}
