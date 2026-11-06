package com.cineflow.util;

import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

/**
 * Utility for resolving and displaying high-resolution movie poster images
 * with Apple Liquid Glass rounded aesthetics and caching.
 */
public class PosterHelper {

    private static final Map<String, Image> IMAGE_CACHE = new HashMap<>();

    /**
     * Resolves the poster image for a movie title, falling back to default poster if not found.
     */
    public static Image getPoster(String movieTitle) {
        String slug = toSlug(movieTitle);
        if (IMAGE_CACHE.containsKey(slug)) {
            return IMAGE_CACHE.get(slug);
        }

        Image image = loadImage("/images/posters/" + slug + ".png");
        if (image == null) {
            image = loadImage("/images/posters/default_poster.png");
        }

        if (image != null) {
            IMAGE_CACHE.put(slug, image);
        }
        return image;
    }

    /**
     * Creates an Apple Liquid Glass styled ImageView for the movie poster.
     *
     * @param movieTitle   The movie's title to match poster asset
     * @param width        Target display width
     * @param height       Target display height
     * @param cornerRadius Corner radius for Apple rounded glass aesthetic
     * @return Configured and clipped ImageView
     */
    public static ImageView createPosterImageView(String movieTitle, double width, double height, double cornerRadius) {
        Image img = getPoster(movieTitle);
        ImageView view = new ImageView();
        if (img != null) {
            view.setImage(img);
        }
        view.setFitWidth(width);
        view.setFitHeight(height);
        view.setPreserveRatio(false);
        view.setSmooth(true);

        // Clip with rounded rectangle for Apple liquid glass look
        Rectangle clip = new Rectangle(width, height);
        clip.setArcWidth(cornerRadius * 2);
        clip.setArcHeight(cornerRadius * 2);
        view.setClip(clip);

        // Specular ambient drop shadow
        DropShadow shadow = new DropShadow();
        shadow.setColor(Color.rgb(0, 0, 0, 0.45));
        shadow.setRadius(12);
        shadow.setOffsetY(6);
        view.setEffect(shadow);

        return view;
    }

    private static Image loadImage(String resourcePath) {
        try (InputStream is = PosterHelper.class.getResourceAsStream(resourcePath)) {
            if (is != null) {
                return new Image(is);
            }
        } catch (Exception e) {
            System.err.println("Could not load poster: " + resourcePath + " (" + e.getMessage() + ")");
        }
        return null;
    }

    private static String toSlug(String title) {
        if (title == null) return "default_poster";
        String lower = title.toLowerCase().trim();
        if (lower.contains("inception")) return "inception";
        if (lower.contains("interstellar")) return "interstellar";
        if (lower.contains("dark knight")) return "the_dark_knight";
        if (lower.contains("oppenheimer")) return "oppenheimer";
        if (lower.contains("rrr")) return "rrr";
        if (lower.contains("kalki")) return "kalki_2898_ad";
        if (lower.contains("jawan")) return "jawan";
        if (lower.contains("stree")) return "stree_2";
        return "default_poster";
    }
}
