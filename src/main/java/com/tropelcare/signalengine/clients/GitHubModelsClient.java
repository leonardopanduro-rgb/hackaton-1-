package com.tropelcare.signalengine.clients;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class GitHubModelsClient implements AiClassificationClient {

    private static final String SYSTEM_PROMPT = """
            Eres el sistema de clasificacion de senales del TropelCare Signal Engine, desarrollado por Tuckersoft.
            Recibes senales emitidas por criaturas digitales llamadas Tropeles y debes clasificarlas.
            Responde UNICAMENTE con este JSON en una sola linea, sin texto adicional, sin markdown, sin bloques de codigo:
            {"signalType":"<TIPO>","severity":"<GRAVEDAD>","assignedUnit":"<UNIDAD>","recommendedAction":"<accion breve y concreta en espanol>"}

            Tipos validos: HAMBRE, ABANDONO, MUTACION, FUGA, CONFLICTO, REPRODUCCION_MASIVA, SENAL_CORRUPTA
            Gravedades validas: LEVE, MODERADO, GRAVE, CRITICO
            Unidades validas: Laboratorio de Nutricion, Unidad de Bienestar, Division Genetica, Equipo de Contencion, Consejo de Mediacion, Control Demografico, Archivo de Senales

            Reglas:
            - HAMBRE -> Laboratorio de Nutricion: escasez de nutrientes, intento de morder objetos digitales.
            - ABANDONO -> Unidad de Bienestar: angustia por falta de interaccion, silencio prolongado.
            - MUTACION -> Division Genetica: cambios fisicos, brillo anomalo, glitch corporal, duplicacion.
            - FUGA -> Equipo de Contencion: intento de abandonar el sector, zonas prohibidas.
            - CONFLICTO -> Consejo de Mediacion: pelea entre Tropeles, invasion de territorio.
            - REPRODUCCION_MASIVA -> Control Demografico: reproduccion no planificada, clonacion accidental.
            - SENAL_CORRUPTA -> Archivo de Senales: senal ininteligible, estatica, datos corruptos.

            Gravedades:
            - LEVE: sin riesgo inmediato para el Tropel o el sector.
            - MODERADO: requiere atencion, pero no es urgente.
            - GRAVE: afecta al Tropel o al sector de forma importante.
            - CRITICO: riesgo de mutacion irreversible, fuga masiva o colapso del sector.
            """;

    private final RestClient restClient;
    private final ObjectMapper objectMapper;
    private final AiClassificationParser parser;
    private final String token;
    private final String modelId;

    public GitHubModelsClient(RestClient.Builder restClientBuilder,
                              ObjectMapper objectMapper,
                              AiClassificationParser parser,
                              @Value("${github.models.url}") String modelsUrl,
                              @Value("${github.token}") String token,
                              @Value("${github.models.model-id}") String modelId) {
        this.restClient = restClientBuilder.baseUrl(modelsUrl).build();
        this.objectMapper = objectMapper;
        this.parser = parser;
        this.token = token;
        this.modelId = modelId;
    }

    @Override
    public AiClassificationResult classify(String rawContent) {
        ObjectNode body = objectMapper.createObjectNode();
        body.put("model", modelId);
        body.put("temperature", 0.1);
        body.put("max_tokens", 300);

        ArrayNode messages = objectMapper.createArrayNode();
        messages.add(message("system", SYSTEM_PROMPT));
        messages.add(message("user", rawContent));
        body.set("messages", messages);

        JsonNode response = restClient.post()
                .uri("/chat/completions")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .body(body)
                .retrieve()
                .body(JsonNode.class);

        String content = response
                .path("choices")
                .path(0)
                .path("message")
                .path("content")
                .asText(null);

        if (content == null || content.isBlank()) {
            throw new IllegalArgumentException("GitHub Models no devolvio contenido");
        }

        return parser.parse(content);
    }

    private ObjectNode message(String role, String content) {
        ObjectNode node = objectMapper.createObjectNode();
        node.put("role", role);
        node.put("content", content);
        return node;
    }
}
