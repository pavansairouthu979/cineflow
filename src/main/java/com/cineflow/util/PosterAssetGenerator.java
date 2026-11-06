package com.cineflow.util;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.geom.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Random;

/**
 * Generates high-resolution, cinema-grade PNG posters for movies
 * with rich Apple Liquid Glass overlays and cinematic gradients.
 */
public class PosterAssetGenerator {

    private static final int WIDTH = 400;
    private static final int HEIGHT = 600;

    public static void main(String[] args) {
        File outDir = new File("src/main/resources/images/posters");
        if (!outDir.exists()) {
            outDir.mkdirs();
        }

        generateInception(new File(outDir, "inception.png"));
        generateInterstellar(new File(outDir, "interstellar.png"));
        generateTheDarkKnight(new File(outDir, "the_dark_knight.png"));
        generateOppenheimer(new File(outDir, "oppenheimer.png"));
        generateRRR(new File(outDir, "rrr.png"));
        generateKalki(new File(outDir, "kalki_2898_ad.png"));
        generateJawan(new File(outDir, "jawan.png"));
        generateStree2(new File(outDir, "stree_2.png"));
        generateDefault(new File(outDir, "default_poster.png"));

        System.out.println("All 9 cinematic movie posters successfully generated in: " + outDir.getAbsolutePath());
    }

    private static Graphics2D createHighQualityGraphics(BufferedImage image) {
        Graphics2D g2d = image.createGraphics();
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
        return g2d;
    }

    private static void applyLiquidGlassOverlay(Graphics2D g2d) {
        // Specular highlight gradient at the top (Apple Glass sheen)
        GradientPaint sheen = new GradientPaint(
                0, 0, new Color(255, 255, 255, 75),
                0, 180, new Color(255, 255, 255, 0)
        );
        g2d.setPaint(sheen);
        g2d.fillRoundRect(8, 8, WIDTH - 16, 160, 24, 24);

        // Thin luminous border
        g2d.setColor(new Color(255, 255, 255, 45));
        g2d.setStroke(new BasicStroke(1.5f));
        g2d.drawRoundRect(6, 6, WIDTH - 12, HEIGHT - 12, 28, 28);

        // Apple 4K IMAX badge at top right
        drawGlassBadge(g2d, WIDTH - 95, 22, 75, 24, "IMAX 4K", new Color(10, 132, 255, 180));
    }

    private static void drawGlassBadge(Graphics2D g2d, int x, int y, int w, int h, String text, Color accent) {
        g2d.setColor(new Color(15, 20, 30, 200));
        g2d.fillRoundRect(x, y, w, h, 12, 12);
        g2d.setColor(accent);
        g2d.setStroke(new BasicStroke(1.2f));
        g2d.drawRoundRect(x, y, w, h, 12, 12);

        g2d.setColor(Color.WHITE);
        g2d.setFont(new Font("Segoe UI", Font.BOLD, 10));
        FontMetrics fm = g2d.getFontMetrics();
        int tx = x + (w - fm.stringWidth(text)) / 2;
        int ty = y + (h - fm.getHeight()) / 2 + fm.getAscent();
        g2d.drawString(text, tx, ty);
    }

    private static void drawRatingBadge(Graphics2D g2d, int x, int y, String rating) {
        drawGlassBadge(g2d, x, y, 68, 24, "★ " + rating, new Color(255, 214, 10, 220));
    }

