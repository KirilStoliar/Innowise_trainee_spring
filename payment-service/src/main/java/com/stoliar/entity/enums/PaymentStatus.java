package com.stoliar.entity.enums;

public enum PaymentStatus {
    PENDING,      // Ожидает обработки
    PROCESSING,   // В обработке
    COMPLETED,    // Успешно завершен
    FAILED,       // Не удалось
    REFUNDED,     // Возвращен
    CANCELLED,    // Отменен
    DECLINED      // Отклонен
}