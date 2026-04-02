package ru.ystu.rating.university.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.test.web.servlet.MockMvc;
import ru.ystu.rating.university.dto.MultiClassParamsRequestDto;
import ru.ystu.rating.university.dto.ClassParamsBlockDto;
import ru.ystu.rating.university.security.CustomUserDetails;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;

@SpringBootTest(properties = {
    "spring.datasource.url=jdbc:h2:mem:rating_test",
    "spring.datasource.driver-class-name=org.h2.Driver",
    "spring.datasource.username=sa",
    "spring.datasource.password=",
    "spring.datasource.hikari.data-source-properties.MODE=PostgreSQL",
    "spring.datasource.hikari.data-source-properties.DATABASE_TO_LOWER=TRUE",
    "spring.datasource.hikari.connection-init-sql=CREATE DOMAIN IF NOT EXISTS JSONB AS JSON\\;CREATE ALIAS IF NOT EXISTS JSONB_TYPEOF FOR 'ru.ystu.rating.university.H2JsonFunctions.jsonbTypeof'",
    "spring.liquibase.enabled=true",
    "spring.liquibase.change-log=classpath:db/changelog/db.changelog-test.xml",
    "spring.jpa.hibernate.ddl-auto=validate",
    "spring.jpa.open-in-view=false"
})
@AutoConfigureMockMvc
public class RatingControllerIntegrationTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private ObjectMapper mapper;

    @Test
    void calcMulti_withExtendedFields_shouldReturnOk() throws Exception {
        Map<String, Object> aRaw = new HashMap<>();
        aRaw.put("year", 2025);
        aRaw.put("PNo", 905.0);
        aRaw.put("PNv", 0.0);
        aRaw.put("PNz", 345.0);
        aRaw.put("DIo", 19.0);
        aRaw.put("DIv", 0.0);
        aRaw.put("DIz", 3.0);
        aRaw.put("PRF", 12.0);
        aRaw.put("KCO", 12.0);
        aRaw.put("ZKN", 2.0);
        aRaw.put("CHVA", 2.02);
        aRaw.put("CHPA", 2.94);
        aRaw.put("CZ", 2.0);
        aRaw.put("CV", 2.0);
        aRaw.put("A23RF", 0.5);
        aRaw.put("WL2022", 60.0);
        aRaw.put("WL2023", 70.0);
        aRaw.put("WL2024", 70.0);
        aRaw.put("NPR2022", 270.1);
        aRaw.put("NPR2023", 278.9);
        aRaw.put("NPR2024", 278.5);
        aRaw.put("DN2022", 38567.4);
        aRaw.put("DN2023", 96735.9);
        aRaw.put("DN2024", 483281.1);
        aRaw.put("RDN2022", 100.0);
        aRaw.put("RDN2023", 100.0);
        aRaw.put("RDN2024", 100.0);
        aRaw.put("IA2022", 1.0);
        aRaw.put("IA2023", 2.0);
        aRaw.put("IA2024", 2.0);
        aRaw.put("ASP2022", 50.0);
        aRaw.put("ASP2023", 50.0);
        aRaw.put("ASP2024", 50.0);
        aRaw.put("OD2022", 760316.0);
        aRaw.put("OD2023", 870778.8);
        aRaw.put("OD2024", 1348285.7);
        aRaw.put("PFN", 20.0);
        aRaw.put("ASO", 5.0);
        aRaw.put("DS", 0.5);

        Map<String, Object> bRaw = new HashMap<>();
        bRaw.put("year", 2025);
        bRaw.put("ENa", 1.0);
        bRaw.put("ENb", 1.0);
        bRaw.put("ENc", 1.0);
        bRaw.put("Eb", 1.0);
        bRaw.put("Ec", 1.0);
        bRaw.put("beta121", 1.0);
        bRaw.put("beta122", 1.0);
        bRaw.put("beta131", 1.0);
        bRaw.put("beta132", 1.0);
        bRaw.put("beta211", 1.0);
        bRaw.put("beta212", 1.0);
        bRaw.put("NBP", 10.0);
        bRaw.put("NMP", 5.0);
        bRaw.put("ACP", 2.0);
        bRaw.put("OPC", 0.0);
        bRaw.put("ACC", 0.0);
        bRaw.put("PKP", 1.0);
        bRaw.put("PPP", 1.0);
        bRaw.put("NP", 100.0);
        bRaw.put("NOA", 50.0);
        bRaw.put("NAP", 20.0);
        bRaw.put("B25_o", 15.0);
        bRaw.put("B26_o", 0.5);
        bRaw.put("UT", 0.6);
        bRaw.put("DO", 40.0);
        bRaw.put("N", 30.0);
        bRaw.put("Npr", 200.0);
        bRaw.put("VO", 180.0);
        bRaw.put("PO", 20.0);

        Map<String, Object> mRaw = new HashMap<>();
        mRaw.put("year", 2025);
        mRaw.put("ZMD", 5.0);
        mRaw.put("ZM", 100.0);
        mRaw.put("CHZ", 8.0);
        mRaw.put("ZPK", 2.0);
        mRaw.put("MDP", 0.2);
        mRaw.put("PRF", 12.0);
        mRaw.put("KCO", 12.0);
        mRaw.put("M21_o", 0.7);
        mRaw.put("M22_o", 0.1);
        mRaw.put("M23_o", 0.1);
        mRaw.put("M31_o", 2.0);
        mRaw.put("N", 30.0);
        mRaw.put("Npr", 200.0);
        mRaw.put("VO", 180.0);
        mRaw.put("PO", 20.0);
        mRaw.put("NR2023", 0.2);
        mRaw.put("NR2024", 0.2);
        mRaw.put("NR2025", 0.9);

        ClassParamsBlockDto aBlock = new ClassParamsBlockDto("A", List.of(aRaw), null);
        ClassParamsBlockDto bBlock = new ClassParamsBlockDto("B", List.of(bRaw), null);
        ClassParamsBlockDto mBlock = new ClassParamsBlockDto("M", List.of(mRaw), null);
        MultiClassParamsRequestDto req = new MultiClassParamsRequestDto(List.of(aBlock, bBlock, mBlock));
        CustomUserDetails principal = new CustomUserDetails(
            1L,
            "admin@email.ru",
            "$2a$10$leRsVyFIdcAe5sHhmuPuWuPycjXQZz4CvDwsPAkQiTac6wil8/EzW",
            "ADMIN"
        );
        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
            principal,
            principal.getPassword(),
            principal.getAuthorities()
        );

        String jsonBody = Objects.requireNonNull(mapper.writeValueAsString(req));

        mvc.perform(post("/api/rating/calc-multi")
            .with(Objects.requireNonNull(authentication(auth)))
                .contentType(Objects.requireNonNull(MediaType.APPLICATION_JSON))
                .content(jsonBody)
                .accept(Objects.requireNonNull(MediaType.APPLICATION_JSON))
        )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.classes.length()").value(3))
                .andExpect(jsonPath("$.classes[0].classType").value("A"))
                .andExpect(jsonPath("$.classes[1].classType").value("B"))
                .andExpect(jsonPath("$.classes[2].classType").value("M"));
    }
}
