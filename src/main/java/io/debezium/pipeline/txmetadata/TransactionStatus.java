package io.debezium.pipeline.txmetadata;

public enum TransactionStatus {
    BEGIN,
    END;

    // $FF: synthetic method
    private static TransactionStatus[] $values() {
        return new TransactionStatus[]{BEGIN, END};
    }
}
