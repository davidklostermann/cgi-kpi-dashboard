/**
 * Project AI orchestration. Reads project facts only via {@code kpi.reader}
 * (Architecture Spine — ai module). External model calls stay server-side;
 * Gemini is the default implementation provider; mock is available only when
 * {@code app.ai.provider=mock} (test profile). Runtime API key, model, and enabled
 * state are resolved per user from the database.
 */
package com.cgi.kpi.dashboard.ai;