    // 1. INCEPTION
    private static void generateInception(File file) {
        BufferedImage img = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = createHighQualityGraphics(img);

        // Background: Deep dream indigo & abyss teal
        GradientPaint bg = new GradientPaint(
                0, 0, new Color(10, 15, 35),
                WIDTH, HEIGHT, new Color(4, 20, 36)
        );
        g2d.setPaint(bg);
        g2d.fillRect(0, 0, WIDTH, HEIGHT);

        // Surreal folding city grid illusion lines
        g2d.setStroke(new BasicStroke(1.5f));
        for (int i = 0; i < 12; i++) {
            g2d.setColor(new Color(0, 210, 255, 20 + i * 8));
            int y1 = 180 + i * 16;
            int y2 = 320 - i * 12;
            g2d.drawLine(40, y1, WIDTH - 40, y2);
            g2d.drawLine(40 + i * 25, 140, WIDTH / 2, 380);
            g2d.drawLine(WIDTH - 40 - i * 25, 140, WIDTH / 2, 380);
        }

        // Center spinning top totem emblem
        g2d.setColor(new Color(0, 240, 255, 180));
        int cx = WIDTH / 2;
        int cy = 270;
        int[] tx = { cx, cx - 35, cx + 35 };
        int[] ty = { cy - 45, cy + 25, cy + 25 };
        g2d.fillPolygon(tx, ty, 3);
        g2d.fillOval(cx - 38, cy + 20, 76, 20);
        g2d.fillRect(cx - 4, cy - 65, 8, 25);
        g2d.fillOval(cx - 6, cy - 72, 12, 12);

        // Director credit
        g2d.setColor(new Color(180, 210, 240));
        g2d.setFont(new Font("Segoe UI", Font.BOLD, 11));
        g2d.drawString("A FILM BY CHRISTOPHER NOLAN", 100, 430);

        // Title
        g2d.setColor(Color.WHITE);
        g2d.setFont(new Font("Segoe UI", Font.BOLD, 36));
        g2d.drawString("INCEPTION", 95, 475);

        // Tagline
        g2d.setColor(new Color(120, 200, 255));
        g2d.setFont(new Font("Segoe UI", Font.ITALIC, 13));
        g2d.drawString("Your mind is the scene of the crime.", 85, 508);

        // Genre pill
        drawGlassBadge(g2d, 30, 540, 80, 26, "SCI-FI", new Color(0, 210, 255));
        drawGlassBadge(g2d, 120, 540, 80, 26, "ENGLISH", new Color(168, 85, 247));
        drawRatingBadge(g2d, 22, 22, "8.8");

        applyLiquidGlassOverlay(g2d);
        g2d.dispose();
        saveImage(img, file);
    }

    // 2. INTERSTELLAR
    private static void generateInterstellar(File file) {
        BufferedImage img = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = createHighQualityGraphics(img);

        // Cosmic abyss background
        GradientPaint bg = new GradientPaint(
                0, 0, new Color(5, 5, 16),
                WIDTH, HEIGHT, new Color(18, 12, 35)
        );
        g2d.setPaint(bg);
        g2d.fillRect(0, 0, WIDTH, HEIGHT);

        // Starfield particles
        Random rand = new Random(42);
        for (int i = 0; i < 180; i++) {
            int sx = rand.nextInt(WIDTH);
            int sy = rand.nextInt(HEIGHT);
            int size = rand.nextInt(3) + 1;
            int alpha = rand.nextInt(180) + 75;
            g2d.setColor(new Color(255, 255, 255, alpha));
            g2d.fillOval(sx, sy, size, size);
        }

        // Golden Gargantua Black Hole accretion disk
        int cx = WIDTH / 2;
        int cy = 250;
        RadialGradientPaint holeGlow = new RadialGradientPaint(
                cx, cy, 140,
                new float[]{ 0.0f, 0.35f, 0.7f, 1.0f },
                new Color[]{
                        new Color(255, 220, 130, 220),
                        new Color(255, 140, 40, 160),
                        new Color(180, 50, 20, 80),
                        new Color(0, 0, 0, 0)
                }
        );
        g2d.setPaint(holeGlow);
        g2d.fillOval(cx - 130, cy - 130, 260, 260);

        // Horizontal warping ring
        g2d.setColor(new Color(255, 215, 120, 220));
        g2d.setStroke(new BasicStroke(6f));
        g2d.drawArc(cx - 150, cy - 25, 300, 50, 0, 360);

        // Event horizon (pitch black core)
        g2d.setColor(new Color(4, 4, 10));
        g2d.fillOval(cx - 55, cy - 55, 110, 110);
        g2d.setColor(new Color(255, 240, 200, 180));
        g2d.setStroke(new BasicStroke(2f));
        g2d.drawOval(cx - 55, cy - 55, 110, 110);

        // Title & text
        g2d.setColor(new Color(210, 225, 245));
        g2d.setFont(new Font("Segoe UI", Font.BOLD, 11));
        g2d.drawString("MATTHEW McCONAUGHEY • ANNE HATHAWAY", 60, 430);

        g2d.setColor(Color.WHITE);
        g2d.setFont(new Font("Segoe UI", Font.BOLD, 33));
        g2d.drawString("INTERSTELLAR", 70, 475);

        g2d.setColor(new Color(255, 200, 120));
        g2d.setFont(new Font("Segoe UI", Font.ITALIC, 12));
        g2d.drawString("Mankind was born on Earth. It was never meant to die here.", 40, 508);

        drawGlassBadge(g2d, 30, 540, 80, 26, "SCI-FI", new Color(255, 170, 40));
        drawGlassBadge(g2d, 120, 540, 80, 26, "ENGLISH", new Color(168, 85, 247));
        drawRatingBadge(g2d, 22, 22, "8.7");

        applyLiquidGlassOverlay(g2d);
        g2d.dispose();
        saveImage(img, file);
    }

