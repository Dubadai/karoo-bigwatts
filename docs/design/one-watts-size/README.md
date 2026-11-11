# One size for all watts (mockup)

Ask (2026-11-09): "keep the size the same for all watts in BigWatts" — no jump at 99 → 100 W.
Today the number is fitted to the live value's width, so short values come out bigger.

Options, drawn by the real renderer at 280 W:
- **A, now:** fitted to the live value.
- **B, fit "888":** every value up to 999 W the same size; only 1000 W and up shrink. At half page
  280 W looks exactly as now (height is the limit); 2-digit values stop being bigger.
- **C, fit "8888":** every value the same size; the zone-colour number and the arc get smaller.
  The Karoo look's bar (Relative, narrower digits) is height-bound, so it doesn't change.

| Picture | What |
| --- | --- |
| `01-number-half.png`, `01-bar-half.png`, `01-arc-half.png`, `01-karoo-bar-hr-half.png` | A / B / C at half page |
| `02-number-sizes.png` | Zone-colour number at full, quarter and one row, A / B / C |
