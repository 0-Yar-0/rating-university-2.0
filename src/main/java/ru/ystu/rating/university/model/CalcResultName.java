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

    @Column(name = "code_b22", nullable = false, length = 50)
    private String codeB22 = "B22";

    @Column(name = "code_b23", nullable = false, length = 50)
    private String codeB23 = "B23";

    @Column(name = "code_b24", nullable = false, length = 50)
    private String codeB24 = "B24";

    @Column(name = "code_b25", nullable = false, length = 50)
    private String codeB25 = "B25";

    @Column(name = "code_b26", nullable = false, length = 50)
    private String codeB26 = "B26";

    @Column(name = "code_b31", nullable = false, length = 50)
    private String codeB31 = "B31";

    @Column(name = "code_b32", nullable = false, length = 50)
    private String codeB32 = "B32";

    @Column(name = "code_b33", nullable = false, length = 50)
    private String codeB33 = "B33";

    @Column(name = "code_b34", nullable = false, length = 50)
    private String codeB34 = "B34";

    @Column(name = "code_b41", nullable = false, length = 50)
    private String codeB41 = "B41";

    @Column(name = "code_b42", nullable = false, length = 50)
    private String codeB42 = "B42";

    @Column(name = "code_b43", nullable = false, length = 50)
    private String codeB43 = "B43";

    @Column(name = "code_b44", nullable = false, length = 50)
    private String codeB44 = "B44";

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

    public String getCodeB22() {
        return codeB22;
    }

    public void setCodeB22(String codeB22) {
        this.codeB22 = codeB22;
    }

    public String getCodeB23() {
        return codeB23;
    }

    public void setCodeB23(String codeB23) {
        this.codeB23 = codeB23;
    }

    public String getCodeB24() {
        return codeB24;
    }

    public void setCodeB24(String codeB24) {
        this.codeB24 = codeB24;
    }

    public String getCodeB25() {
        return codeB25;
    }

    public void setCodeB25(String codeB25) {
        this.codeB25 = codeB25;
    }

    public String getCodeB26() {
        return codeB26;
    }

    public void setCodeB26(String codeB26) {
        this.codeB26 = codeB26;
    }

    public String getCodeB31() {
        return codeB31;
    }

    public void setCodeB31(String codeB31) {
        this.codeB31 = codeB31;
    }

    public String getCodeB32() {
        return codeB32;
    }

    public void setCodeB32(String codeB32) {
        this.codeB32 = codeB32;
    }

    public String getCodeB33() {
        return codeB33;
    }

    public void setCodeB33(String codeB33) {
        this.codeB33 = codeB33;
    }

    public String getCodeB34() {
        return codeB34;
    }

    public void setCodeB34(String codeB34) {
        this.codeB34 = codeB34;
    }

    public String getCodeB41() {
        return codeB41;
    }

    public void setCodeB41(String codeB41) {
        this.codeB41 = codeB41;
    }

    public String getCodeB42() {
        return codeB42;
    }

    public void setCodeB42(String codeB42) {
        this.codeB42 = codeB42;
    }

    public String getCodeB43() {
        return codeB43;
    }

    public void setCodeB43(String codeB43) {
        this.codeB43 = codeB43;
    }

    public String getCodeB44() {
        return codeB44;
    }

    public void setCodeB44(String codeB44) {
        this.codeB44 = codeB44;
    }
}
