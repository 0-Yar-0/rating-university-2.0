package ru.ystu.rating.university.model;

import jakarta.persistence.*;

import java.time.OffsetDateTime;

@Entity
@Table(
        name = "data",
        uniqueConstraints = @UniqueConstraint(
                name = "uq_data_user_class_year_iter",
                columnNames = {"app_user_id", "class_id", "year_data", "iter"}
        )
)
public class Data {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "app_user_id", nullable = false)
    private AppUser appUser;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "class_id", nullable = false)
    private RatingClass classType;

    @Column(nullable = false)
    private Integer iter;

    @Column(name = "year_data", nullable = false)
    private Integer yearData;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt = OffsetDateTime.now();

    @OneToOne(mappedBy = "data", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    private ParamsSet paramsSet;

    @OneToOne(mappedBy = "data", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    private CalcResult calcResult;

    public Data() {
    }

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

    public RatingClass getClassType() {
        return classType;
    }

    public void setClassType(RatingClass classType) {
        this.classType = classType;
    }

    public Integer getIter() {
        return iter;
    }

    public void setIter(Integer iter) {
        this.iter = iter;
    }

    public Integer getYearData() {
        return yearData;
    }

    public void setYearData(Integer year_data) {
        this.yearData = year_data;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(OffsetDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public ParamsSet getParamsSet() {
        return paramsSet;
    }

    public void setParamsSet(ParamsSet paramsSet) {
        this.paramsSet = paramsSet;
    }

    public CalcResult getCalcResult() {
        return calcResult;
    }

    public void setCalcResult(CalcResult calcResult) {
        this.calcResult = calcResult;
    }


}
