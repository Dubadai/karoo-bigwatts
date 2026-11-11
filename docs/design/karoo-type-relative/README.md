# Karoo look: the Karoo's own value font (picked 2026-11-08)

Typography pass (item 6 of the 2026-11-08 ride list). The Karoo draws field values in **Relative**
(Hammerhead Relative Mono 12, letterSpacing −0.04), not Plex (design-assets/karoo-type/README.md).
`KarooType.kt` (copied from karoo-starter) now draws the Karoo look's watts; the header already
followed the same measured rules. Relative is loaded from the Karoo (`Typeface.create("relative")`),
never bundled. The BigWatts look keeps Plex SemiBold.

Drawn by the field gallery (the app's own code, Karoo fonts):

| Picture | What |
| --- | --- |
| `karoo-bar-hr-half.png` | Our setup (Karoo look, bar, heart rate), now vs Relative, light and dark |
| `karoo-half.png` | Karoo look with the zone-colour number, now vs Relative |
| `karoo-sizes.png` | Relative at full page, quarter, small and one row |
| `01-device-karoo-bar-hr-dark.png` | On our test Karoo (dark, demo data on USB): Relative loads from the Karoo |
| `02-all-relative.png` | Watts only vs everything under the watts in Relative too (feedback: "always use relative") |
| `03-tighter-heavier.png` | Under-line words with a normal gap ("ZONE 4") and a tad heavier (thin outline in the text colour) |
| `04-header-weight.png`, `04-header-weight-closeup.png` | Under-line at the Karoo header's weight (outline 0.028 × size, font spacing), normal word gap |
| `05-device-header-weight-dark.png` | On our test Karoo (dark, demo on USB): heart rate in Relative at the header weight |
| `06-heart-options.png` | Heart icon options A–D next to the native HR tile, light and dark |
| `07-device-karoo-red-heart-dark.png` | Picked D: the Karoo heart shape in Karoo red (#D60404), on our test Karoo |
| `08-shared-bolt-heart.png` | Shared KarooType bolt and red heart (Karoo's exact shape, box 0.86 × text size) vs ours |
