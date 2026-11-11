# No power meter: the Karoo's empty state (mockup)

Shared rule (KarooType `emptyPaint` / `emptyBaseline`, workspace 8b8d944): empty states like
"No Sensor" in the label font at 25 px caps, centred, ink bottom 39 px above the tile bottom
(measured on 238 × 167 tiles). BigWatts today shows a big "--" with an empty gauge.

| Picture | What |
| --- | --- |
| `01-no-power-half.png` | Half page, Karoo look (bar, HR) and zone-colour number: A now "--", B at the Karoo's spot, C in the middle |
| `02-no-power-sizes.png` | Full page and quarter, A / B / C |

Notes: at quarter size B and C are almost the same (the measured tile size). On half and full pages
B sits at the very bottom. B and C as mocked leave out the heart-rate line; it can stay.
"Searching..." (power meter paired but not connected yet) vs "No Sensor" needs the stream state.

**Picked C (2026-11-09):** "No Sensor" where the watts go, the heart-rate / average line kept under it (no zone line without power). Gallery: `docs/design/gallery/*-nopower-*`.
| `03-device-no-sensor-light.png` | On our test Karoo (0.1.12): our No Sensor next to the Karoo's own; no strap, so "♥ –" |
| `04-no-heart-rate.png` | Mockup: the line hidden when its value is missing too (no strap), kept with a strap |
