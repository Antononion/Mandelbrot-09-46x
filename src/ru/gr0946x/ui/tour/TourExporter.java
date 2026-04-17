package ru.gr0946x.ui.tour;

import ru.gr0946x.Converter;
import ru.gr0946x.ui.fractals.ColorFunction;
import ru.gr0946x.ui.fractals.FractalState;
import ru.gr0946x.ui.fractals.QuadraticFractal;
import ru.gr0946x.ui.painting.FractalPainter;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class TourExporter {

    private final QuadraticFractal exportFractal;
    private final Converter exportConverter;
    private final FractalPainter exportPainter;
    private final List<TourKeyframe> keyframes;
    private final int fps;
    private final int exportWidth;
    private final int exportHeight;

    public TourExporter(QuadraticFractal sourceFractal, ColorFunction colorFunction,
                        List<TourKeyframe> keyframes, int fps, int exportWidth, int exportHeight) {
        this.keyframes = keyframes;
        this.fps = fps;
        this.exportWidth = exportWidth;
        this.exportHeight = exportHeight;

        // Isolated fractal — never touches the live window's fractal
        exportFractal = sourceFractal.isJulia()
                ? new QuadraticFractal(sourceFractal.getConstant())
                : new QuadraticFractal();
        exportFractal.setMaxIterations(sourceFractal.getMaxIterations());

        FractalState first = keyframes.getFirst().getState();
        exportConverter = new Converter(first.xMin(), first.xMax(), first.yMin(), first.yMax());
        exportConverter.setWidth(exportWidth);
        exportConverter.setHeight(exportHeight);

        exportPainter = new FractalPainter(exportFractal, exportConverter, colorFunction);
    }

    /**
     * Renders and writes the video. Runs on a background thread.
     *
     * @param outputFile      destination .avi file
     * @param progressCallback called with 0..100 progress values
     * @param cancelCheck     return true to stop early
     */
    public void export(File outputFile,
                       Consumer<Integer> progressCallback,
                       Supplier<Boolean> cancelCheck) throws IOException {
        int totalFrames = computeTotalFrames();

        try (MjpegAviWriter writer = new MjpegAviWriter(outputFile, exportWidth, exportHeight, fps)) {
            int framesWritten = 0;

            for (int seg = 0; seg < keyframes.size() - 1; seg++) {
                TourKeyframe kfA = keyframes.get(seg);
                TourKeyframe kfB = keyframes.get(seg + 1);
                int segFrames = Math.max(1, (int) Math.round(kfA.getDurationSeconds() * fps));

                for (int f = 0; f < segFrames; f++) {
                    if (Boolean.TRUE.equals(cancelCheck.get())) return;

                    double t = (double) f / segFrames;
                    FractalState state = KeyframeInterpolator.interpolate(
                            kfA.getState(), kfB.getState(), t, exportWidth, exportHeight);

                    applyState(state);

                    writer.writeFrame(renderFrame());
                    framesWritten++;
                    progressCallback.accept(framesWritten * 100 / totalFrames);
                }
            }

            // Last keyframe — hold for 1 second
            if (!Boolean.TRUE.equals(cancelCheck.get())) {
                applyState(keyframes.getLast().getState());
                BufferedImage lastFrame = renderFrame();
                for (int f = 0; f < fps; f++) {
                    if (Boolean.TRUE.equals(cancelCheck.get())) return;
                    writer.writeFrame(lastFrame);
                    framesWritten++;
                    progressCallback.accept(Math.min(100, framesWritten * 100 / totalFrames));
                }
            }
        }
    }

    private void applyState(FractalState s) {
        exportConverter.setXShape(s.xMin(), s.xMax());
        exportConverter.setYShape(s.yMin(), s.yMax());
        exportFractal.setMaxIterations(s.maxIterations());
    }

    private BufferedImage renderFrame() {
        BufferedImage img = new BufferedImage(exportWidth, exportHeight, BufferedImage.TYPE_INT_RGB);
        Graphics g = img.getGraphics();
        exportPainter.paint(g);
        g.dispose();
        return img;
    }

    private int computeTotalFrames() {
        int total = fps; // last keyframe hold
        for (int i = 0; i < keyframes.size() - 1; i++)
            total += Math.max(1, (int) Math.round(keyframes.get(i).getDurationSeconds() * fps));
        return total;
    }
}