    // 3. THE DARK KNIGHT
    private static void generateTheDarkKnight(File file) {
        BufferedImage img = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = createHighQualityGraphics(img);

        // Dark Gotham night sky
        GradientPaint bg = new GradientPaint(
                0, 0, new Color(8, 12, 22),
                0, HEIGHT, new Color(20, 26, 42)
        );
        g2d.setPaint(bg);
        g2d.fillRect(0, 0, WIDTH, HEIGHT);

        // Gotham skyscrapers skyline silhouettes
        g2d.setColor(new Color(12, 16, 28));
        int[] bHeights = { 180, 240, 290, 210, 310, 260, 230, 280, 190 };
        for (int i = 0; i < bHeights.length; i++) {
            int bx = i * 45;
            int bw = 48;
            int bh = bHeights[i];
            g2d.fillRect(bx, HEIGHT - bh - 80, bw, bh + 80);
            // skyscraper windows
            g2d.setColor(new Color(255, 220, 100, 50));
            for (int r = 0; r < bh / 25; r++) {
                g2d.fillRect(bx + 10, HEIGHT - bh - 60 + r * 22, 6, 8);
                g2d.fillRect(bx + 26, HEIGHT - bh - 60 + r * 22, 6, 8);
            }
            g2d.setColor(new Color(12, 16, 28));
        }

        // Bat Signal beam & emblem in clouds
        int cx = WIDTH / 2;
        int cy = 210;
        RadialGradientPaint batBeam = new RadialGradientPaint(
                cx, cy, 130,
                new float[]{ 0.0f, 0.6f, 1.0f },
                new Color[]{
                        new Color(100, 180, 255, 140),
                        new Color(30, 80, 160, 60),
                        new Color(0, 0, 0, 0)
                }
        );
        g2d.setPaint(batBeam);
        g2d.fillOval(cx - 120, cy - 90, 240, 180);

        // Bat Silhouette
        g2d.setColor(new Color(6, 9, 16));
        Path2D bat = new Path2D.Double();
        bat.moveTo(cx, cy - 25);
        bat.curveTo(cx - 20, cy - 50, cx - 80, cy - 35, cx - 85, cy);
        bat.curveTo(cx - 65, cy + 30, cx - 35, cy + 15, cx - 18, cy + 32);
        bat.curveTo(cx - 10, cy + 22, cx, cy + 38, cx, cy + 38);
        bat.curveTo(cx, cy + 38, cx + 10, cy + 22, cx + 18, cy + 32);
        bat.curveTo(cx + 35, cy + 15, cx + 65, cy + 30, cx + 85, cy);
        bat.curveTo(cx + 80, cy - 35, cx + 20, cy - 50, cx, cy - 25);
        bat.closePath();
        g2d.fill(bat);

        // Glow outline on Bat
        g2d.setColor(new Color(130, 200, 255, 180));
        g2d.setStroke(new BasicStroke(2f));
        g2d.draw(bat);

        // Joker catchphrase & Title
        g2d.setColor(new Color(255, 69, 58));
        g2d.setFont(new Font("Segoe UI", Font.BOLD, 12));
        g2d.drawString("WHY SO SERIOUS?", 140, 425);

        g2d.setColor(Color.WHITE);
        g2d.setFont(new Font("Segoe UI", Font.BOLD, 30));
        g2d.drawString("THE DARK KNIGHT", 55, 470);

        g2d.setColor(new Color(160, 180, 210));
        g2d.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        g2d.drawString("CHRISTIAN BALE • HEATH LEDGER", 100, 502);

        drawGlassBadge(g2d, 30, 540, 80, 26, "ACTION", new Color(10, 132, 255));
        drawGlassBadge(g2d, 120, 540, 80, 26, "ENGLISH", new Color(168, 85, 247));
        drawRatingBadge(g2d, 22, 22, "9.0");

        applyLiquidGlassOverlay(g2d);
        g2d.dispose();
        saveImage(img, file);
    }

