package io.debezium.pipeline.source.snapshot.incremental;

import java.util.Objects;
import java.util.Optional;

public class DataCollection<T> {
    private final T id;
    private final Optional<String> additionalCondition;
    private final Optional<String> surrogateKey;

    public DataCollection(T id) {
        this(id, Optional.empty(), Optional.empty());
    }

    public DataCollection(T id, Optional<String> additionalCondition, Optional<String> surrogateKey) {
        Objects.requireNonNull(additionalCondition);
        Objects.requireNonNull(surrogateKey);
        this.id = id;
        this.additionalCondition = additionalCondition;
        this.surrogateKey = surrogateKey;
    }

    public T getId() {
        return this.id;
    }

    public Optional<String> getAdditionalCondition() {
        return this.additionalCondition;
    }

    public Optional<String> getSurrogateKey() {
        return this.surrogateKey;
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        } else if (o != null && this.getClass() == o.getClass()) {
            DataCollection<?> that = (DataCollection) o;
            return this.id.equals(that.id);
        } else {
            return false;
        }
    }

    public int hashCode() {
        return Objects.hash(new Object[]{this.id});
    }

    public String toString() {
        return "DataCollection{id=" + this.id + ", additionalCondition=" + this.additionalCondition + ", surrogateKey=" + this.surrogateKey + "}";
    }
}
