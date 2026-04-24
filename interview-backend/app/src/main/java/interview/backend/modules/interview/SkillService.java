package interview.backend.modules.interview;

import interview.backend.modules.interview.model.InterviewDirection;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.EnumMap;
import java.util.Map;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

@Service
public class SkillService {

    private final Map<InterviewDirection, String> cache = new EnumMap<>(InterviewDirection.class);

    public String loadDefinition(InterviewDirection direction) {
        return cache.computeIfAbsent(direction, this::readSkill);
    }

    private String readSkill(InterviewDirection direction) {
        var path = "skills/" + direction.name() + "/SKILL.md";
        try (var inputStream = new ClassPathResource(path).getInputStream()) {
            return new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException ex) {
            return "# Skill\n\nNo skill file configured yet.";
        }
    }
}
