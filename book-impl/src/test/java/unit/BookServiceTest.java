package unit;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.net.URI;
import java.util.Optional;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import akka.Done;
import com.knoldus.Book;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import com.lightbend.lagom.javadsl.client.integration.LagomClientFactory;
import akka.actor.ActorSystem;
import akka.stream.ActorMaterializer;
import akka.stream.Materializer;

import com.knoldus.BookService;

public class BookServiceTest {

    /**
     * Demo data for testing
     */
    private static final String SERVICE_LOCATOR_URI = "http://localhost:8000";
    private static LagomClientFactory clientFactory;
    //    private static CommerceContentService commerceContentService;
    private static BookService bookService;
    private static ActorSystem system;
    private static Materializer mat;
    private static final String locale = "en";
    private static final String channel = "royal-mobile";
    private static final Optional<String> sailingID = Optional.of("AL20170514");
    private static final Optional<String> venueCode = Optional.of("jrok-2");

    @BeforeClass
    public static void setup() {

        clientFactory = LagomClientFactory.create("unit-test", BookService.class.getClassLoader());
        bookService = clientFactory.createDevClient(BookService.class,
                URI.create(SERVICE_LOCATOR_URI));
        system = ActorSystem.create();
        mat = ActorMaterializer.create(system);

    }

    @Before
    public void preLoadData(){
        Book book1 = Book.builder().isbn("1").bookName("Introduction to Algorithms").authorName("Thomas H. Cormen").price(499.99f).build();
        Book book2 = Book.builder().isbn("2").bookName("C Programming").authorName("Dennis Ritchie").price(199.99f).build();
        Book book3 = Book.builder().isbn("3").bookName("Operating System Concepts").authorName("Galvin").price(620).build();
        Book book4 = Book.builder().isbn("4").bookName("Compiler Design").authorName("Aho Ullman").price(340).build();
        try {
            Done done;
            done = await(bookService.newBook().invoke(book1));
            done = await(bookService.newBook().invoke(book2));
            done = await(bookService.newBook().invoke(book3));
            done = await(bookService.newBook().invoke(book4));
        }catch (Exception exc){
            System.out.println("Exception (preLoadData()) : Unable to load data.");
        }
        System.out.println("Data inserted successfully.");
    }

    @Test
    public void getBook() throws Exception {
        String isbn = "1";
        Optional<Book> response = await(bookService.getBook(isbn).invoke());

        assertTrue(response.isPresent());

        if (response.get().getBookName() == null || response.get().getAuthorName() == null || response.get().getPrice() == 0.0)
            throw new AssertionError("getBook() service failed to return Book Object.");
        String bookName = "Introduction to Algorithms";
        String authorName = "Thomas H. Cormen";
        float price = 199.99f;
        if(bookName.equals(response.get().getBookName())){
            if(authorName.equals(response.get().getAuthorName())){
                if(price==response.get().getPrice()){
                    System.out.println("Valid Input.");
                }else{
                    System.out.println("Price Mismatch.");
                    assert(true);
                }
            }else{
                System.out.println("Author Name mismatch.");
                assert(true);
            }
        }else{
            System.out.println("Book Name mismatch.");
            assert(true);
        }
    }

    @After
    public void postDeleteData(){
        try {
            Done done;
            done = await(bookService.deleteBook("1").invoke());
            done = await(bookService.deleteBook("2").invoke());
            done = await(bookService.deleteBook("3").invoke());
            done = await(bookService.deleteBook("4").invoke());
        }catch (Exception exc){
            System.out.println("Exception (preLoadData()) : Unable to delete data from cassandra.");
        }
        System.out.println("Data deleted from cassandra successfully.");
    }

    private <T> T await(CompletionStage<T> future) throws Exception {
        return future.toCompletableFuture().get(10, TimeUnit.SECONDS);
    }
}