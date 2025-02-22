package planettask.model;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.io.Serializable;
import lombok.Getter;
import lombok.Setter;


@Getter
@Setter
public class BookDTO implements Serializable {

  private Long id;

  @NotNull
  @Size(max = 255)
  private String title;

  @NotNull
  @Pattern(regexp = "\\d{13}", message = "ISBN must be 13 digits")
  private String isbn;

  @NotNull
  @Size(max = 255)
  private String author;

  @NotNull
  private Integer availableCopies;

  @NotNull
  private Integer totalCopies;

}
