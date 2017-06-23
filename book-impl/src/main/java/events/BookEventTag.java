package events;

import com.lightbend.lagom.javadsl.persistence.AggregateEventTag;

public class BookEventTag {

    public static final AggregateEventTag<BookEvent> INSTANCE = AggregateEventTag.of(BookEvent.class);
}
