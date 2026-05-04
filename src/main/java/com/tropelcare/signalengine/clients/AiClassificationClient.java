package com.tropelcare.signalengine.clients;

public interface AiClassificationClient {

    AiClassificationResult classify(String rawContent);
}