    // 4. OPPENHEIMER
    private static void generateOppenheimer(File file) {
        BufferedImage img = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = createHighQualityGraphics(img);

        // Dramatic fiery nuclear detonation gradient
        GradientPaint bg = new GradientPaint(
                0, 0, new Color(18, 5, 2),
                0, HEIGHT, new Color(4, 2, 6)
        );
        g2d.setPaint(bg);
        g2d.fillRect(0, 0, WIDTH, HEIGHT);

        int cx = WIDTH / 2;
        int cy = 230;

        // Radial fiery explosion cloud
        RadialGradientPaint nuke = new RadialGradientPaint(
                cx, cy, 160,
                new float[]{ 0.0f, 0.25f, 0.55f, 0.85f, 1.0f },
                new Color[]{
                        new Color(255, 255, 230, 240),
                        new Color(255, 175, 40, 200),
                        new Color(230, 60, 15, 160),
                        new Color(90, 15, 10, 90),
                        new Color(0, 0, 0, 0)
                }
        );
        g2d.setPaint(nuke);
        g2d.fillOval(cx - 150, cy - 140, 300, 280);

        // Fiery particles / sparks
        Random rand = new Random(101);
        for (int i = 0; i < 120; i++) {
            int px = cx + rand.nextInt(260) - 130;
            int py = cy + rand.nextInt(260) - 130;
            g2d.setColor(new Color(255, rand.nextInt(150) + 105, 30, rand.nextInt(200) + 55));
            g2d.fillOval(px, py, rand.nextInt(4) + 1, rand.nextInt(4) + 1);
        }

        // J. Robert Oppenheimer hat silhouette
        g2d.setColor(new Color(10, 4, 4));
        g2d.fillOval(cx - 45, cy + 30, 90, 18);
        g2d.fillRect(cx - 28, cy - 5, 56, 38);
        g2d.fillOval(cx - 28, cy - 18, 56, 26);

        // Credits & Title
        g2d.setColor(new Color(255, 200, 140));
        g2d.setFont(new Font("Segoe UI", Font.BOLD, 11));
        g2d.drawString("CILLIAN MURPHY IS", 145, 430);

        g2d.setColor(Color.WHITE);
        g2d.setFont(new Font("Segoe UI", Font.BOLD, 34));
        g2d.drawString("OPPENHEIMER", 75, 475);

        g2d.setColor(new Color(255, 160, 80));
        g2d.setFont(new Font("Segoe UI", Font.ITALIC, 13));
        g2d.drawString("The world forever changes.", 118, 508);

        drawGlassBadge(g2d, 30, 540, 90, 26, "BIOGRAPHY", new Color(255, 149, 0));
        drawGlassBadge(g2d, 130, 540, 80, 26, "ENGLISH", new Color(168, 85, 247));
        drawRatingBadge(g2d, 22, 22, "8.9");

        applyLiquidGlassOverlay(g2d);
        g2d.dispose();
        saveImage(img, file);
    }

