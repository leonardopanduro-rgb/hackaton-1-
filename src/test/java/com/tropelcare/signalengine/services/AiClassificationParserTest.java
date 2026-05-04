package com.tropelcare.signalengine.services;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tropelcare.signalengine.clients.AiClassificationParser;
import com.tropelcare.signalengine.clients.AiClassificationResult;
import com.tropelcare.signalengine.models.enums.Severity;
import com.tropelcare.signalengine.models.enums.SignalType;
import org.junit.jupiter.api.Test;

class AiClassificationParserTest {

    @Test
    void parsesJsonEvenWhenModelAddsExtraText() {
        AiClassificationParser parser = new AiClassificationParser(new ObjectMapper());
        String content = """
                Aqui va texto extra.
                {"signalType":"MUTACION","severity":"GRAVE","assignedUnit":"Division Genetica","recommendedAction":"Aislar y observar el brillo anomalo."}
                Texto final.
                """;

        AiClassificationResult result = parser.parse(content);

        assertEquals(SignalType.MUTACION, result.signalType());
        assertEquals(Severity.GRAVE, result.severity());
        assertEquals("Division Genetica", result.assignedUnit());
    }
}
