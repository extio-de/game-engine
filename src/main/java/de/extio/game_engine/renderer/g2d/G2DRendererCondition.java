package de.extio.game_engine.renderer.g2d;

import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.env.Environment;
import org.springframework.core.type.AnnotatedTypeMetadata;

/**
 * Condition that matches when the renderer is enabled and the strategy is g2d.
 *
 * Behavior mirrors the previous Spring Boot conditions:
 * - game-engine.renderer.enabled: when present must be "true"; when missing it matches (default true).
 * - game-engine.renderer.strategy: when present must equal "g2d"; when missing it matches.
 */
public class G2DRendererCondition implements Condition {

    @Override
    public boolean matches(final ConditionContext context, final AnnotatedTypeMetadata metadata) {
        final Environment env = context.getEnvironment();

        final String enabled = env.getProperty("game-engine.renderer.enabled");
        if (enabled != null && !Boolean.parseBoolean(enabled)) {
            return false;
        }

        final String strategy = env.getProperty("game-engine.renderer.strategy");
        if (strategy != null) {
            return "g2d".equalsIgnoreCase(strategy);
        }

        return true;
    }
}