    // 5. RRR
    private static void generateRRR(File file) {
        BufferedImage img = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = createHighQualityGraphics(img);

        // Dual Fire (Left) & Water (Right) Clash
        GradientPaint fireSide = new GradientPaint(
                0, 0, new Color(45, 10, 5),
                WIDTH / 2, HEIGHT, new Color(15, 5, 5)
        );
        g2d.setPaint(fireSide);
        g2d.fillRect(0, 0, WIDTH / 2, HEIGHT);

        GradientPaint waterSide = new GradientPaint(
                WIDTH / 2, 0, new Color(5, 20, 45),
                WIDTH, HEIGHT, new Color(2, 8, 20)
        );
        g2d.setPaint(waterSide);
        g2d.fillRect(WIDTH / 2, 0, WIDTH / 2, HEIGHT);

        int cx = WIDTH / 2;
        int cy = 250;

        // Fire blaze left
        RadialGradientPaint fireGlow = new RadialGradientPaint(
                cx - 70, cy, 120,
                new float[]{ 0.0f, 0.6f, 1.0f },
                new Color[]{ new Color(255, 100, 20, 220), new Color(200, 40, 10, 120), new Color(0, 0, 0, 0) }
        );
        g2d.setPaint(fireGlow);
        g2d.fillOval(cx - 160, cy - 100, 180, 200);

        // Water splash right
        RadialGradientPaint waterGlow = new RadialGradientPaint(
                cx + 70, cy, 120,
                new float[]{ 0.0f, 0.6f, 1.0f },
                new Color[]{ new Color(30, 160, 255, 220), new Color(10, 90, 200, 120), new Color(0, 0, 0, 0) }
        );
        g2d.setPaint(waterGlow);
        g2d.fillOval(cx - 20, cy - 100, 180, 200);

        // Epic giant "RRR" metallic lettering
        g2d.setColor(new Color(255, 215, 0));
        g2d.setFont(new Font("Impact", Font.BOLD, 85));
        g2d.drawString("RRR", 125, 300);

        g2d.setColor(Color.WHITE);
        g2d.setStroke(new BasicStroke(2.5f));
        g2d.setFont(new Font("Impact", Font.BOLD, 85));
        // text outline
        FontMetrics fm = g2d.getFontMetrics();
        g2d.drawString("RRR", 125, 300);

        // Subtitle
        g2d.setColor(new Color(240, 200, 120));
        g2d.setFont(new Font("Segoe UI", Font.BOLD, 12));
        g2d.drawString("S.S. RAJAMOULI'S MASTERPIECE", 100, 430);

        g2d.setColor(Color.WHITE);
        g2d.setFont(new Font("Segoe UI", Font.BOLD, 18));
        g2d.drawString("RISE • ROAR • REVOLT", 105, 475);

        g2d.setColor(new Color(180, 220, 255));
        g2d.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        g2d.drawString("N.T. RAMA RAO JR. • RAM CHARAN", 95, 505);

        drawGlassBadge(g2d, 30, 540, 80, 26, "ACTION", new Color(255, 100, 20));
        drawGlassBadge(g2d, 120, 540, 80, 26, "TELUGU", new Color(52, 199, 89));
        drawRatingBadge(g2d, 22, 22, "7.8");

        applyLiquidGlassOverlay(g2d);
        g2d.dispose();
        saveImage(img, file);
    }

    // 6. KALKI 2898 AD
    private static void generateKalki(File file) {
        BufferedImage img = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = createHighQualityGraphics(img);

        // Cybernetic dystopian sci-fi background
        GradientPaint bg = new GradientPaint(
                0, 0, new Color(15, 12, 5),
                0, HEIGHT, new Color(5, 15, 25)
        );
        g2d.setPaint(bg);
        g2d.fillRect(0, 0, WIDTH, HEIGHT);

        int cx = WIDTH / 2;
        int cy = 230;

        // Golden divine halo & sci-fi ring
        g2d.setColor(new Color(255, 195, 0, 180));
        g2d.setStroke(new BasicStroke(4f));
        g2d.drawOval(cx - 90, cy - 90, 180, 180);

        // Divine glowing energy trident / futuristic emblem
        g2d.setColor(new Color(255, 225, 120));
        g2d.fillRect(cx - 4, cy - 100, 8, 180);
        g2d.drawArc(cx - 50, cy - 80, 100, 80, 180, 180);

        // Sci-Fi grid lines
        g2d.setColor(new Color(0, 220, 255, 40));
        g2d.setStroke(new BasicStroke(1f));
        for (int y = 300; y < HEIGHT; y += 20) {
            g2d.drawLine(0, y, WIDTH, y);
        }

        // Title
        g2d.setColor(new Color(255, 215, 0));
        g2d.setFont(new Font("Segoe UI", Font.BOLD, 12));
        g2d.drawString("PRABHAS • AMITABH BACHCHAN • KAMAL HAASAN", 45, 430);

        g2d.setColor(Color.WHITE);
        g2d.setFont(new Font("Segoe UI", Font.BOLD, 30));
        g2d.drawString("KALKI 2898 AD", 90, 475);

        g2d.setColor(new Color(0, 210, 255));
        g2d.setFont(new Font("Segoe UI", Font.ITALIC, 13));
        g2d.drawString("The Future Has Begun.", 135, 508);

        drawGlassBadge(g2d, 30, 540, 80, 26, "SCI-FI", new Color(0, 210, 255));
        drawGlassBadge(g2d, 120, 540, 80, 26, "TELUGU", new Color(52, 199, 89));
        drawRatingBadge(g2d, 22, 22, "7.6");

        applyLiquidGlassOverlay(g2d);
        g2d.dispose();
        saveImage(img, file);
    }

