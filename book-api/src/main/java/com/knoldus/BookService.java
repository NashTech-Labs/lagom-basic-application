package com.knoldus;

import akka.Done;
import akka.NotUsed;
import com.lightbend.lagom.javadsl.api.Descriptor;
import com.lightbend.lagom.javadsl.api.Service;
import com.lightbend.lagom.javadsl.api.ServiceCall;

import java.util.List;
import java.util.Optional;

import static com.lightbend.lagom.javadsl.api.Service.named;
import static com.lightbend.lagom.javadsl.api.Service.restCall;
import static com.lightbend.lagom.javadsl.api.transport.Method.*;

public interface BookService extends Service {
    /**
     * @param isbn
     * @return
     */
    ServiceCall<NotUsed, Optional<Book>> getBook(String isbn);

    /**
     * @return
     */
    ServiceCall<Book, Done> newBook();

    /**
     * @return
     */
    ServiceCall<Book, Done> updateBook();

    /**
     * @param isbn
     * @return
     */
    ServiceCall<NotUsed, Done> deleteBook(String isbn);

    ServiceCall<NotUsed, List<Book>> getAllBook();

    /**
     * @return
     */
    @Override
    default Descriptor descriptor() {

        return named("book").withCalls(
                restCall(GET, "/api/get-book/:id", this::getBook),
                restCall(POST, "/api/new-book", this::newBook),
                restCall(PUT, "/api/update-book", this::updateBook),
                restCall(DELETE, "/api/delete-book/:id", this::deleteBook),
                restCall(GET, "/api/get-all-book", this::getAllBook)
        ).withAutoAcl(true);
    }
}
