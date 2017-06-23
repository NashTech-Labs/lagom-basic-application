package commands;

import akka.Done;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.knoldus.Book;
import com.lightbend.lagom.javadsl.persistence.PersistentEntity;
import com.lightbend.lagom.serialization.Jsonable;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Value;

import java.util.Optional;

/**
 * Created by knoldus on 30/1/17.
 */
public interface BookCommand extends Jsonable {

    @Value
    @Builder
    @JsonDeserialize
    @AllArgsConstructor
    final class CreateBook implements BookCommand, PersistentEntity.ReplyType<Done> {
        Book book;
    }

    @Value
    @Builder
    @JsonDeserialize
    @AllArgsConstructor
    final class UpdateBook implements BookCommand, PersistentEntity.ReplyType<Done> {
        Book book;
    }

    @Value
    @Builder
    @JsonDeserialize
    @AllArgsConstructor
    final class DeleteBook implements BookCommand, PersistentEntity.ReplyType<Done> {
        Book book;
    }

    @JsonDeserialize
    final class BookCurrentState implements BookCommand, PersistentEntity.ReplyType<Optional<Book>> {}
}
