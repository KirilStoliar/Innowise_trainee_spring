package com.stoliar.entity.enums;

public enum PaymentStatus {
    PENDING,      // Ожидает обработки
    PROCESSING,   // В обработке
    COMPLETED,    // Успешно завершен (SUCCESS)
    FAILED,       // Не удалось
    REFUNDED,     // Возвращен
    CANCELLED,    // Отменен
    DECLINED;     // Отклонен

    public boolean isSuccess() {
        return this == COMPLETED;
    }

    public boolean isFailure() {
        return this == FAILED || this == DECLINED || this == CANCELLED;
    }
}