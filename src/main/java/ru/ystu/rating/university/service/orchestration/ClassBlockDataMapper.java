package ru.ystu.rating.university.service.orchestration;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;
import ru.ystu.rating.university.dto.ClassParamsBlockDto;

import java.util.List;
import java.util.Map;

@Component
public class ClassBlockDataMapper {

    private final ObjectMapper mapper;

    public ClassBlockDataMapper(ObjectMapper mapper) {
        this.mapper = mapper;
    }

    public List<Map<String, Object>> toMapData(ClassParamsBlockDto block, String classType) {
        JavaType targetType = mapper.getTypeFactory()
                .constructCollectionType(List.class, Map.class);
        try {
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> mapped = (List<Map<String, Object>>) mapper.convertValue(block.data(), targetType);
            return mapped;
        } catch (Exception ex) {
            throw new IllegalArgumentException("Invalid parameters for class " + classType, ex);
        }
    }

    public List<Map<String, Object>> toRawMapData(ClassParamsBlockDto block, String classType) {
        JavaType targetType = mapper.getTypeFactory()
                .constructCollectionType(List.class, Map.class);
        try {
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> mapped = (List<Map<String, Object>>) mapper.convertValue(block.data(), targetType);
            return mapped;
        } catch (Exception ex) {
            throw new IllegalArgumentException("Invalid raw parameters for class " + classType, ex);
        }
    }

    public <T> List<T> toTypedData(ClassParamsBlockDto block, Class<T> elementType, String classType) {
        JavaType targetType = mapper.getTypeFactory()
                .constructCollectionType(List.class, elementType);
        try {
            return mapper.convertValue(block.data(), targetType);
        } catch (Exception ex) {
            throw new IllegalArgumentException("Invalid parameters for class " + classType, ex);
        }
    }
}
