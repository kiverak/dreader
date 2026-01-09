package dreadernewsparser.controller;

import dto.SourceDetails;
import dreadernewsparser.service.SourceService;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@AllArgsConstructor
@RequestMapping("/api/source")
public class SourceController {

    private final SourceService sourceService;

    @GetMapping
    public ResponseEntity<List<SourceDetails>> getAllSources() {
        return ResponseEntity.ok(sourceService.findAllSourceDetails());
    }

    @GetMapping("/{id}")
    public ResponseEntity<SourceDetails> getSourceById(@PathVariable Long id) {
        return sourceService.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<SourceDetails> createSource(@RequestBody SourceDetails sourceDetails) {
        return ResponseEntity.status(HttpStatus.CREATED).body(sourceService.save(sourceDetails));
    }

    @PutMapping("/{id}")
    public ResponseEntity<SourceDetails> updateSource(@PathVariable Long id, @RequestBody SourceDetails sourceDetails) {
        try {
            return ResponseEntity.ok(sourceService.update(id, sourceDetails));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteSource(@PathVariable Long id) {
        sourceService.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}