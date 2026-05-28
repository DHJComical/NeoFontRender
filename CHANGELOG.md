# Changelog

All notable changes to Neo Font Render will be documented in this file.

## [0.2.0] - Unreleased

### Added
- Added a backend abstraction layer with `TextRenderBackend` and `TextRenderResult` so advanced text renderers no longer couple directly to the FontRenderer mixin surface.
- Added `SkijaRuntimeSupport` to detect Java runtime and embedded native compatibility before enabling the Skija renderer.
- Added multi-platform embedded Skija natives for Windows x64, Linux x64, Linux arm64, macOS x64, and macOS arm64 packaging.
- Added architecture notes for the current text renderer split and Arc3D / ModernUI migration research under `docs/`.

### Changed
- Reorganized the font codebase by actual renderer responsibility into `core.font.backend`, `core.font.awt`, `core.font.skia`, `core.font.support`, and `core.font.layout`.
- Updated `FontManager` to select renderers through the backend abstraction and to fall back to the AWT atlas renderer when Skija is unsupported or initialization fails.
- Updated FontRenderer integration, commands, and GUI test surfaces to consume backend abstractions instead of directly referencing Skija implementation types.
- Kept the layout cache package as a future-facing seam instead of forcing it into the current Skija hot path.

## [0.1.0]

### Added
- Initial project scaffold based on CleanroomMC TemplateDevEnv.
- CoreMod ASM transformer skeleton.
- Mixin configuration placeholder.
- Access Transformer definitions for FontRenderer and GlStateManager.
