package planettask.repos;


import java.util.List;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Optional;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.AutoConfigureTestEntityManager;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import planettask.domain.Book;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;

@DataJpaTest
@ExtendWith(SpringExtension.class)
@AutoConfigureTestEntityManager
public class BookRepositoryTest {
  @Autowired
  private BookRepository bookRepository;


  @BeforeEach
  void setUp() {
    bookRepository.deleteAll();
  }

  @Test
  void existsByIsbn_ShouldReturnFalse_WhenBookDoesNotExist() {
    // When
    boolean exists = bookRepository.existsByIsbn("9999999999999");

    // Then
    assertFalse(exists);
  }

  @Test
  void findByIsbn_ShouldReturnBook_WhenIsbnExists() {
    Book book = new Book();
    book.setTitle("Clean Code");
    book.setIsbn("9780132350884");
    book.setAuthor("Robert C. Martin");
    book.setAvailableCopies(5);
    book.setTotalCopies(6);
    bookRepository.save(book);

    // When
    Optional<Book> foundBook = bookRepository.findByIsbn("9780132350884");

    // Then
    assertThat(foundBook).isPresent();
    assertThat(foundBook.get().getTitle()).isEqualTo("Clean Code");
  }

  @Test
  void findByIsbn_ShouldReturnEmpty_WhenIsbnDoesNotExist() {
    // When
    Optional<Book> foundBook = bookRepository.findByIsbn("9999999999999");

    // Then
    assertThat(foundBook).isEmpty();
  }

  @Test
  public void testExistsByIsbn_BookExists() {
    // Given
    Book book = new Book();
    book.setIsbn("1234567891234");
    book.setTitle("Test Book");
    book.setAuthor("Test Author");
    book.setAvailableCopies(3);
    book.setTotalCopies(5);
    bookRepository.save(book);

    // When
    boolean exists = bookRepository.existsByIsbn("1234567891234");

    // Then
    assertTrue(exists, "Book should exist in the repository.");
  }

  @Test
  public void testExistsByIsbn_BookDoesNotExist() {
    // When
    boolean exists = bookRepository.existsByIsbn("9999999991234");

    // Then
    assertFalse(exists, "Non-existent ISBN should return false.");
  }

  @Test
  public void testFindByIsbn_BookExists() {
    // Given
    Book book = new Book();
    book.setIsbn("1112223334444");
    book.setTitle("Findable Book");
    book.setAuthor("author");
    book.setAvailableCopies(3);
    book.setTotalCopies(5);
    bookRepository.save(book);

    // When
    Optional<Book> foundBook = bookRepository.findByIsbn("1112223334444");

    // Then
    assertTrue(foundBook.isPresent(), "Book should be found by ISBN.");
    assertEquals("Findable Book", foundBook.get().getTitle(), "Titles should match.");
  }

  @Test
  public void testFindByIsbn_BookDoesNotExist() {
    // When
    Optional<Book> foundBook = bookRepository.findByIsbn("0000000000000");

    // Then
    assertFalse(foundBook.isPresent(), "No book should be found for a non-existing ISBN.");
  }

  @Test
  public void testExistsByIsbn_EmptyString() {
    // When
    boolean exists = bookRepository.existsByIsbn("");

    // Then
    assertFalse(exists, "Empty ISBN should return false.");
  }

  @Test
  public void testExistsByIsbn_NullValue() {
    // When
    boolean exists = bookRepository.existsByIsbn(null);

    // Then
    assertFalse(exists, "Null ISBN should return false.");
  }

  @Test
  public void testExistsByIsbn_PartialMatchFails() {
    // Given
    Book book = new Book();
    book.setIsbn("1234567891234");
    book.setTitle("Book with partial ISBN");
    book.setAuthor("author");
    book.setAvailableCopies(2);
    book.setTotalCopies(5);
    bookRepository.save(book);

    // When
    boolean exists = bookRepository.existsByIsbn("123456");

    // Then
    assertFalse(exists, "Partial ISBN should not match.");
  }

  @Test
  public void testGetAllBooks() {
    // Given
    Book book1 = new Book();
    book1.setIsbn("111111111");
    book1.setAuthor("author 1");
    book1.setTitle("Book One");
    book1.setAvailableCopies(4);
    book1.setTotalCopies(6);

    Book book2 = new Book();
    book2.setIsbn("222222222");
    book2.setAuthor("author 2");
    book2.setTitle("Book Two");
    book2.setAvailableCopies(2);
    book2.setTotalCopies(4);

    bookRepository.save(book1);
    bookRepository.save(book2);

    // When
    List<Book> books = bookRepository.findAll();

    // Then
    Assertions.assertEquals(2, books.size(), "Should return all books.");
  }

  @Test
  public void testFindById_BookExists() {
    // Given
    Book book = new Book();
    book.setIsbn("5555555551234");
    book.setTitle("Book by ID");
    Book savedBook = bookRepository.save(book);

    // When
    Optional<Book> foundBook = bookRepository.findById(savedBook.getBookId());

    // Then
    assertTrue(foundBook.isPresent(), "Book should be found by ID.");
    assertEquals("Book by ID", foundBook.get().getTitle(), "Titles should match.");
  }

  @Test
  public void testFindById_BookDoesNotExist() {
    // When
    Optional<Book> foundBook = bookRepository.findById(999L);

    // Then
    assertFalse(foundBook.isPresent(), "No book should be found for a non-existing ID.");
  }

  @Test
  public void testFindAllBooks_EmptyDatabase() {
    // When
    List<Book> books = bookRepository.findAll();

    // Then
    assertTrue(books.isEmpty(), "Book list should be empty.");
  }
}