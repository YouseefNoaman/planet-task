package planettask.repos;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import planettask.domain.Book;

@Repository
public interface BookRepository extends JpaRepository<Book, Long> {

  boolean existsByIsbn(String isbn);

  Optional<Book> findByIsbn(String isbn);

}
