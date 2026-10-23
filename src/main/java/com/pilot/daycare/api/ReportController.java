package com.pilot.daycare.api;

import com.pilot.daycare.model.SlotReport;
import com.pilot.daycare.service.RateLimiter;
import com.pilot.daycare.service.ReportService;
import com.pilot.daycare.service.ScreenshotEvaluator;
import jakarta.servlet.http.HttpSession;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@RestController
@RequestMapping("/api/reports")
public class ReportController {
    private final ReportService reports;
    private final ScreenshotEvaluator evaluator;
    private final RateLimiter limiter;

    public ReportController(ReportService reports, ScreenshotEvaluator evaluator, RateLimiter limiter) {
        this.reports = reports; this.evaluator = evaluator; this.limiter = limiter;
    }

    @PostMapping("/evaluate-screenshot")
    public Map<String, Object> evaluate(@RequestParam("file") MultipartFile file, HttpSession s) {
        require(s);
        Map<String, Boolean> found = evaluator.evaluate(file.getOriginalFilename());
        return Map.of(
                "filename", file.getOriginalFilename() == null ? "" : file.getOriginalFilename(),
                "fields", found,
                "missing", found.entrySet().stream().filter(e -> !e.getValue())
                        .map(Map.Entry::getKey).toList());
    }

    @PostMapping
    public Map<String, Object> create(@RequestBody SlotReport r, HttpSession s) {
        String id = require(s);
        if (!limiter.canReport(id)) {
            return Map.of("ok", false,
                    "error", "Rate limited: new accounts can post 1 report/hour and 3/day.");
        }
        r.reporterId = id;
        reports.create(r);
        return Map.of("ok", true, "id", r.id, "status", r.status.name());
    }

    @PostMapping("/{id}/confirm")
    public Map<String, Object> confirm(@PathVariable String id, HttpSession s) {
        require(s); reports.confirm(id);
        return Map.of("ok", true);
    }

    @PostMapping("/{id}/verify")
    public Map<String, Object> verify(@PathVariable String id, HttpSession s) {
        reports.verify(require(s), id);
        return Map.of("ok", true);
    }

    @PostMapping("/{id}/flag")
    public Map<String, Object> flag(@PathVariable String id, HttpSession s) {
        require(s); reports.flagFake(id);
        return Map.of("ok", true);
    }

    private String require(HttpSession s) {
        Object id = s.getAttribute("parentId");
        if (id == null) throw new IllegalStateException("unauthenticated");
        return (String) id;
    }
}