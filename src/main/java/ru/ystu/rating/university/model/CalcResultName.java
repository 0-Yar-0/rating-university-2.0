package ru.ystu.rating.university.model;

import jakarta.persistence.*;

@Entity
@Table(name = "calc_result_name")
public class CalcResultName {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "calc_result_id", nullable = false, unique = true)
    private CalcResult calcResult;

    @Column(name = "code_class_a", nullable = false)
    private String codeClassA = "А";

    @Column(name = "code_class_b", nullable = false)
    private String codeClassB = "Б";

    @Column(name = "code_class_v", nullable = false)
    private String codeClassV = "В";

    @Column(name = "code_b11", nullable = false, length = 50)
    private String codeB11 = "B11";

    @Column(name = "code_b12", nullable = false, length = 50)
    private String codeB12 = "B12";

    @Column(name = "code_b13", nullable = false, length = 50)
    private String codeB13 = "B13";

    @Column(name = "code_b21", nullable = false, length = 50)
    private String codeB21 = "B21";

    public CalcResultName() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public CalcResult getCalcResult() {
        return calcResult;
    }

    public void setCalcResult(CalcResult calcResult) {
        this.calcResult = calcResult;
    }

    public String getCodeClassA() {
        return codeClassA;
    }

    public void setCodeClassA(String codeClassA) {
        this.codeClassA = codeClassA;
    }

    public String getCodeClassB() {
        return codeClassB;
    }

    public void setCodeClassB(String codeClassB) {
        this.codeClassB = codeClassB;
    }

    public String getCodeClassV() {
        return codeClassV;
    }

    public void setCodeClassV(String codeClassV) {
        this.codeClassV = codeClassV;
    }

    public String getCodeB11() {
        return codeB11;
    }

    public void setCodeB11(String codeB11) {
        this.codeB11 = codeB11;
    }

    public String getCodeB12() {
        return codeB12;
    }

    public void setCodeB12(String codeB12) {
        this.codeB12 = codeB12;
    }

    public String getCodeB13() {
        return codeB13;
    }

    public void setCodeB13(String codeB13) {
        this.codeB13 = codeB13;
    }

    public String getCodeB21() {
        return codeB21;
    }

    public void setCodeB21(String codeB21) {
        this.codeB21 = codeB21;
    }
}
