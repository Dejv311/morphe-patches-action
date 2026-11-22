# Action App Morphe Patches

Custom Morphe patches for the official Action store Android app (`com.action.consumerapp`).

Fork of [janowakps/morphe-patches-action](https://github.com/janowakps/morphe-patches-action), with a customer card QR scanning fix for dark mode. Credit for the original patches belongs to the upstream authors.

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
> **[v1.0.3](https://github.com/Dejv311/morphe-patches-action/releases/tag/v1.0.3)**&nbsp;&nbsp;•&nbsp;&nbsp;`main`&nbsp;&nbsp;•&nbsp;&nbsp;3 patches total
<details open>
<summary>📦 Action&nbsp;&nbsp;•&nbsp;&nbsp;3 patches</summary>
<br>

**🎯 Supported versions:**

| 1.133.0 |
| :---: |

| 💊&nbsp;Patch | 📜&nbsp;Description | ⚙️&nbsp;Options |
|----------|----------------|-----------|
| [Dark Mode](#dark-mode) | Forces dark mode throughout the Action app while keeping the customer card QR code black on white with a scannable white margin. |  |
| [Dark Splash Screen](#dark-splash-screen) | Forces dark window and splash screen backgrounds in styles.xml to eliminate the white flash on app startup. |  |
| [Weekly Deals Sorting](#weekly-deals-sorting) | Adds bidirectional sorting options with interactive chips for Weekly Deals, keeps native filtering/sorting untouched on 'New' and category pages, increases initial deals page limit to 120, and fixes badge label clipping. |  |

</details>

<!-- PATCHES_END -->

## Features & Included Patches

### Dark Mode
Forces an eye-friendly, deeply integrated dark theme throughout the entire Action app:
- **Core Jetpack Compose Palette Override**: Systematically maps and overrides the central `ai.b` palette (background `#121212`, surfaces `#1E1E1E`, card tiles `#282828`).
- **Product Card Contrast**: Uses a gentle dark gray `#282828` for product card backgrounds so transparent PNG product photos (including dark items) contrast clearly with the card surface.
- **Customer Card QR Code Protection**: Keeps the customer card barcode and QR code solid black on white. QR codes also receive a white quiet zone at least four modules wide on every side so the dark screen does not touch the code. Existing margins are reused; the code's pixels and contents are preserved, and linear barcodes keep their original layout. This protection also works when Weekly Deals Sorting is disabled.

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