    // 7. JAWAN
    private static void generateJawan(File file) {
        BufferedImage img = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = createHighQualityGraphics(img);

        // Crimson and shadow background
        GradientPaint bg = new GradientPaint(
                0, 0, new Color(35, 5, 8),
                0, HEIGHT, new Color(8, 4, 6)
        );
        g2d.setPaint(bg);
        g2d.fillRect(0, 0, WIDTH, HEIGHT);

        int cx = WIDTH / 2;
        int cy = 240;

        // Bandaged vigilante silhouette with red glowing gaze
        g2d.setColor(new Color(255, 45, 85, 140));
        RadialGradientPaint crimsonGlow = new RadialGradientPaint(
                cx, cy, 140,
                new float[]{ 0.0f, 0.7f, 1.0f },
                new Color[]{ new Color(255, 45, 85, 180), new Color(180, 20, 40, 90), new Color(0, 0, 0, 0) }
        );
        g2d.setPaint(crimsonGlow);
        g2d.fillOval(cx - 130, cy - 130, 260, 260);

        // Wrapped bandages texture
        g2d.setColor(new Color(230, 210, 190, 180));
        g2d.setStroke(new BasicStroke(5f));
        for (int i = 0; i < 7; i++) {
            g2d.drawLine(cx - 45 + i * 4, cy - 50 + i * 15, cx + 55 - i * 4, cy - 40 + i * 15);
        }

        // Title
        g2d.setColor(new Color(255, 75, 75));
        g2d.setFont(new Font("Segoe UI", Font.BOLD, 12));
        g2d.drawString("SHAH RUKH KHAN • NAYANTHARA", 95, 430);

        g2d.setColor(Color.WHITE);
        g2d.setFont(new Font("Impact", Font.BOLD, 46));
        g2d.drawString("JAWAN", 135, 475);

        g2d.setColor(new Color(255, 180, 180));
        g2d.setFont(new Font("Segoe UI", Font.ITALIC, 13));
        g2d.drawString("Ready Chief.", 160, 508);

        drawGlassBadge(g2d, 30, 540, 80, 26, "ACTION", new Color(255, 69, 58));
        drawGlassBadge(g2d, 120, 540, 80, 26, "HINDI", new Color(255, 149, 0));
        drawRatingBadge(g2d, 22, 22, "7.0");

        applyLiquidGlassOverlay(g2d);
        g2d.dispose();
        saveImage(img, file);
    }

