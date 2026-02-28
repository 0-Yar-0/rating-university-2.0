package ru.ystu.rating.university.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.ystu.rating.university.dto.MultiClassParamsRequestDto;
import ru.ystu.rating.university.dto.ClassParamsBlockDto;
import ru.ystu.rating.university.dto.BParamsDto;

import java.util.List;
import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class RatingControllerIntegrationTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private ObjectMapper mapper;

    @Test
    void calcMulti_withExtendedFields_shouldReturnOk() throws Exception {
        BParamsDto params = new BParamsDto(
                2025,
                1.0,1.0,1.0,1.0,1.0,
                1.0,1.0,1.0,1.0,1.0,1.0,
                10.0,5.0,2.0,0.0,0.0,
                1.0,1.0,100.0,50.0,20.0,15.0,
                0.5,0.6,40.0,30.0,200.0,180.0,20.0
        );
        ClassParamsBlockDto block = new ClassParamsBlockDto("B", List.of(params), null);
        MultiClassParamsRequestDto req = new MultiClassParamsRequestDto(List.of(block));

        mvc.perform(post("/api/rating/calc-multi")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(req))
                .accept(MediaType.APPLICATION_JSON)
        )
                .andExpect(status().isOk());
    }
}
