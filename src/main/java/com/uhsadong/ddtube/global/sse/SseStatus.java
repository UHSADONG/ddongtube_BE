package com.uhsadong.ddtube.global.sse;

public enum SseStatus {
    ADD("ADD"),
    MOVE("MOVE"),
    DELETE("DELETE");

    private final String status;

    SseStatus(String status) {
        this.status = status;
    }

    public String getStatus() {
        return status;
    }

    @Override
    public String toString() {
        return status;
    }
}
