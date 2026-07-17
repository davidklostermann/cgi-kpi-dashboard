package com.cgi.kpi.dashboard.ai.config;



import org.springframework.boot.context.properties.ConfigurationProperties;



@ConfigurationProperties(prefix = "app.ai")

public class AiProperties {



    private boolean enabled = true;

    private String provider = "mock";

    private String model = "local-mock";

    private int timeoutMs = 8000;

    /** Loaded only from environment / Spring property — never hardcode. */

    private String apiKey = "";

    private String geminiApiBaseUrl = "https://generativelanguage.googleapis.com";



    public boolean isEnabled() {

        return enabled;

    }



    public void setEnabled(boolean enabled) {

        this.enabled = enabled;

    }



    public String getProvider() {

        return provider;

    }



    public void setProvider(String provider) {

        this.provider = provider;

    }



    public String getModel() {

        return model;

    }



    public void setModel(String model) {

        this.model = model;

    }



    public int getTimeoutMs() {

        return timeoutMs;

    }



    public void setTimeoutMs(int timeoutMs) {

        this.timeoutMs = timeoutMs;

    }



    public String getApiKey() {

        return apiKey;

    }



    public void setApiKey(String apiKey) {

        this.apiKey = apiKey;

    }



    public String getGeminiApiBaseUrl() {

        return geminiApiBaseUrl;

    }



    public void setGeminiApiBaseUrl(String geminiApiBaseUrl) {

        this.geminiApiBaseUrl = geminiApiBaseUrl;

    }



    public boolean hasApiKey() {

        return apiKey != null && !apiKey.isBlank();

    }



    public boolean isGeminiProvider() {

        return "gemini".equalsIgnoreCase(provider);

    }



    public String getConfiguredModel() {

        return model == null ? "" : model.trim();

    }



    public boolean hasGeminiModel() {

        String configured = getConfiguredModel();

        return !configured.isBlank() && !"local-mock".equals(configured);

    }



    /**

     * Resolves the Gemini model name. Requires an explicit {@code APP_AI_MODEL} when provider is gemini.

     */

    public String requireGeminiModel() {

        if (!hasGeminiModel()) {

            throw new IllegalStateException(

                    "APP_AI_MODEL must be set to a valid Gemini model when APP_AI_PROVIDER=gemini "

                            + "(local-mock is only valid for provider=mock).");

        }

        return getConfiguredModel();

    }

}


