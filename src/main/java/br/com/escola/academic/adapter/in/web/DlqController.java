package br.com.escola.academic.adapter.in.web;

import br.com.escola.academic.adapter.out.persistence.entity.DlqEventEntity;
import br.com.escola.academic.adapter.out.persistence.repository.DlqEventJpaRepository;
import br.com.escola.academic.infrastructure.messaging.KafkaPayloadNormalizer;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.UUID;

@RestController
@RequestMapping("/dlq")
@RequiredArgsConstructor
public class DlqController {

    private final DlqEventJpaRepository repository;
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final KafkaPayloadNormalizer payloadNormalizer;

    // =========================================
    // 📋 LISTAR — com filtros opcionais
    // =========================================
    @GetMapping
    public Page<DlqEventEntity> listar(
            @RequestParam(required = false) Boolean reprocessed,
            @RequestParam(required = false) LocalDateTime start,
            @RequestParam(required = false) LocalDateTime end,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {

        Pageable pageable = PageRequest.of(
                page,
                size,
                Sort.by(Sort.Direction.DESC, "createdAt")
        );

        if (reprocessed != null && start != null && end != null) {
            return repository.findByReprocessedAndCreatedAtBetween(reprocessed, start, end, pageable);
        }

        if (reprocessed != null) {
            return repository.findByReprocessed(reprocessed, pageable);
        }

        if (start != null && end != null) {
            return repository.findByCreatedAtBetween(start, end, pageable);
        }

        return repository.findAll(pageable);
    }

    // =========================================
    // ♻️ REPROCESSAR — devolve evento ao tópico MATRICULA
    // Marca reprocessed=true para não ser pego pelo scheduler
    // =========================================
    @PostMapping("/{id}/reprocess")
    public ResponseEntity<Void> reprocess(@PathVariable UUID id) {

        DlqEventEntity event = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Evento DLQ não encontrado: " + id));

        if (event.isReprocessed()) {
            return ResponseEntity.noContent().build(); // já reprocessado — idempotente
        }

        // ✅ Republica o payload original no tópico MATRICULA
        String normalizedPayload = payloadNormalizer.normalizeToJsonObject(event.getPayload());
        kafkaTemplate.send("MATRICULA", normalizedPayload);

        // ✅ Marca como reprocessado para evitar que o scheduler retire novamente
        event.setReprocessed(true);
        repository.save(event);

        return ResponseEntity.ok().build();
    }
}