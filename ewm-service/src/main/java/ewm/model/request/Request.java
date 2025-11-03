package ewm.model.request;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "requests")
@Getter
@Setter
public class Request {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "event_id")
    private Long eventId;

    @Column(name = "requester_id")
    private Long requesterId;

    @CreationTimestamp
    private LocalDateTime created;

    @Enumerated(EnumType.STRING)
    private RequestStatus status;
/*
    public void confirmed() {
        this.status = RequestStatus.CONFIRMED;
    }

    public void rejected() {
        this.status = RequestStatus.REJECTED;
    }

    public void canceled() {
        this.status = RequestStatus.CANCELED;
    }*/
}
