package io.liveoak.pgsql;

import java.util.IdentityHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import io.liveoak.spi.ResourceProcessingException;
import io.liveoak.spi.resource.async.Resource;

/**
 * @author <a href="mailto:marko.strukelj@gmail.com">Marko Strukelj</a>
 */
public class PgSqlBatchItem<T, R> {

    private Resource parent;
    private String id;
    private ResourceProcessingException error;
    private T input;
    private R output;

    public PgSqlBatchItem(Resource parent, String id) {
        this.parent = parent;
        this.id = id;
    }

    public PgSqlBatchItem(Resource parent, String id, T input) {
        this.parent = parent;
        this.id = id;
        this.input = input;
    }

    public Resource parent() {
        return parent;
    }

    public String id() {
        return id;
    }

    public void error(ResourceProcessingException error) {
        this.error = error;
    }

    public ResourceProcessingException error() {
        return error;
    }

    public PgSqlBatchItem input(T o) {
        this.input = o;
        return this;
    }

    public T input() {
        return input;
    }

    public PgSqlBatchItem output(R o) {
        this.output = o;
        return this;
    }

    public R output() {
        return output;
    }

    public static <T> List<T> asListOfInputs(List<PgSqlBatchItem<T, ?>> batchItems) {
        List<T> result = new LinkedList<>();
        for (PgSqlBatchItem<T, ?> item: batchItems) {
            result.add(item.input());
        }
        return result;
    }

    public static <T> Map<T, PgSqlBatchItem> asMapOfInputs(List<PgSqlBatchItem<T, ?>> batchItems) {
        Map<T, PgSqlBatchItem> result = new IdentityHashMap<>();
        for (PgSqlBatchItem<T, ?> item: batchItems) {
            result.put(item.input(), item);
        }
        return result;
    }
}
