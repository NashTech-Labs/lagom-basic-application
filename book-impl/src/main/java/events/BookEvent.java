package events;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.knoldus.Book;
import com.lightbend.lagom.javadsl.persistence.AggregateEvent;
import com.lightbend.lagom.javadsl.persistence.AggregateEventTagger;
import com.lightbend.lagom.serialization.CompressedJsonable;
import com.lightbend.lagom.serialization.Jsonable;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Value;

/**
 * Created by knoldus on 30/1/17.
 */
public interface BookEvent extends Jsonable, AggregateEvent<BookEvent> {

    @Override
    default AggregateEventTagger<BookEvent> aggregateTag() {
        return BookEventTag.INSTANCE;
    }

    @Value
    @Builder
    @JsonDeserialize
    @AllArgsConstructor
    final class BookCreated implements BookEvent, CompressedJsonable {
        Book book;
        String entityId;
    }

    @Value
    @Builder
    @JsonDeserialize
    @AllArgsConstructor
    final class BookUpdated implements BookEvent, CompressedJsonable {
        Book book;
        String entityId;
    }

    @Value
    @Builder
    @JsonDeserialize
    @AllArgsConstructor
    final class BookDeleted implements BookEvent, CompressedJsonable {
        Book book;
        String entityId;
    }
}