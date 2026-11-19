package com.campusbite.orders;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

/** One document per outlet per local date; "seq" is the last token handed out that day. */
@Document(collection = "queue_counters")
public class QueueCounter {
    @Id
    private String id;
    private long seq;

    public QueueCounter() {
    }

    public String getId() {
        return id;
    }

    public long getSeq() {
        return seq;
    }
}
