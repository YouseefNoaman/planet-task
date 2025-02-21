package planettask.controller;

import planettask.model.BookDTO;
import planettask.service.BookService;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.validation.Valid;
import java.util.List;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping(value = "/api/v1/books", produces = MediaType.APPLICATION_JSON_VALUE)
@Validated
public class BookController {

    private final BookService bookService;

    public BookController(final BookService bookService) {
        this.bookService = bookService;
    }

    @GetMapping
    public ResponseEntity<List<BookDTO>> getAllBooks(@RequestParam(defaultValue = "0") final int page,
                                                     @RequestParam(defaultValue = "10") final int size,
                                                     @RequestParam(defaultValue = "bookId,asc") final String[] sort) {
        final Sort.Direction sortDirection = sort[1].equalsIgnoreCase("desc")
                ? Sort.Direction.DESC
                : Sort.Direction.ASC;
        final Pageable pageable = PageRequest.of(page, size, Sort.by(sortDirection, sort[0]));

        return ResponseEntity.ok(this.bookService.findAll(pageable));
    }

    @GetMapping("/{bookId}")
    public ResponseEntity<BookDTO> getBook(@PathVariable(name = "bookId") final Long bookId) {
        return ResponseEntity.ok(bookService.get(bookId));
    }

    @GetMapping("/isbn/{isbn:\\d{13}}")
    public ResponseEntity<BookDTO> getBookByIsbn(@PathVariable(name = "isbn") final String isbn) {
        return ResponseEntity.ok(bookService.findByIsbn(isbn));
    }

    @PostMapping
    @ApiResponse(responseCode = "201")
    public ResponseEntity<Long> createBook(@RequestBody @Valid final BookDTO bookDTO) {
        final Long createdBookId = bookService.create(bookDTO);
        return new ResponseEntity<>(createdBookId, HttpStatus.CREATED);
    }

//    @PutMapping("/{bookId}")
//    public ResponseEntity<Long> updateBook(@PathVariable(name = "bookId") final Long bookId,
//            @RequestBody @Valid final BookDTO bookDTO) {
//        bookService.update(bookId, bookDTO);
//        return ResponseEntity.ok(bookId);
//    }
//
//    @DeleteMapping("/{bookId}")
//    @ApiResponse(responseCode = "204")
//    public ResponseEntity<Void> deleteBook(@PathVariable(name = "bookId") final Long bookId) {
//        bookService.delete(bookId);
//        return ResponseEntity.noContent().build();
//    }

}
