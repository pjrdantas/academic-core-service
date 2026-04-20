package br.com.escola.academic.adapter.in.web;

import br.com.escola.academic.adapter.out.persistence.entity.DlqEventEntity;
import br.com.escola.academic.adapter.out.persistence.repository.DlqEventJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/dlq")
@RequiredArgsConstructor
public class DlqController {

    private final DlqEventJpaRepository repository;

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

        // 🔥 filtro completo
        if (reprocessed != null && start != null && end != null) {
            return repository.findByReprocessedAndCreatedAtBetween(
                    reprocessed, start, end, pageable
            );
        }

        if (reprocessed != null) {
            return repository.findByReprocessed(reprocessed, pageable);
        }

        if (start != null && end != null) {
            return repository.findByCreatedAtBetween(start, end, pageable);
        }

        return repository.findAll(pageable);
    }
}