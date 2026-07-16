/**
 * Project AI orchestration. Reads project facts only via {@code kpi.reader}
 * (Architecture Spine — ai module). External model calls stay server-side;
 * default provider is a local mock for development and CI.
 */
package com.cgi.kpi.dashboard.ai;
