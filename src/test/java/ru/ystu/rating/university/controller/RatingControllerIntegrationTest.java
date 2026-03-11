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
import ru.ystu.rating.university.dto.BParamsDto;
import ru.ystu.rating.university.security.CustomUserDetails;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;

@SpringBootTest
@AutoConfigureMockMvc
public class RatingControllerIntegrationTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private ObjectMapper mapper;

    @Test
    void calcMulti_withExtendedFields_shouldReturnOk() throws Exception {
        Map<String, Object> raw = new HashMap<>();
        raw.put("year", 2025);
        raw.put("ENa", 1.0);
        raw.put("ENb", 1.0);
        raw.put("ENc", 1.0);
        raw.put("Eb", 1.0);
        raw.put("Ec", 1.0);
        raw.put("beta121", 1.0);
        raw.put("beta122", 1.0);
        raw.put("beta131", 1.0);
        raw.put("beta132", 1.0);
        raw.put("beta211", 1.0);
        raw.put("beta212", 1.0);
        raw.put("NBP", 10.0);
        raw.put("NMP", 5.0);
        raw.put("ACP", 2.0);
        raw.put("OPC", 0.0);
        raw.put("ACC", 0.0);
        raw.put("PKP", 1.0);
        raw.put("PPP", 1.0);
        raw.put("NP", 100.0);
        raw.put("NOA", 50.0);
        raw.put("NAP", 20.0);
        raw.put("B25_o", 15.0);
        raw.put("B26_o", 0.5);
        raw.put("UT", 0.6);
        raw.put("DO", 40.0);
        raw.put("N", 30.0);
        raw.put("Npr", 200.0);
        raw.put("VO", 180.0);
        raw.put("PO", 20.0);

        BParamsDto params = mapper.convertValue(raw, BParamsDto.class);
        ClassParamsBlockDto block = new ClassParamsBlockDto("B", List.of(params), null);
        MultiClassParamsRequestDto req = new MultiClassParamsRequestDto(List.of(block));
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

        mvc.perform(post("/api/rating/calc-multi")
            .with(authentication(auth))
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(req))
                .accept(MediaType.APPLICATION_JSON)
        )
                .andExpect(status().isOk());
    }
}
