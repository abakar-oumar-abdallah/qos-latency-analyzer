package com.qos.latency.analyzer.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import com.qos.latency.analyzer.model.LatencyModel;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Vue personnalisée pour afficher un graphique de latence réseau interactif.
 *
 * Cette classe hérite de View Android et dessine un graphique en temps réel
 * des données de paquets réseau. Elle supporte le zoom, le déplacement et
 * un curseur interactif pour examiner les points individuels.
 *
 * Fonctionnalités :
 * - Affichage des paquets avec différentes couleurs selon leur statut
 * - Zoom avec gestes multi-touch
 * - Curseur déplaçable pour examiner les détails d'un point
 * - Légende dynamique des types de paquets présents
 *
 * @author Équipe QoS Gaming
 * @version 3.0
 */
public class ChartView extends View {

    /** Indique si le mode timestamp est activé */
    private boolean useTimestampMode = false;

    /** Liste des points à afficher sur le graphique */
    private final List<PacketPoint> points = new ArrayList<>();

    // Objets Paint pour dessiner les différents éléments
    private Paint paintRecu, paintPerdu, paintDuplique, paintInverse;
    private Paint paintAxe, paintTexte, paintHighlight, paintCross;

    /** Limites maximales du graphique */
    private float maxX = 10f, maxY = 100f;

    // Variables pour la gestion du curseur interactif
    private boolean isDraggingCursor = false;
    private float cursorX = 0f, cursorY = 0f;
    private boolean showCursor = false;

    // Variables pour la gestion du zoom et du déplacement
    private float scale = 1f;
    private float offsetX = 0f, offsetY = 0f;
    private float lastTouchX, lastTouchY;
    private boolean isZooming = false;
    private float lastSpacing = 0f;
    private long lastTapTime = 0;

    /**
     * Constructeur utilisé quand la vue est créée depuis du code Java.
     */
    public ChartView(Context context) {
        super(context);
        init();
    }

    /**
     * Constructeur utilisé quand la vue est définie dans un fichier XML.
     */
    public ChartView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    /**
     * Initialise tous les objets Paint nécessaires au dessin.
     * Définit les couleurs et styles pour chaque type d'élément graphique.
     */
    private void init() {
        // Couleurs pour les différents types de paquets
        paintRecu = createPaint("#4CAF50");        // Vert pour paquets reçus
        paintPerdu = createPaint("#F44336");       // Rouge pour paquets perdus
        paintDuplique = createPaint("#FF9800");    // Orange pour paquets dupliqués
        paintInverse = createPaint("#9C27B0");     // Violet pour paquets inversés

        // Style pour les axes du graphique
        paintAxe = new Paint(Paint.ANTI_ALIAS_FLAG);
        paintAxe.setColor(Color.GRAY);
        paintAxe.setStrokeWidth(2f);

        // Style pour le texte (labels, légende, etc.)
        paintTexte = new Paint(Paint.ANTI_ALIAS_FLAG);
        paintTexte.setColor(Color.BLACK);
        paintTexte.setTextSize(24f);

        // Style pour surligner le point le plus proche du curseur
        paintHighlight = new Paint(Paint.ANTI_ALIAS_FLAG);
        paintHighlight.setColor(Color.BLUE);
        paintHighlight.setStyle(Paint.Style.STROKE);
        paintHighlight.setStrokeWidth(4f);

        // Style pour dessiner le curseur (croix rouge)
        paintCross = new Paint(Paint.ANTI_ALIAS_FLAG);
        paintCross.setColor(Color.parseColor("#FF4444"));
        paintCross.setStrokeWidth(3f);
    }

    /**
     * Crée un objet Paint avec la couleur spécifiée.
     *
     * @param color Code couleur hexadécimal (ex: "#FF0000")
     * @return Objet Paint configuré avec cette couleur
     */
    private Paint createPaint(String color) {
        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setColor(Color.parseColor(color));
        return paint;
    }

    /**
     * Active ou désactive le mode timestamp pour l'affichage des paquets.
     */
    public void setTimestampMode(boolean enabled) {
        this.useTimestampMode = enabled;
    }

    /**
     * Ajoute un nouveau point représentant un paquet réseau sur le graphique.
     * Détermine les coordonnées selon le statut du paquet :
     * - Paquets perdus : temps TX, RTT = 0
     * - Autres paquets : temps RX, RTT réel
     *
     * @param packet Données du paquet à ajouter
     */
    public void addPacketPoint(LatencyModel.RequestData packet) {
        if (!useTimestampMode || packet == null) return;

        float x, y;

        if (packet.getStatus() == LatencyModel.PacketStatus.LOST) {
            x = (float) packet.getTxTimeRelative();
            y = 0.0f;
        } else {
            x = (float) packet.getRxTimeRelative();
            y = (float) packet.getRtt();
        }

        points.add(new PacketPoint(x, y, packet.getStatus()));

        // Met à jour les limites du graphique si nécessaire
        if (x > maxX) maxX = x * 1.1f;
        if (y > maxY) maxY = y * 1.1f;

        invalidate(); // Redessine la vue
    }