    // 8. STREE 2
    private static void generateStree2(File file) {
        BufferedImage img = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = createHighQualityGraphics(img);

        // Spooky haunted gothic night (purple & toxic green fog)
        GradientPaint bg = new GradientPaint(
                0, 0, new Color(18, 8, 30),
                0, HEIGHT, new Color(5, 15, 12)
        );
        g2d.setPaint(bg);
        g2d.fillRect(0, 0, WIDTH, HEIGHT);

        int cx = WIDTH / 2;
        int cy = 230;

        // Ghostly full moon
        g2d.setColor(new Color(245, 255, 230, 220));
        g2d.fillOval(cx - 75, cy - 110, 150, 150);

        // Haunted mansion / trees silhouette
        g2d.setColor(new Color(12, 6, 20));
        int[] mx = { cx - 40, cx - 40, cx, cx + 40, cx + 40 };
        int[] my = { cy + 70, cy - 10, cy - 40, cy - 10, cy + 70 };
        g2d.fillPolygon(mx, my, 5);

        // Red sari / veil spectral glow
        g2d.setColor(new Color(255, 45, 85, 190));
        g2d.setStroke(new BasicStroke(4f));
        g2d.drawArc(cx - 50, cy - 30, 100, 120, 140, 160);

        // Title
        g2d.setColor(new Color(190, 255, 120));
        g2d.setFont(new Font("Segoe UI", Font.BOLD, 12));
        g2d.drawString("SHRADDHA KAPOOR • RAJKUMMAR RAO", 75, 430);

        g2d.setColor(new Color(255, 45, 85));
        g2d.setFont(new Font("Segoe UI", Font.BOLD, 36));
        g2d.drawString("STREE 2", 130, 475);

        g2d.setColor(new Color(200, 255, 180));
        g2d.setFont(new Font("Segoe UI", Font.ITALIC, 13));
        g2d.drawString("O Stree Raksha Karna.", 130, 508);

        drawGlassBadge(g2d, 30, 540, 80, 26, "COMEDY", new Color(168, 85, 247));
        drawGlassBadge(g2d, 120, 540, 80, 26, "HINDI", new Color(255, 149, 0));
        drawRatingBadge(g2d, 22, 22, "7.3");

        applyLiquidGlassOverlay(g2d);
        g2d.dispose();
        saveImage(img, file);
    }

    // 9. DEFAULT POSTER (For new movies created by Admin)
    private static void generateDefault(File file) {
        BufferedImage img = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = createHighQualityGraphics(img);

        // Apple Space Gray / Deep Navy Liquid Glass background
        GradientPaint bg = new GradientPaint(
                0, 0, new Color(16, 20, 32),
                WIDTH, HEIGHT, new Color(24, 32, 50)
        );
        g2d.setPaint(bg);
        g2d.fillRect(0, 0, WIDTH, HEIGHT);

        int cx = WIDTH / 2;
        int cy = 240;

        // Projector light cone
        GradientPaint light = new GradientPaint(
                cx, cy - 80, new Color(10, 132, 255, 120),
                cx, cy + 120, new Color(10, 132, 255, 0)
        );
        g2d.setPaint(light);
        int[] lx = { cx - 15, cx - 120, cx + 120, cx + 15 };
        int[] ly = { cy - 70, cy + 110, cy + 110, cy - 70 };
        g2d.fillPolygon(lx, ly, 4);

        // Movie clapperboard icon
        g2d.setColor(new Color(30, 40, 60));
        g2d.fillRoundRect(cx - 55, cy - 25, 110, 85, 16, 16);
        g2d.setColor(Color.WHITE);
        g2d.setStroke(new BasicStroke(2.5f));
        g2d.drawRoundRect(cx - 55, cy - 25, 110, 85, 16, 16);

        // Clapper stripes
        for (int i = 0; i < 4; i++) {
            g2d.setColor(new Color(10, 132, 255));
            g2d.fillRect(cx - 48 + i * 26, cy - 20, 12, 18);
        }

        // Title
        g2d.setColor(new Color(10, 132, 255));
        g2d.setFont(new Font("Segoe UI", Font.BOLD, 12));
        g2d.drawString("CINEFLOW EXCLUSIVE CINEMA", 110, 430);

        g2d.setColor(Color.WHITE);
        g2d.setFont(new Font("Segoe UI", Font.BOLD, 28));
        g2d.drawString("NOW SHOWING", 95, 475);

        g2d.setColor(new Color(180, 195, 215));
        g2d.setFont(new Font("Segoe UI", Font.ITALIC, 13));
        g2d.drawString("Experience Cinema in Pure Ultra HD.", 85, 508);

        drawGlassBadge(g2d, 30, 540, 80, 26, "FEATURE", new Color(10, 132, 255));
        drawGlassBadge(g2d, 120, 540, 80, 26, "DOLBY", new Color(255, 214, 10));
        drawRatingBadge(g2d, 22, 22, "8.0");

        applyLiquidGlassOverlay(g2d);
        g2d.dispose();
        saveImage(img, file);
    }

    private static void saveImage(BufferedImage img, File file) {
        try {
            ImageIO.write(img, "png", file);
            System.out.println("Generated: " + file.getName());
        } catch (IOException e) {
            System.err.println("Failed to write image " + file.getName() + ": " + e.getMessage());
        }
    }
}
