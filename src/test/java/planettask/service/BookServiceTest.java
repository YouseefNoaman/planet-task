package planettask.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import planettask.domain.Book;
import planettask.model.BookDTO;
import planettask.repos.BookRepository;
import planettask.util.NotFoundException;

@ExtendWith(MockitoExtension.class)
class BookServiceTest {

  @Mock
  private BookRepository bookRepository;

  @Mock
  private ModelMapper modelMapper;

  @InjectMocks
  private BookService bookService;

  private Book book;
  private BookDTO bookDTO;

  @BeforeEach
  void setUp() {
    book = new Book();
    book.setBookId(1L);
    book.setTitle("Test Book");
    book.setIsbn("1234567891234");

    bookDTO = new BookDTO();
    bookDTO.setTitle("Test Book");
    bookDTO.setIsbn("1234567891234");
  }

  @Test
  void findAll_ShouldReturnListOfBooks() {
    Pageable pageable = PageRequest.of(0, 5);
    Page<Book> bookPage = new PageImpl<>(List.of(book));

    when(bookRepository.findAll(pageable)).thenReturn(bookPage);
    when(modelMapper.map(any(Book.class), eq(BookDTO.class))).thenReturn(bookDTO);

    List<BookDTO> result = bookService.findAll(pageable);

    assertEquals(1, result.size());
    assertEquals("Test Book", result.getFirst().getTitle());
    verify(bookRepository).findAll(pageable);
  }

  @Test
  void get_ShouldReturnBookDTO_WhenBookExists() {
    when(bookRepository.findById(1L)).thenReturn(Optional.of(book));
    when(modelMapper.map(book, BookDTO.class)).thenReturn(bookDTO);

    BookDTO result = bookService.get(1L);

    assertNotNull(result);
    assertEquals("Test Book", result.getTitle());
    verify(bookRepository).findById(1L);
  }

  @Test
  void get_ShouldThrowNotFoundException_WhenBookNotFound() {
    when(bookRepository.findById(1L)).thenReturn(Optional.empty());

    assertThrows(NotFoundException.class, () -> bookService.get(1L));

    verify(bookRepository).findById(1L);
  }

  @Test
  void create_ShouldSaveBook_WhenIsbnIsUnique() {
    when(bookRepository.existsByIsbn("1234567891234")).thenReturn(false);
    when(modelMapper.map(bookDTO, Book.class)).thenReturn(book);
    when(bookRepository.save(any(Book.class))).thenReturn(book);

    Long result = bookService.create(bookDTO);

    assertEquals(1L, result);
    verify(bookRepository).save(any(Book.class));
  }

  @Test
  void create_ShouldThrowException_WhenIsbnAlreadyExists() {
    when(bookRepository.existsByIsbn("1234567891234")).thenReturn(true);

    assertThrows(IllegalArgumentException.class, () -> bookService.create(bookDTO));

    verify(bookRepository, never()).save(any(Book.class));
  }

  @Test
  void findByIsbn_ShouldReturnBookDTO_WhenBookExists() {
    when(bookRepository.findByIsbn("1234567891234")).thenReturn(Optional.of(book));
    when(modelMapper.map(book, BookDTO.class)).thenReturn(bookDTO);

    BookDTO result = bookService.findByIsbn("1234567891234");

    assertNotNull(result);
    assertEquals("Test Book", result.getTitle());
    verify(bookRepository).findByIsbn("1234567891234");
  }

  @Test
  void findByIsbn_ShouldThrowNotFoundException_WhenBookNotFound() {
    when(bookRepository.findByIsbn("1234567891234")).thenReturn(Optional.empty());

    assertThrows(NotFoundException.class, () -> bookService.findByIsbn("1234567891234"));

    verify(bookRepository).findByIsbn("1234567891234");
  }
}