    /**
     * Efface tous les points du graphique et remet les valeurs par défaut.
     */
    public void clearAllSeries() {
        points.clear();
        maxX = 10f;
        maxY = 100f;
        showCursor = false;
        resetZoom();
        invalidate();
    }

    /**
     * Remet le zoom et la position du graphique aux valeurs par défaut.
     */
    public void resetZoom() {
        scale = 1f;
        offsetX = offsetY = 0f;
        invalidate();
    }

    /**
     * Méthode principale de dessin appelée par Android.
     * Dessine tous les éléments du graphique dans l'ordre :
     * axes, points, légende, curseur.
     */
    @Override
    protected void onDraw(Canvas canvas) {
        float margin = 80f;
        float width = getWidth() - margin * 2;
        float height = getHeight() - margin * 2;

        drawAxes(canvas, margin, width, height);
        drawPoints(canvas, margin, width, height);
        drawLegend(canvas);

        if (showCursor) {
            drawCursor(canvas, margin, width, height);
        }
    }

    /**
     * Dessine les axes X et Y avec leurs graduations et labels.
     */
    private void drawAxes(Canvas canvas, float margin, float width, float height) {
        // Dessine les lignes d'axes
        canvas.drawLine(margin, getHeight() - margin, getWidth() - margin, getHeight() - margin, paintAxe);
        canvas.drawLine(margin, margin, margin, getHeight() - margin, paintAxe);

        // Labels des axes
        canvas.drawText("Temps (s)", getWidth() / 2f - 40f, getHeight() - 20f, paintTexte);
        canvas.save();
        canvas.rotate(-90, 40f, getHeight() / 2f);
        canvas.drawText("RTT (ms)", 40f, getHeight() / 2f, paintTexte);
        canvas.restore();

        // Dessine les graduations et leurs valeurs
        for (int i = 0; i <= 5; i++) {
            float valueX = (offsetX + i * width / (5f * scale)) * maxX / width;
            float valueY = (offsetY + i * height / (5f * scale)) * maxY / height;

            if (valueX <= maxX) {
                float x = margin + i * width / 5f;
                canvas.drawLine(x, getHeight() - margin, x, getHeight() - margin + 10f, paintAxe);
                canvas.drawText(String.format(Locale.getDefault(), "%.1f", valueX), x - 18f, getHeight() - margin + 35f, paintTexte);
            }

            if (valueY <= maxY) {
                float y = getHeight() - margin - i * height / 5f;
                canvas.drawLine(margin - 10f, y, margin, y, paintAxe);
                canvas.drawText(String.format(Locale.getDefault(), "%.1f", valueY), margin - 75f, y + 8f, paintTexte);
            }
        }
    }

    /**
     * Dessine tous les points représentant les paquets réseau.
     * Utilise une zone de clipping pour éviter de dessiner en dehors du graphique.
     */
    private void drawPoints(Canvas canvas, float margin, float width, float height) {
        canvas.save();
        canvas.clipRect(margin, margin, getWidth() - margin, getHeight() - margin);

        for (PacketPoint point : points) {
            float x = margin + ((point.x / maxX) * width - offsetX) * scale;
            float y = getHeight() - margin - ((point.y / maxY) * height - offsetY) * scale;

            // Ne dessine que les points visibles (avec une petite marge)
            if (x >= margin - 20 && x <= getWidth() - margin + 20 &&
                    y >= margin - 20 && y <= getHeight() - margin + 20) {
                canvas.drawCircle(x, y, 8f, getPaint(point.status));
            }
        }
        canvas.restore();
    }

