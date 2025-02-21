package planettask.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import java.util.Objects;
import java.util.Set;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;


@Entity
@Table(name = "books")
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
@RequiredArgsConstructor
@Builder
@AllArgsConstructor
public class Book extends BaseEntity{

    @Id
    @SequenceGenerator(
        name = "book_sequence",
        sequenceName = "book_sequence",
        allocationSize = 1,
        initialValue = 10000
    )
    @GeneratedValue(
        strategy = GenerationType.SEQUENCE,
        generator = "book_sequence"
    )
    @Column(name = "book_id", nullable = false, updatable = false)
    private Long bookId;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false, unique = true, length = 13)
    private String isbn;

    @Column(nullable = false)
    private String author;

    @Column(nullable = false)
    @Positive
    private Integer totalCopies;

    @Column(nullable = false)
    @PositiveOrZero
    private Integer availableCopies;

    @ManyToMany(mappedBy = "books")
    private Set<Reservation> reservation;

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Book book = (Book) o;
        return Objects.equals(bookId, book.bookId) && Objects.equals(title,
            book.title) && Objects.equals(isbn, book.isbn) && Objects.equals(author,
            book.author) && Objects.equals(totalCopies, book.totalCopies)
            && Objects.equals(availableCopies, book.availableCopies)
            && Objects.equals(reservation, book.reservation);
    }

    @Override
    public int hashCode() {
        return Objects.hash(bookId, title, isbn, author, totalCopies, availableCopies, reservation);
    }

    @Override
    public String toString() {
        return "Book{" +
            "availableCopies=" + availableCopies +
            ", totalCopies=" + totalCopies +
            ", author='" + author + '\'' +
            ", isbn='" + isbn + '\'' +
            ", title='" + title + '\'' +
            ", bookId=" + bookId +
            '}';
    }
}
