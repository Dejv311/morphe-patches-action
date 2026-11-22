package app.action.extension;

import android.graphics.Bitmap;

@SuppressWarnings("unused")
public final class QrCodeExtension {
    private QrCodeExtension() {}

    public static Bitmap addWhiteQuietZone(Bitmap bitmap) {
        if (bitmap == null || bitmap.getWidth() != bitmap.getHeight()) return bitmap;

        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        int[] pixels = new int[width * height];
        bitmap.getPixels(pixels, 0, width, 0, 0, width, height);
        int padding = QrCodeQuietZone.requiredPadding(pixels, width, height);
        if (padding == 0) return bitmap;

        Bitmap padded = Bitmap.createBitmap(
                QrCodeQuietZone.addPadding(pixels, width, height, padding),
                width + 2 * padding, height + 2 * padding, Bitmap.Config.ARGB_8888);
        padded.setDensity(bitmap.getDensity());
        return padded;
    }
}