    /**
     * Dessine le curseur interactif (croix rouge) et les informations du point le plus proche.
     */
    private void drawCursor(Canvas canvas, float margin, float width, float height) {
        float x = margin + ((cursorX / maxX) * width - offsetX) * scale;
        float y = getHeight() - margin - ((cursorY / maxY) * height - offsetY) * scale;

        // Vérifie que le curseur est dans la zone visible
        if (x < margin || x > getWidth() - margin || y < margin || y > getHeight() - margin) return;

        // Dessine la croix
        canvas.drawLine(x, margin, x, getHeight() - margin, paintCross);
        canvas.drawLine(margin, y, getWidth() - margin, y, paintCross);

        // Dessine le point central blanc avec contour rouge
        Paint white = new Paint(Paint.ANTI_ALIAS_FLAG);
        white.setColor(Color.WHITE);
        canvas.drawCircle(x, y, 12f, white);

        Paint border = new Paint(Paint.ANTI_ALIAS_FLAG);
        border.setColor(Color.parseColor("#FF4444"));
        border.setStyle(Paint.Style.STROKE);
        border.setStrokeWidth(3f);
        canvas.drawCircle(x, y, 12f, border);

        // Trouve et surligne le point le plus proche
        PacketPoint nearest = findNearest();
        if (nearest != null) {
            float nearX = margin + ((nearest.x / maxX) * width - offsetX) * scale;
            float nearY = getHeight() - margin - ((nearest.y / maxY) * height - offsetY) * scale;

            // Ligne de liaison entre curseur et point ciblé
            Paint linePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
            linePaint.setColor(Color.parseColor("#88FF4444"));
            linePaint.setStrokeWidth(2f);
            canvas.drawLine(x, y, nearX, nearY, linePaint);

            // Cercle de surbrillance
            canvas.drawCircle(nearX, nearY, 15f, paintHighlight);

            // Affiche les informations du point
            String statusText = getStatusDisplayName(nearest.status);
            String info = String.format(Locale.getDefault(), "Point: %.2fs, %.2fms (%s) | Curseur: %.2fs, %.2fms",
                    nearest.x, nearest.y, statusText, cursorX, cursorY);
            drawInfo(canvas, x, y, info);
        }
    }

    /**
     * Dessine une bulle d'information à côté du curseur.
     */
    private void drawInfo(Canvas canvas, float x, float y, String text) {
        float textWidth = paintTexte.measureText(text);
        float textX = x + 20f;
        if (textX + textWidth > getWidth() - 10f) textX = x - 20f - textWidth;

        // Fond blanc semi-transparent
        Paint bg = new Paint(Paint.ANTI_ALIAS_FLAG);
        bg.setColor(Color.WHITE);
        bg.setAlpha(220);
        canvas.drawRect(textX - 6f, y - 35f, textX + textWidth + 6f, y - 5f, bg);
        canvas.drawText(text, textX, y - 15f, paintTexte);
    }

    /**
     * Trouve le point le plus proche du curseur.
     *
     * @return Le point le plus proche, ou null si aucun point assez proche
     */
    private PacketPoint findNearest() {
        PacketPoint nearest = null;
        float minDist = Float.MAX_VALUE;

        for (PacketPoint point : points) {
            // Distance normalisée pour tenir compte de l'échelle du graphique
            float dx = (point.x - cursorX) / maxX;
            float dy = (point.y - cursorY) / maxY;
            float dist = dx * dx + dy * dy;

            if (dist < minDist && dist < 0.01f) {
                minDist = dist;
                nearest = point;
            }
        }
        return nearest;
    }

    /**
     * Dessine la légende dynamique dans le coin supérieur droit.
     * Affiche seulement les types de paquets présents dans les données.
     */
    private void drawLegend(Canvas canvas) {
        float x = getWidth() - 150f, y = 50f;
        boolean[] has = new boolean[4];

        // Détermine quels types de paquets sont présents
        for (PacketPoint p : points) {
            if (p.status != null) {
                switch (p.status) {
                    case RECEIVED: has[0] = true; break;
                    case REVERSED: has[1] = true; break;
                    case DUPLICATED: has[2] = true; break;
                    case LOST: has[3] = true; break;
                }
            }
        }

        // Dessine les entrées de légende pour les types présents
        String[] labels = {"Reçus", "Inversés", "Dupliqués", "Perdus"};
        Paint[] paints = {paintRecu, paintInverse, paintDuplique, paintPerdu};

        int count = 0;
        for (int i = 0; i < 4; i++) {
            if (has[i]) {
                canvas.drawCircle(x, y + count * 25f, 6f, paints[i]);
                canvas.drawText(labels[i], x + 15f, y + count * 25f + 5f, paintTexte);
                count++;
            }
        }
    }

    /**
     * Retourne l'objet Paint approprié selon le statut du paquet.
     */
    private Paint getPaint(LatencyModel.PacketStatus status) {
        if (status == null) return paintRecu;
        switch (status) {
            case RECEIVED: return paintRecu;
            case LOST: return paintPerdu;
            case DUPLICATED: return paintDuplique;
            case REVERSED: return paintInverse;
            default: return paintRecu;
        }
    }

    /**
     * Retourne le nom d'affichage du statut du paquet.
     */
    private String getStatusDisplayName(LatencyModel.PacketStatus status) {
        if (status == null) return "Reçu";
        switch (status) {
            case RECEIVED: return "Reçu";
            case LOST: return "Perdu";
            case DUPLICATED: return "Dupliqué";
            case REVERSED: return "Inversé";
            default: return "Reçu";
        }
    }

