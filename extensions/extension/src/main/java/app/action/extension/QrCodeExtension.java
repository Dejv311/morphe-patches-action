package app.action.extension;

import android.graphics.Bitmap;

@SuppressWarnings("unused")
public final class QrCodeExtension {
    private static volatile int whiteBorderModules = 1;

    private QrCodeExtension() {}

    public static void setWhiteBorderModules(int modules) {
        whiteBorderModules = Math.max(1, Math.min(4, modules));
    }

    public static Bitmap addWhiteQuietZone(Bitmap bitmap) {
        if (bitmap == null || bitmap.getWidth() != bitmap.getHeight()) return bitmap;
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        int[] pixels = new int[width * height];
        bitmap.getPixels(pixels, 0, width, 0, 0, width, height);
        QrCodeQuietZone.Card card = QrCodeQuietZone.render(pixels, width, height, whiteBorderModules);
        if (card == null) return bitmap;
        Bitmap result = Bitmap.createBitmap(card.pixels, card.size, card.size, Bitmap.Config.ARGB_8888);
        result.setDensity(bitmap.getDensity());
        return result;
    }
}
