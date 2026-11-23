# Action App Morphe Patches

Custom Morphe patches for the official Action store Android app (`com.action.consumerapp`).

Fork of [janowakps/morphe-patches-action](https://github.com/janowakps/morphe-patches-action), with an OLED palette and a compact customer card QR code with smooth rounded corners. Credit for the original patches belongs to the upstream authors.

## About

This repository provides a custom patch bundle for Morphe that enhances the official Action Android application with a fully integrated Dark Theme, Dark Splash Screen, and advanced interactive sorting for Weekly Deals.

### How to use these patches in Morphe Manager

1. Open **Morphe Manager** on your Android device.
2. Go to **Settings** > **Sources**.
3. Add custom source: `https://github.com/Dejv311/morphe-patches-action`
4. Or tap this link on your device: [Add to Morphe](https://morphe.software/add-source?github=Dejv311/morphe-patches-action)
5. Select **Action**, choose your desired patches, and patch the app!

## Patches list

<!-- PATCHES_START EXPANDED -->
> **[v1.1.2](https://github.com/Dejv311/morphe-patches-action/releases/tag/v1.1.2)**&nbsp;&nbsp;•&nbsp;&nbsp;`main`&nbsp;&nbsp;•&nbsp;&nbsp;3 patches total
<details open>
<summary>📦 Action&nbsp;&nbsp;•&nbsp;&nbsp;3 patches</summary>
<br>

**🎯 Supported versions:**

| 1.133.0 |
| :---: |

| 💊&nbsp;Patch | 📜&nbsp;Description | ⚙️&nbsp;Options |
|----------|----------------|-----------|
| [Dark Mode](#dark-mode) | Uses true-black OLED backgrounds, neutral gray surfaces, saturated accents, and a compact QR card with smooth corners. | • QR white border width |
| [Dark Splash Screen](#dark-splash-screen) | Forces true-black window and splash screen backgrounds in styles.xml to eliminate the white flash on app startup. |  |
| [Weekly Deals Sorting](#weekly-deals-sorting) | Adds bidirectional sorting options with interactive chips for Weekly Deals, keeps native filtering/sorting untouched on 'New' and category pages, increases initial deals page limit to 120, and fixes badge label clipping. |  |

</details>

<!-- PATCHES_END -->

## Features & Included Patches

### Dark Mode
Overrides the central Action palette with an OLED-oriented dark theme:
- **True Black Background**: Overrides the central `ai.b` palette with a `#000000` app background, neutral gray surfaces (`#0D0D0D`, `#1A1A1A`), and `#151515` card tiles. The startup window and splash screen also use black when Dark Splash Screen is selected.
- **Product Card Contrast**: Keeps product photos on a slightly raised gray card surface so dark items remain visible against the black app background.
- **Saturated Accents**: Defines 78 coordinated surface, text, border and accent tokens. Blue, cyan, green, orange, pink and purple highlights sit on neutral gray containers instead of tinted backgrounds. Previously omitted middle shades are covered, and popup menus use the same card surface. Only fields present in the matched palette are patched. Regression tests check text contrast on the intended surfaces and colored containers.
- **Compact Rounded QR Card**: The customer card QR code has a one-module white border by default, with softly rounded outer corners. One module is one QR square, not one screen pixel. Existing excess white space is normalized. Low-resolution bitmaps are enlarged by an integer factor with nearest-neighbor copying before rounding, preserving every QR module while giving the corners enough pixels for smooth edges. Extra upscaling is limited to a 1024-pixel card; larger originals are not reduced. Bitmap density is adjusted with the scale to preserve intrinsic logical dimensions where a density is specified. Choose **2** or **4** modules in the Dark Mode patch options if needed; four is the standard quiet-zone width. Decoding tests composite the rounded card onto a dark surface. Linear barcodes keep their original layout, and QR styling also works when Weekly Deals Sorting is disabled.

### Dark Splash Screen
Eliminates the jarring white flash on app startup by overriding window background and splash screen styles in `styles.xml`.

### Weekly Deals Sorting
Brings interactive sorting options to the Weekly Deals screen:
- **Interactive Sorting Chips**:
  - `Polecane` / `Empfohlen` / `Doporučené` / `Aanbevolen` (Default / Relevance): dynamically retrieved from native Action resources.
  - `Cena ↑` / `Cena ↓`: bidirectional price sorting (lowest or highest price first).
  - `% ↓`: discount percentage descending.
- **Automatic Multi-Language Support**: Chip labels are dynamically pulled from Action app resources at runtime without hardcoded dictionaries, supporting Polish, German, Czech, Dutch, French, Spanish, Italian, and all future European locales automatically.
- **Data-Driven Discount Chip**: Intelligently hides the `% ↓` chip when browsing markets or grids where percentage discounts are not displayed (such as in the Czech Republic or Germany).
- **All-in-One Pagination**: Increases initial deals query limit from 24 to 120 items to load the complete weekly deals catalog in a single request.
- **Grid Context Isolation**: Strictly confines sorting and pagination modifications to Weekly Deals, keeping native filtering and pagination intact on 'New' (Nowości / Neu / Nové), Search, and Category pages.
- **Badge Label Layout Fix**: Corrects vertical clipping of multi-line promotional badge labels.

## Development

- Releases are automated via GitHub Actions using semantic release.
