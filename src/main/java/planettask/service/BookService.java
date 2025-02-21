package planettask.service;

import jakarta.transaction.Transactional;
import java.util.List;
import org.modelmapper.ModelMapper;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import planettask.domain.Book;
import planettask.model.BookDTO;
import planettask.repos.BookRepository;
import planettask.util.NotFoundException;


@Service
@Transactional
public class BookService {

  private final BookRepository bookRepository;
  private final ModelMapper modelMapper;

  public BookService(final BookRepository bookRepository, ModelMapper modelMapper) {
    this.bookRepository = bookRepository;
    this.modelMapper = modelMapper;
  }

  @Cacheable(value = "book", key = "{#pageable.pageNumber, #pageable.pageSize, #pageable.sort}")
  public List<BookDTO> findAll(final Pageable pageable) {
    final Page<Book> books = this.bookRepository.findAll(pageable);
    return books.stream().map(device -> modelMapper.map(device, BookDTO.class)).toList();
  }

  @Cacheable(value = "book", key = "#bookId")
  public BookDTO get(final Long bookId) {
    return bookRepository.findById(bookId)
        .map(book -> modelMapper.map(book, BookDTO.class))
        .orElseThrow(NotFoundException::new);
  }

  public Long create(final BookDTO bookDTO) {
    if (bookRepository.existsByIsbn(bookDTO.getIsbn())) {
      throw new IllegalArgumentException("Book with isbn " + bookDTO.getIsbn() + " already exists");
    }
    Book book = modelMapper.map(bookDTO, Book.class);
    return bookRepository.save(book).getBookId();
  }

  @Cacheable(value = "book", key = "#isbn")
  public BookDTO findByIsbn(final String isbn) {
    return bookRepository.findByIsbn(isbn)
        .map(book -> modelMapper.map(book, BookDTO.class))
        .orElseThrow(NotFoundException::new);
  }

}
