package ru.ystu.rating.university.model;

import jakarta.persistence.*;

@Entity
@Table(
        name = "user_iter_state",
        uniqueConstraints = @UniqueConstraint(
                name = "uq_user_iter_state_user",
                columnNames = {"app_user_id"}
        )
)
public class UserIterState {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "app_user_id", nullable = false)
    private AppUser appUser;

    @Column(name = "current_iter")
    private Integer currentIter; // может быть null

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public AppUser getAppUser() {
        return appUser;
    }

    public void setAppUser(AppUser appUser) {
        this.appUser = appUser;
    }

    public Integer getCurrentIter() {
        return currentIter;
    }

    public void setCurrentIter(Integer currentIter) {
        this.currentIter = currentIter;
    }
}