    /**
     * Convertit les coordonnées d'écran en coordonnées du graphique.
     * Prend en compte le zoom et le déplacement.
     */
    private float[] screenToGraph(float screenX, float screenY) {
        float margin = 80f;
        float width = getWidth() - margin * 2;
        float height = getHeight() - margin * 2;

        float graphX = Math.max(0, Math.min(((screenX - margin) / scale + offsetX) * maxX / width, maxX));
        float graphY = Math.max(0, Math.min(((getHeight() - margin - screenY) / scale + offsetY) * maxY / height, maxY));

        return new float[]{graphX, graphY};
    }

    /**
     * Vérifie si l'utilisateur touche le curseur.
     */
    private boolean isTouchingCursor(float touchX, float touchY) {
        if (!showCursor) return false;
        float margin = 80f;
        float width = getWidth() - margin * 2;
        float height = getHeight() - margin * 2;

        float cursorScreenX = margin + ((cursorX / maxX) * width - offsetX) * scale;
        float cursorScreenY = getHeight() - margin - ((cursorY / maxY) * height - offsetY) * scale;

        float dx = touchX - cursorScreenX;
        float dy = touchY - cursorScreenY;
        return Math.sqrt(dx * dx + dy * dy) <= 30f;
    }

    /**
     * Calcule la distance entre deux doigts pour la gestion du zoom.
     */
    private float getSpacing(MotionEvent event) {
        if (event.getPointerCount() < 2) return 0f;
        float x = event.getX(0) - event.getX(1);
        float y = event.getY(0) - event.getY(1);
        return (float) Math.sqrt(x * x + y * y);
    }

    /**
     * Gère tous les événements tactiles (touch, zoom, déplacement du curseur).
     * Implémente la logique complexe de gestion multi-touch.
     */
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float margin = 80f;
        boolean inGraph = event.getX() >= margin && event.getX() <= getWidth() - margin &&
                event.getY() >= margin && event.getY() <= getHeight() - margin;

        switch (event.getAction() & MotionEvent.ACTION_MASK) {
            case MotionEvent.ACTION_DOWN:
                if (inGraph) {
                    if (isTouchingCursor(event.getX(), event.getY())) {
                        isDraggingCursor = true;
                    } else {
                        float[] coords = screenToGraph(event.getX(), event.getY());
                        cursorX = coords[0];
                        cursorY = coords[1];
                        showCursor = true;
                        invalidate();
                    }
                }
                lastTouchX = event.getX();
                lastTouchY = event.getY();
                break;

            case MotionEvent.ACTION_POINTER_DOWN:
                isZooming = true;
                isDraggingCursor = false;
                lastSpacing = getSpacing(event);
                break;

            case MotionEvent.ACTION_MOVE:
                if (event.getPointerCount() == 2 && isZooming) {
                    // Gestion du zoom avec deux doigts
                    float currentSpacing = getSpacing(event);
                    if (currentSpacing > 10f && lastSpacing > 10f) {
                        scale *= Math.max(0.8f, Math.min(currentSpacing / lastSpacing, 1.25f));
                        scale = Math.max(0.5f, Math.min(scale, 5f));
                        lastSpacing = currentSpacing;
                        invalidate();
                    }
                } else if (event.getPointerCount() == 1) {
                    if (isDraggingCursor && inGraph) {
                        // Déplacement du curseur
                        float[] coords = screenToGraph(event.getX(), event.getY());
                        cursorX = coords[0];
                        cursorY = coords[1];
                        invalidate();
                    } else if (scale > 1f) {
                        // Déplacement du graphique quand il est zoomé
                        offsetX -= (event.getX() - lastTouchX) / scale;
                        offsetY += (event.getY() - lastTouchY) / scale;
                        lastTouchX = event.getX();
                        lastTouchY = event.getY();
                        invalidate();
                    }
                }
                break;

            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_POINTER_UP:
                // Double-tap pour remettre le zoom à zéro
                if (event.getPointerCount() == 1 && scale > 1f) {
                    long currentTime = System.currentTimeMillis();
                    if (currentTime - lastTapTime < 300) {
                        resetZoom();
                    }
                    lastTapTime = currentTime;
                }
                isDraggingCursor = false;
                isZooming = false;
                break;
        }
        return true;
    }

    /**
     * Classe interne représentant un point sur le graphique.
     * Stocke les coordonnées et le statut d'un paquet.
     */
    private static class PacketPoint {
        final float x, y;
        final LatencyModel.PacketStatus status;

        PacketPoint(float x, float y, LatencyModel.PacketStatus status) {
            this.x = x;
            this.y = y;
            this.status = status;
        }
    }
}