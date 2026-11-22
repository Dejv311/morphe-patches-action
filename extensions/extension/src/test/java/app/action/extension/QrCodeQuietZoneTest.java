package app.action.extension;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.BinaryBitmap;
import com.google.zxing.EncodeHintType;
import com.google.zxing.RGBLuminanceSource;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.common.HybridBinarizer;
import com.google.zxing.qrcode.QRCodeReader;
import com.google.zxing.qrcode.QRCodeWriter;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;

import static org.junit.Assert.*;

public class QrCodeQuietZoneTest {
    private static final int BLACK = 0xFF000000;
    private static final int WHITE = 0xFFFFFFFF;

    @Test
    public void addsFourWhiteModulesAndPreservesDecodedContents() throws Exception {
        for (String content : new String[] {"CARD-TEST-12345", "QR quiet-zone regression test: " + "1234567890".repeat(20)}) {
            BitMatrix modules = qr(content);
            for (int scale : new int[] {2, 3, 10}) {
                int width = modules.getWidth() * scale;
                int[] original = render(modules, scale, 0);
                int padding = QrCodeQuietZone.requiredPadding(original, width, width);
                assertEquals(4 * scale, padding);
                int[] padded = QrCodeQuietZone.addPadding(original, width, width, padding);
                int paddedWidth = width + 2 * padding;

                for (int y = 0; y < paddedWidth; y++) {
                    for (int x = 0; x < paddedWidth; x++) {
                        boolean border = x < padding || y < padding
                                || x >= paddedWidth - padding || y >= paddedWidth - padding;
                        assertEquals(border ? WHITE : original[(y - padding) * width + x - padding],
                                padded[y * paddedWidth + x]);
                    }
                }

                BinaryBitmap scan = new BinaryBitmap(new HybridBinarizer(
                        new RGBLuminanceSource(paddedWidth, paddedWidth, padded)));
                try {
                    assertEquals(content, new QRCodeReader().decode(scan).getText());
                } catch (com.google.zxing.ReaderException exception) {
                    throw new AssertionError("Cannot decode " + modules.getWidth()
                            + " modules at " + scale + " pixels per module", exception);
                }
                assertEquals("Adding a quiet zone twice must not keep growing the bitmap", 0,
                        QrCodeQuietZone.requiredPadding(padded, paddedWidth, paddedWidth));
            }
        }
    }

    @Test
    public void onlyAddsMissingMargin() throws Exception {
        BitMatrix modules = qr("PARTIAL-MARGIN");
        for (int scale : new int[] {1, 5}) {
            for (int margin : new int[] {0, 1, 2, 3, 4, 6}) {
                int width = (modules.getWidth() + 2 * margin) * scale;
                assertEquals(Math.max(0, (4 - margin) * scale),
                        QrCodeQuietZone.requiredPadding(render(modules, scale, margin), width, width));
            }
        }
    }

    @Test
    public void leavesLinearBarcodesAndNonQrImagesAlone() {
        int[] barcode = new int[120 * 40];
        for (int i = 0; i < barcode.length; i++) barcode[i] = i % 4 < 2 ? BLACK : WHITE;
        assertEquals(0, QrCodeQuietZone.requiredPadding(barcode, 120, 40));

        int[] square = new int[63 * 63];
        Arrays.fill(square, WHITE);
        assertEquals(0, QrCodeQuietZone.requiredPadding(square, 63, 63));
        Arrays.fill(square, BLACK);
        assertEquals(0, QrCodeQuietZone.requiredPadding(square, 63, 63));
    }

    private static BitMatrix qr(String content) throws Exception {
        return new QRCodeWriter().encode(content, BarcodeFormat.QR_CODE, 0, 0,
                Collections.singletonMap(EncodeHintType.MARGIN, 0));
    }

    private static int[] render(BitMatrix modules, int scale, int margin) {
        int width = (modules.getWidth() + 2 * margin) * scale;
        int[] pixels = new int[width * width];
        Arrays.fill(pixels, WHITE);
        for (int y = 0; y < modules.getHeight() * scale; y++) {
            for (int x = 0; x < modules.getWidth() * scale; x++) {
                if (modules.get(x / scale, y / scale)) {
                    pixels[(y + margin * scale) * width + x + margin * scale] = BLACK;
                }
            }
        }
        return pixels;
    }
}
