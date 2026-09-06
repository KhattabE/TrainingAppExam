package app.dto;

import java.util.List;

public record GeminiRequestDTO(List<GeminiContentDTO> contents) {
}
