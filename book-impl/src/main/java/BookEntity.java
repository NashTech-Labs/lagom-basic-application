import akka.Done;
import com.lightbend.lagom.javadsl.persistence.PersistentEntity;
import commands.BookCommand;
import events.BookEvent;
import states.BookStates;

import java.time.LocalDateTime;
import java.util.Optional;

public class BookEntity extends PersistentEntity<BookCommand, BookEvent, BookStates> {
    /**
     *
     * @param snapshotState
     * @return
     */
    @Override
    public Behavior initialBehavior(Optional<BookStates> snapshotState) {

        // initial behaviour of book
        BehaviorBuilder behaviorBuilder = newBehaviorBuilder(
                BookStates.builder().book(Optional.empty())
                        .timestamp(LocalDateTime.now().toString()).build()
        );

        behaviorBuilder.setCommandHandler(BookCommand.CreateBook.class, (cmd, ctx) ->
                ctx.thenPersist(BookEvent.BookCreated.builder().book(cmd.getBook())
                        .entityId(entityId()).build(), evt -> ctx.reply(Done.getInstance()))
        );

        behaviorBuilder.setEventHandler(BookEvent.BookCreated.class, evt ->
                BookStates.builder().book(Optional.of(evt.getBook()))
                        .timestamp(LocalDateTime.now().toString()).build()
        );

        behaviorBuilder.setCommandHandler(BookCommand.UpdateBook.class, (cmd, ctx) ->
                ctx.thenPersist(BookEvent.BookUpdated.builder().book(cmd.getBook()).entityId(entityId()).build()
                        , evt -> ctx.reply(Done.getInstance()))
        );

        behaviorBuilder.setEventHandler(BookEvent.BookUpdated.class, evt ->
                BookStates.builder().book(Optional.of(evt.getBook()))
                        .timestamp(LocalDateTime.now().toString()).build()
        );

        behaviorBuilder.setCommandHandler(BookCommand.DeleteBook.class, (cmd, ctx) ->
                ctx.thenPersist(BookEvent.BookDeleted.builder().book(cmd.getBook()).entityId(entityId()).build(),
                        evt -> ctx.reply(Done.getInstance()))
        );

        behaviorBuilder.setEventHandler(BookEvent.BookDeleted.class, evt ->
                BookStates.builder().book(Optional.empty())
                        .timestamp(LocalDateTime.now().toString()).build()
        );

        behaviorBuilder.setReadOnlyCommandHandler(BookCommand.BookCurrentState.class, (cmd, ctx) ->
                ctx.reply(state().getBook())
        );

        return behaviorBuilder.build();
    }
}