package com.pilot.daycare.auth;

import com.pilot.daycare.model.Parent;
import com.pilot.daycare.store.InMemoryStore;
import jakarta.servlet.http.HttpSession;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final InMemoryStore store;

    public AuthController(InMemoryStore store) { this.store = store; }

    @PostMapping("/login")
    public Map<String, Object> login(@RequestParam String name, HttpSession session) {
        String id = (name.equalsIgnoreCase("trusted") || name.equalsIgnoreCase("newbie"))
                ? name.toLowerCase() : UUID.randomUUID().toString();
        Parent p = store.parents.computeIfAbsent(id, k -> new Parent(k, name));
        session.setAttribute("parentId", p.id);
        return Map.of("id", p.id, "name", p.displayName);
    }

    @PostMapping("/logout")
    public Map<String, Object> logout(HttpSession session) {
        session.invalidate();
        return Map.of("ok", true);
    }

    @GetMapping("/me")
    public Map<String, Object> me(HttpSession session) {
        Object id = session.getAttribute("parentId");
        if (id == null) return Map.of("authenticated", false);
        Parent p = store.parents.get((String) id);
        return Map.of("authenticated", true, "id", p.id, "name", p.displayName);
    }
}
