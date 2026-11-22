package app.action.extension;

import java.util.Arrays;

/** Pixel-only QR quiet-zone calculations, independent of Android and screen density. */
final class QrCodeQuietZone {
    private static final int BLACK = 0xFF000000;
    private static final int WHITE = 0xFFFFFFFF;

    private QrCodeQuietZone() {}

    static int requiredPadding(int[] pixels, int width, int height) {
        // The renderer also handles linear barcodes; their layout must stay unchanged.
        if (width != height || width < 21) return 0;

        int first = 0;
        while (first < pixels.length && pixels[first] != BLACK) first++;
        if (first == pixels.length) return 0;

        int left = first % width;
        int top = first / width;
        int end = left;
        while (end < width && pixels[top * width + end] == BLACK) end++;
        int finderWidth = end - left;
        if (finderWidth < 7 || top + finderWidth > height) return 0;

        // A QR finder is seven modules wide. Check its black/white pattern before
        // treating an arbitrary square bitmap as a QR code.
        for (int row = 0; row < 7; row++) {
            int y = top + (2 * row + 1) * finderWidth / 14;
            for (int column = 0; column < 7; column++) {
                int x = left + (2 * column + 1) * finderWidth / 14;
                boolean expectedBlack = row == 0 || row == 6 || column == 0 || column == 6
                        || (row >= 2 && row <= 4 && column >= 2 && column <= 4);
                if ((pixels[y * width + x] == BLACK) != expectedBlack) return 0;
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

        int existingMargin = Math.min(Math.min(minX, minY),
                Math.min(width - maxX - 1, height - maxY - 1));
        // QR codes require at least four white modules on every side. Round up
        // for bitmaps whose finder width is not a multiple of seven pixels.
        int moduleSize = (finderWidth + 6) / 7;
        return Math.max(0, 4 * moduleSize - existingMargin);
    }

    static int[] addPadding(int[] pixels, int width, int height, int padding) {
        int paddedWidth = width + 2 * padding;
        int[] result = new int[paddedWidth * (height + 2 * padding)];
        Arrays.fill(result, WHITE);
        for (int row = 0; row < height; row++) {
            System.arraycopy(pixels, row * width, result,
                    (row + padding) * paddedWidth + padding, width);
        }
        return result;
    }
}
