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
    private static final int BACKGROUND = 0xFF101418;

    @Test
    public void compactAndStandardCardsDecodeAgainstDarkBackground() throws Exception {
        for (String content : new String[] {"L1234567890123", "QR regression " + "1234567890".repeat(12)}) {
            BitMatrix modules = qr(content);
            for (int scale : new int[] {2, 3, 10}) {
                int width = modules.getWidth() * scale;
                int[] original = render(modules, scale, 0);
                for (int borderModules : new int[] {1, 2, 4}) {
                    QrCodeQuietZone.Card card = QrCodeQuietZone.render(original, width, width, borderModules);
                    assertNotNull(card);
                    assertEquals(borderModules * scale, card.margin);
                    assertEquals(width + 2 * card.margin, card.size);
                    for (int y = 0; y < width; y++) {
                        for (int x = 0; x < width; x++) {
                            assertEquals(original[y * width + x],
                                    card.pixels[(y + card.margin) * card.size + x + card.margin]);
                        }
                    }
                    // Composite the real alpha edges onto a dark app surface before
                    // decoding; ignoring alpha would hide broken rounded corners.
                    int surround = 8 * scale;
                    int scanWidth = card.size + 2 * surround;
                    int[] scanPixels = new int[scanWidth * scanWidth];
                    Arrays.fill(scanPixels, BACKGROUND);
                    for (int y = 0; y < card.size; y++) {
                        for (int x = 0; x < card.size; x++) {
                            scanPixels[(y + surround) * scanWidth + x + surround]
                                    = composite(card.pixels[y * card.size + x]);
                        }
                    }
                    BinaryBitmap scan = new BinaryBitmap(new HybridBinarizer(
                            new RGBLuminanceSource(scanWidth, scanWidth, scanPixels)));
                    try {
                        assertEquals(content, new QRCodeReader().decode(scan).getText());
                    } catch (com.google.zxing.ReaderException exception) {
                        throw new AssertionError("Cannot decode " + modules.getWidth() + " modules, scale "
                                + scale + ", border " + borderModules, exception);
                    }
                    QrCodeQuietZone.Card repeated = QrCodeQuietZone.render(card.pixels, card.size, card.size, borderModules);
                    assertNotNull(repeated);
                    assertArrayEquals(card.pixels, repeated.pixels);
                }
            }
        }
    }

    @Test
    public void normalizesExistingWhiteSpaceToExactlyOneModule() throws Exception {
        BitMatrix modules = qr("EXISTING-MARGIN");
        for (int scale : new int[] {1, 5}) {
            for (int existing : new int[] {0, 1, 2, 4, 6}) {
                int width = (modules.getWidth() + 2 * existing) * scale;
                QrCodeQuietZone.Card card = QrCodeQuietZone.render(render(modules, scale, existing), width, width, 1);
                assertNotNull(card);
                assertEquals(scale, card.margin);
                assertEquals((modules.getWidth() + 2) * scale, card.size);
            }
        }
    }

    @Test
    public void roundsOnlyTheWhiteOuterCorners() throws Exception {
        BitMatrix modules = qr("ROUNDED-CORNERS");
        int scale = 10;
        int width = modules.getWidth() * scale;
        QrCodeQuietZone.Card card = QrCodeQuietZone.render(render(modules, scale, 0), width, width, 1);
        assertNotNull(card);
        assertEquals(0, card.pixels[0] >>> 24);
        assertEquals(0, card.pixels[card.size - 1] >>> 24);
        assertEquals(WHITE, card.pixels[card.size / 2]);
        assertEquals(BLACK, card.pixels[card.margin * card.size + card.margin]);
        boolean antialiased = false;
        for (int pixel : card.pixels) {
            int alpha = pixel >>> 24;
            if (alpha > 0 && alpha < 255) antialiased = true;
        }
        assertTrue("Outer corners should have smooth alpha edges", antialiased);
    }

    @Test
    public void leavesBarcodesAndNonQrImagesAlone() {
        int[] barcode = new int[120 * 40];
        for (int i = 0; i < barcode.length; i++) barcode[i] = i % 4 < 2 ? BLACK : WHITE;
        assertNull(QrCodeQuietZone.render(barcode, 120, 40, 1));
        int[] square = new int[63 * 63];
        Arrays.fill(square, WHITE);
        assertNull(QrCodeQuietZone.render(square, 63, 63, 1));
        Arrays.fill(square, BLACK);
        assertNull(QrCodeQuietZone.render(square, 63, 63, 1));
    }

    private static int composite(int argb) {
        int alpha = argb >>> 24;
        int result = 0xFF000000;
        for (int shift : new int[] {16, 8, 0}) {
            int foreground = (argb >>> shift) & 255;
            int background = (BACKGROUND >>> shift) & 255;
            result |= ((foreground * alpha + background * (255 - alpha) + 127) / 255) << shift;
        }
        return result;
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
