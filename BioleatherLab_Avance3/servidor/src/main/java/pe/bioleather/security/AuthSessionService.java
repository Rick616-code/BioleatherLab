package pe.bioleather.security;

import jakarta.servlet.http.HttpServletRequest;
import java.security.SecureRandom;
import java.time.Instant;
import java.util.Base64;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
@Service
public class AuthSessionService {
    public record Session(long id, String rol, Instant vence) {}
    public record IssuedSession(Session session, String token) {}
    private final JdbcTemplate db;
    private final SecureRandom random = new SecureRandom();
    private final Map<String, Session> sessions = new ConcurrentHashMap<>();

    public AuthSessionService(JdbcTemplate db) { this.db = db; }

    public IssuedSession open(long id, String role) {
        byte[] bytes = new byte[32];
        random.nextBytes(bytes);
        String token = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
        Session session = new Session(id, role, Instant.now().plusSeconds(8 * 3600));
        sessions.put(token, session);
        return new IssuedSession(session, token);
    }

    public Session requireUser(HttpServletRequest request) {
        String header = request.getHeader("Authorization");
        String token = header != null && header.startsWith("Bearer ") ? header.substring(7) : "";
        Session session = sessions.get(token);
        if (session == null || !Instant.now().isBefore(session.vence())) {
            sessions.remove(token);
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Inicia sesión");
        }
        try {
            Boolean active = db.queryForObject("SELECT activo FROM usuarios WHERE id=?", Boolean.class, session.id());
            if (!Boolean.TRUE.equals(active)) throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Usuario desactivado");
        } catch (EmptyResultDataAccessException e) {
            sessions.remove(token);
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Sesión no válida");
        }
        return session;
    }

    public Session requireAdmin(HttpServletRequest request) {
        Session session = requireUser(request);
        if (!"ADMIN".equals(session.rol())) throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Solo administrador");
        return session;
    }

    public void logout(HttpServletRequest request) {
        String header = request.getHeader("Authorization");
        if (header != null && header.startsWith("Bearer ")) sessions.remove(header.substring(7));
    }
}
