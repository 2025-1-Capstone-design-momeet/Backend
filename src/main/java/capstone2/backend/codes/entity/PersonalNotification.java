package capstone2.backend.codes.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "PersonalNotification")
public class PersonalNotification {
    @Id
    @Column(name = "notificationId", nullable = false)
    private String notificationId;

    @Column(name = "type")
    private int type;

    @Column(name = "content" )
    private String content;

    @Column(name = "title")
    private String title;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "userId")
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "payId")
    private PaymentHistory paymentHistory;
}
