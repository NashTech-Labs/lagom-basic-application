import com.google.inject.AbstractModule;
import com.knoldus.BookService;
import com.lightbend.lagom.javadsl.server.ServiceGuiceSupport;

public class BookModule extends AbstractModule implements ServiceGuiceSupport {
    @Override
    protected void configure() {
        bindServices(serviceBinding(BookService.class, BookServiceImpl.class));
    }
}
