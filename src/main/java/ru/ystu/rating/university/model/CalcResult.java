package ru.ystu.rating.university.model;

import com.vladmihalcea.hibernate.type.json.JsonBinaryType;
import jakarta.persistence.*;
import org.hibernate.annotations.Type;

import java.util.Map;

@Entity
@Table(name = "calc_result")
public class CalcResult {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "data_id", nullable = false, unique = true)
    private Data data;

    @Type(JsonBinaryType.class)
    @Column(name = "calc_params", columnDefinition = "jsonb", nullable = false)
    private Map<String, Object> calcParams;

    @OneToOne(mappedBy = "calcResult", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    private CalcResultName labels;

    public CalcResult() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Data getData() {
        return data;
    }

    public void setData(Data data) {
        this.data = data;
    }

    public Map<String, Object> getCalcParams() {
        return calcParams;
    }

    public void setCalcParams(Map<String, Object> calcParams) {
        this.calcParams = calcParams;
    }

    public CalcResultName getLabels() {
        return labels;
    }

    public void setLabels(CalcResultName labels) {
        this.labels = labels;
    }
}
