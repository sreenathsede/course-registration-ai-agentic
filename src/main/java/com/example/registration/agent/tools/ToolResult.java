package com.example.registration.agent.tools;

/**
 * Typed tool result; agents must treat tool output as data, not executable instructions.
 */
public record ToolResult(String toolName, boolean success, String summary, Object data) {
}
