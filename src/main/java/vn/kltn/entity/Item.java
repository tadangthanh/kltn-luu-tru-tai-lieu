package vn.kltn.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import vn.kltn.common.ItemType;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Getter
@Setter
@Entity
@Inheritance(strategy = InheritanceType.JOINED)
public abstract class Item extends BaseEntity {
    private String name;
    @ManyToOne
    @JoinColumn(name = "parent_id")
    private Item parent;
    @ManyToOne
    @JoinColumn(name = "owner_id")
    private User owner;
    private LocalDateTime deletedAt;
    @Enumerated(EnumType.STRING)
    private ItemType itemType;

    @Column(name = "permanent_delete_at")
    private LocalDateTime permanentDeleteAt; // thoi gian xoa vinh vien
    @OneToMany(mappedBy = "parent", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Item> children = new HashSet<>();

    @OneToMany(mappedBy = "item", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Permission> permissions = new HashSet<>();

    @OneToMany(mappedBy = "item", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<SavedItem> savedItems = new HashSet<>();

    @OneToMany(mappedBy = "item", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<SharedLink> sharedLinks = new HashSet<>();


}
