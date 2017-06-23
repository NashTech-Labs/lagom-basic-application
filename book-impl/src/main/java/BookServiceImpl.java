import akka.Done;
import akka.NotUsed;
import com.knoldus.Book;
import com.knoldus.BookService;
import com.lightbend.lagom.javadsl.api.ServiceCall;
import com.lightbend.lagom.javadsl.persistence.PersistentEntityRef;
import com.lightbend.lagom.javadsl.persistence.PersistentEntityRegistry;
import com.lightbend.lagom.javadsl.persistence.ReadSide;
import com.lightbend.lagom.javadsl.persistence.cassandra.CassandraSession;
import commands.BookCommand;
import events.BookEventProcessor;
import play.api.libs.iteratee.Enumeratee;

import javax.inject.Inject;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletionStage;
import java.util.stream.Collector;
import java.util.stream.Collectors;

import static org.reflections.util.ConfigurationBuilder.build;

public class BookServiceImpl implements BookService {

    private final PersistentEntityRegistry persistentEntityRegistry;
    private final CassandraSession session;

    /**
     * @param registry
     * @param readSide
     * @param session
     */
    @Inject
    public BookServiceImpl(final PersistentEntityRegistry registry, ReadSide readSide, CassandraSession session) {
        this.persistentEntityRegistry = registry;
        this.session = session;

        persistentEntityRegistry.register(BookEntity.class);
        readSide.register(BookEventProcessor.class);
    }

    /**
     * @param isbn
     * @return
     */
    @Override
    public ServiceCall<NotUsed, Optional<Book>> getBook(String isbn) {
        return request -> {
            CompletionStage<Optional<Book>> bookFuture =
                    session.selectAll("SELECT * FROM book WHERE isbn = ?", isbn)
                            .thenApply(rows ->
                                    rows.stream()
                                            .map(row -> Book.builder().isbn(row.getString("isbn"))
                                                    .bookName(row.getString("bookName"))
                                                    .authorName(row.getString("authorName"))
                                                    .price(row.getFloat("price"))
                                                    .build()
                                            )
                                            .findFirst()
                            );
            return bookFuture;
        };
    }

    /**
     * @return
     */
    @Override
    public ServiceCall<Book, Done> newBook() {
        return book -> {
            PersistentEntityRef<BookCommand> ref = bookEntityRef(book);
            return ref.ask(BookCommand.CreateBook.builder().book(book).build());
        };
    }

    /**
     * @return
     */
    @Override
    public ServiceCall<Book, Done> updateBook() {
        return book -> {
            PersistentEntityRef<BookCommand> ref = bookEntityRef(book);
            return ref.ask(BookCommand.UpdateBook.builder().book(book).build());
        };
    }

    /**
     * @param isbn
     * @return
     */
    @Override
    public ServiceCall<NotUsed, Done> deleteBook(String isbn) {
        return request -> {
            Book book = Book.builder().isbn(isbn).build();
            System.out.println(book);
            PersistentEntityRef<BookCommand> ref = bookEntityRef(book);
            return ref.ask(BookCommand.DeleteBook.builder().book(book).build());
        };
    }

    @Override
    public ServiceCall<NotUsed, List<Book>> getAllBook() {
        return request -> {
            CompletionStage<List<Book>> bookFuture =
                    session.selectAll("SELECT * FROM book")
                            .thenApply(rows ->
                                    rows.stream()
                                            .map(row -> Book.builder().isbn(row.getString("isbn"))
                                                    .bookName(row.getString("bookName"))
                                                    .authorName(row.getString("authorName"))
                                                    .price(row.getFloat("price"))
                                                    .build()
                                            ).collect(Collectors.toList())
                            );
            return bookFuture;
        };
    }

    /**
     * @param book
     * @return
     */
    private PersistentEntityRef<BookCommand> bookEntityRef(Book book) {
        return persistentEntityRegistry.refFor(BookEntity.class, book.getIsbn());
    }
}

