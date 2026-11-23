package app.action.extension;

import java.util.Arrays;

/** QR geometry and rounded white-card rendering without Android dependencies. */
final class QrCodeQuietZone {
    private static final int BLACK = 0xFF000000;
    private static final int WHITE = 0xFFFFFFFF;
    static final int MIN_MODULE_PIXELS = 16;
    static final int MAX_UPSCALED_CARD_SIZE = 1024;

    private QrCodeQuietZone() {}

    static final class Card {
        final int[] pixels;
        final int size;
        final int margin;
        final int moduleSize;
        final int pixelScale;

        Card(int[] pixels, int size, int margin, int moduleSize, int pixelScale) {
            this.pixels = pixels;
            this.size = size;
            this.margin = margin;
            this.moduleSize = moduleSize;
            this.pixelScale = pixelScale;
        }
    }

    static Card render(int[] pixels, int width, int height, int borderModules) {
        // This shared renderer also handles linear barcodes and other images.
        if (pixels == null || width != height || width < 21
                || (long) width * height != pixels.length) return null;
        if (borderModules < 1 || borderModules > 4) {
            throw new IllegalArgumentException("QR border must be between one and four modules");
        }

        int first = 0;
        while (first < pixels.length && pixels[first] != BLACK) first++;
        if (first == pixels.length) return null;
        int left = first % width;
        int top = first / width;
        int end = left;
        while (end < width && pixels[top * width + end] == BLACK) end++;
        int finderWidth = end - left;
        if (finderWidth < 7 || top + finderWidth > height) return null;

        // Validate the seven-by-seven QR finder before changing any image.
        for (int row = 0; row < 7; row++) {
            int y = top + (2 * row + 1) * finderWidth / 14;
            for (int column = 0; column < 7; column++) {
                int x = left + (2 * column + 1) * finderWidth / 14;
                boolean expectedBlack = row == 0 || row == 6 || column == 0 || column == 6
                        || (row >= 2 && row <= 4 && column >= 2 && column <= 4);
                if ((pixels[y * width + x] == BLACK) != expectedBlack) return null;
            }
        }

        int minX = width;
        int minY = height;
        int maxX = -1;
        int maxY = -1;
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                if (pixels[y * width + x] != BLACK) continue;
                minX = Math.min(minX, x);
                minY = Math.min(minY, y);
                maxX = Math.max(maxX, x);
                maxY = Math.max(maxY, y);
            }
        }
        if (maxX - minX != maxY - minY) return null;

        int sourceModuleSize = (finderWidth + 6) / 7;
        int sourceCodeSize = maxX - minX + 1;
        int sourceCardSize = sourceCodeSize + 2 * borderModules * sourceModuleSize;
        int desiredScale = (MIN_MODULE_PIXELS + sourceModuleSize - 1) / sourceModuleSize;
        int maximumScale = Math.max(1, MAX_UPSCALED_CARD_SIZE / sourceCardSize);
        int pixelScale = Math.min(desiredScale, maximumScale);
        int moduleSize = sourceModuleSize * pixelScale;
        int margin = borderModules * moduleSize;
        int codeSize = sourceCodeSize * pixelScale;
        int size = codeSize + 2 * margin;
        int[] result = new int[size * size];
        Arrays.fill(result, WHITE);
        // The source can be just one pixel per QR module. Enlarge by an integer
        // factor before drawing corners, using nearest-neighbour copying only.
        // Existing high-resolution codes are never downsampled.
        for (int y = 0; y < codeSize; y++) {
            int sourceRow = (minY + y / pixelScale) * width + minX;
            int destinationRow = (margin + y) * size + margin;
            if (pixelScale == 1) {
                System.arraycopy(pixels, sourceRow, result, destinationRow, codeSize);
            } else {
                for (int x = 0; x < codeSize; x++) {
                    result[destinationRow + x] = pixels[sourceRow + x / pixelScale];
                }
            }
        }

        // The radius never reaches the QR data. Only the outer white corners
        // become transparent, with a one-pixel antialiased edge.
        int radius = moduleSize;
        for (int y = 0; y < radius; y++) {
            for (int x = 0; x < radius; x++) {
                double distance = Math.hypot(radius - x - 0.5, radius - y - 0.5);
                int alpha = (int) Math.round(255 * Math.max(0, Math.min(1, radius + 0.5 - distance)));
                int color = (alpha << 24) | 0x00FFFFFF;
                result[y * size + x] = color;
                result[y * size + size - x - 1] = color;
                result[(size - y - 1) * size + x] = color;
                result[(size - y - 1) * size + size - x - 1] = color;
            }
        }
        return new Card(result, size, margin, moduleSize, pixelScale);
    }
}
