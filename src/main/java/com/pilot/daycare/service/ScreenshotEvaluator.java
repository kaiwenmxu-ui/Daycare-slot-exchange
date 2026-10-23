package com.pilot.daycare.service;

import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Demo stand-in for OCR. Production would call an image-extraction model.
 * Heuristic: filenames matching *infant*|*slot*|*daycare* are treated as
 * "rich" screenshots (provider + age group + start time detected); anything
 * else only gets provider+ageGroup. The UI pre-fills ONLY detected fields.
 */
@Service
public class ScreenshotEvaluator {

    public Map<String, Boolean> evaluate(String filename) {
        Map<String, Boolean> found = new LinkedHashMap<>();
        boolean looksRich = filename != null && filename.toLowerCase().matches(".*(infant|slot|daycare).*");
        found.put("providerName", looksRich);
        found.put("ageGroup",     looksRich);
        found.put("address",      false);
        found.put("startTime",    looksRich);
        found.put("endTime",      false);
        return found;
    }
}