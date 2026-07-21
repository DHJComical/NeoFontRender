# NFR Modern Tooltips

Optional Minecraft 1.12.2 client addon for Neo Font Render. It replaces Forge
tooltip layout/background rendering while continuing to publish Forge tooltip
color and post-render events.

## Runtime design

- Requires Neo Font Render 0.3.4 or newer.
- Uses Arc3D Core 2026.2.0 distributed by the required NFR main mod.
- Uses Cleanroom's host LWJGL 3.4.1 and never bundles LWJGL or native files.
- Yields to LegendaryTooltips by default when that mod is present.
- Uses NFR visual glyph bounds for wrapping and screen-edge placement.

## Build

```powershell
.\gradlew.bat :addons:modern-tooltips:test :addons:modern-tooltips:remapJar
```

The distributable jar is written to `addons/modern-tooltips/build/libs/` without
the `-dev` classifier.

## Configuration

The addon creates the independent file `config/neofontrender-modern-tooltips.toml`.
It controls rounded corners and curve quality, continuous shadow quality/color/offset,
edge antialiasing, title and body text, divider, padding, line spacing, wrapping,
and four-corner fill/border colors. The same options are embedded as an NFR settings tab.
