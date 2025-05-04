package vn.kltn.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "saved_items", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"user_id", "item_id"})
})
public class SavedItem extends BaseEntity {
    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne
    @JoinColumn(name = "item_id", nullable = false)
    private Item item;
}
