package org.rush_road;

import java.awt.Color;
import java.awt.Point;
import java.awt.event.ActionListener;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.Timer;

public class AnimationUtil {
    public static void pulseComponent(JComponent component, int duration) {
        Timer timer = new Timer(50, null);
        float[] scale = { 1.0f };
        boolean[] increasing = { true };

        ActionListener animate = e -> {
            if (increasing[0]) {
                scale[0] += 0.03f;
                if (scale[0] >= 1.2f)
                    increasing[0] = false;
            } else {
                scale[0] -= 0.03f;
                if (scale[0] <= 1.0f)
                    increasing[0] = true;
            }

            if (component instanceof JLabel) {
                JLabel label = (JLabel) component;
                label.setFont(label.getFont().deriveFont(label.getFont().getSize() * scale[0]));
            }
            component.revalidate();
            component.repaint();
        };

        timer.addActionListener(animate);
        timer.start();

        Timer stopTimer = new Timer(duration, e -> timer.stop());
        stopTimer.setRepeats(false);
        stopTimer.start();
    }

    public static void slideIn(JComponent component, int startX, int endX, int duration) {
        Timer timer = new Timer(16, null);
        int distance = endX - startX;
        int steps = duration / 16;
        float[] step = { 0 };

        Point original = component.getLocation();
        component.setLocation(startX, original.y);

        timer.addActionListener(e -> {
            step[0]++;
            float progress = Math.min(1, step[0] / steps);
            // Smoother easing function
            float position = (float) (1 - Math.pow(1 - progress, 4));
            int newX = startX + (int) (distance * position);
            component.setLocation(newX, original.y);

            if (progress >= 1) {
                timer.stop();
                component.setLocation(endX, original.y);
            }
        });

        timer.start();
    }

    public static void fadeIn(JComponent component, int duration) {
        Timer timer = new Timer(16, null);
        float[] opacity = { 0.0f };

        component.setOpaque(false);

        timer.addActionListener(e -> {
            opacity[0] += 0.05f;
            if (opacity[0] >= 1.0f) {
                opacity[0] = 1.0f;
                timer.stop();
            }
            component.setBackground(new Color(
                    component.getBackground().getRed(),
                    component.getBackground().getGreen(),
                    component.getBackground().getBlue(),
                    (int) (255 * opacity[0])));
            component.repaint();
        });

        timer.start();
    }
}