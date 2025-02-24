package planettask.domain;

import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import java.io.Serializable;
import java.time.OffsetDateTime;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;


@Getter
@Setter
@ToString
@RequiredArgsConstructor
@MappedSuperclass
public abstract class BaseEntity implements Serializable {

  @CreatedDate
  @Column(nullable = false, updatable = false)
  @Temporal(TemporalType.TIMESTAMP)
  private OffsetDateTime dateCreated;

  @LastModifiedDate
  @Column(nullable = false)
  @Temporal(TemporalType.TIMESTAMP)
  private OffsetDateTime lastUpdated;

  @PrePersist
  protected void onCreate() {
    if (dateCreated == null) {
      this.dateCreated = OffsetDateTime.now();
    }
    if (lastUpdated == null) {
      this.lastUpdated = OffsetDateTime.now();
    }
  }

  @PreUpdate
  protected void onUpdate() {
    lastUpdated = OffsetDateTime.now();
  }


}
