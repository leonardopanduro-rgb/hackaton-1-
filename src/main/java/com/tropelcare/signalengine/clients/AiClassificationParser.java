package com.tropelcare.signalengine.clients;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tropelcare.signalengine.models.enums.Severity;
import com.tropelcare.signalengine.models.enums.SignalType;
import java.util.Locale;
import org.springframework.stereotype.Component;

@Component
public class AiClassificationParser {

    private final ObjectMapper objectMapper;

    public AiClassificationParser(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public AiClassificationResult parse(String modelContent) {
        String json = extractJson(modelContent);

        try {
            JsonNode root = objectMapper.readTree(json);
            SignalType signalType = SignalType.valueOf(requiredText(root, "signalType").toUpperCase(Locale.ROOT));
            Severity severity = Severity.valueOf(requiredText(root, "severity").toUpperCase(Locale.ROOT));
            String assignedUnit = requiredText(root, "assignedUnit");
            String recommendedAction = requiredText(root, "recommendedAction");

            if (!signalType.getAssignedUnit().equals(assignedUnit)) {
                throw new IllegalArgumentException("La unidad asignada no corresponde al tipo de senal");
            }

            return new AiClassificationResult(signalType, severity, assignedUnit, recommendedAction);
        } catch (Exception exception) {
            throw new IllegalArgumentException("La respuesta de IA no contiene un JSON valido", exception);
        }
    }

    private String extractJson(String content) {
        if (content == null) {
            throw new IllegalArgumentException("La respuesta de IA esta vacia");
        }

        int start = content.indexOf('{');
        int end = content.lastIndexOf('}');
        if (start < 0 || end < start) {
            throw new IllegalArgumentException("La respuesta de IA no contiene JSON");
        }
        return content.substring(start, end + 1);
    }

    private String requiredText(JsonNode root, String fieldName) {
        JsonNode value = root.get(fieldName);
        if (value == null || !value.isTextual() || value.asText().isBlank()) {
            throw new IllegalArgumentException("Campo faltante o invalido: " + fieldName);
        }
        return value.asText().trim();
    }
}
