package events;

import akka.Done;
import com.datastax.driver.core.BoundStatement;
import com.datastax.driver.core.PreparedStatement;
import com.lightbend.lagom.javadsl.persistence.AggregateEventTag;
import com.lightbend.lagom.javadsl.persistence.ReadSideProcessor;
import com.lightbend.lagom.javadsl.persistence.cassandra.CassandraReadSide;
import com.lightbend.lagom.javadsl.persistence.cassandra.CassandraSession;
import events.BookEvent.*;
import org.pcollections.PSequence;
import org.pcollections.TreePVector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletionStage;


public class BookEventProcessor extends ReadSideProcessor<BookEvent> {

    private static final Logger LOGGER = LoggerFactory.getLogger(BookEventProcessor.class);

    private final CassandraSession session;
    private final CassandraReadSide readSide;

    private PreparedStatement writeBook;
    private PreparedStatement deleteBook;

    /**
     *
     * @param session
     * @param readSide
     */
    @Inject
    public BookEventProcessor(final CassandraSession session, final CassandraReadSide readSide) {
        this.session = session;
        this.readSide = readSide;
    }

    /**
     *
     * @return
     */
    @Override
    public PSequence<AggregateEventTag<BookEvent>> aggregateTags() {
        LOGGER.info(" aggregateTags method ... ");
        return TreePVector.singleton(BookEventTag.INSTANCE);
    }

    /**
     *
     * @return
     */
    @Override
    public ReadSideHandler<BookEvent> buildHandler() {
        LOGGER.info(" buildHandler method ... ");
        return readSide.<BookEvent>builder("books_offset")
                .setGlobalPrepare(this::createTable)
                .setPrepare(evtTag -> prepareWriteBook()
                        .thenCombine(prepareDeleteBook(), (d1, d2) -> Done.getInstance())
                )
                .setEventHandler(BookCreated.class, this::processPostAdded)
                .setEventHandler(BookUpdated.class, this::processPostUpdated)
                .setEventHandler(BookDeleted.class, this::processPostDeleted)
                .build();
    }

    /**
     *
     * @return
     */
    // Execute only once while application is start
    private CompletionStage<Done> createTable() {
        return session.executeCreateTable(
                "CREATE TABLE IF NOT EXISTS book ( " +
                        "isbn TEXT, bookName TEXT, authorName TEXT, price float, PRIMARY KEY(isbn))"
        );
    }

    /*
    * START: Prepare statement for insert Book values into Book table.
    * This is just creation of prepared statement, we will map this statement with our event
    */

    /**
     *
     * @return
     */
    private CompletionStage<Done> prepareWriteBook() {
        return session.prepare(
                "INSERT INTO book (isbn, bookName, authorName, price) VALUES (?, ?, ?, ?)"
        ).thenApply(ps -> {
            setWriteBook(ps);
            return Done.getInstance();
        });
    }

    /**
     *
     * @param statement
     */
    private void setWriteBook(PreparedStatement statement) {
        this.writeBook = statement;
    }

    // Bind prepare statement while BookCreate event is executed

    /**
     *
     * @param event
     * @return
     */
    private CompletionStage<List<BoundStatement>> processPostAdded(BookCreated event) {
        BoundStatement bindWriteBook = writeBook.bind();
        bindWriteBook.setString("isbn", event.getBook().getIsbn());
        bindWriteBook.setString("bookName", event.getBook().getBookName());
        bindWriteBook.setString("authorName", event.getBook().getAuthorName());
        bindWriteBook.setFloat("price", event.getBook().getPrice());
        return CassandraReadSide.completedStatements(Arrays.asList(bindWriteBook));
    }
    /* ******************* END ****************************/

    /* START: Prepare statement for update the data in Book table.
    * This is just creation of prepared statement, we will map this statement with our event
    */

    /**
     *
     * @param event
     * @return
     */
    private CompletionStage<List<BoundStatement>> processPostUpdated(BookUpdated event) {
        BoundStatement bindWriteBook = writeBook.bind();
        bindWriteBook.setString("isbn", event.getBook().getIsbn());
        bindWriteBook.setString("bookName", event.getBook().getBookName());
        bindWriteBook.setString("authorName", event.getBook().getAuthorName());
        bindWriteBook.setFloat("price", event.getBook().getPrice());
        return CassandraReadSide.completedStatements(Arrays.asList(bindWriteBook));
    }
    /* ******************* END ****************************/

    /* START: Prepare statement for delete the the Book from table.
    * This is just creation of prepared statement, we will map this statement with our event
    */

    /**
     *
     * @return
     */
    private CompletionStage<Done> prepareDeleteBook() {
        return session.prepare(
                "DELETE FROM book WHERE isbn=?"
        ).thenApply(ps -> {
            setDeleteBook(ps);
            return Done.getInstance();
        });
    }

    /**
     *
     * @param deleteBook
     */
    private void setDeleteBook(PreparedStatement deleteBook) {
        this.deleteBook = deleteBook;
    }

    /**
     *
     * @param event
     * @return
     */
    private CompletionStage<List<BoundStatement>> processPostDeleted(BookDeleted event) {
        BoundStatement bindWriteBook = deleteBook.bind();
        bindWriteBook.setString("isbn", event.getBook().getIsbn());
        return CassandraReadSide.completedStatements(Arrays.asList(bindWriteBook));
    }
    /* ******************* END ****************************/
}