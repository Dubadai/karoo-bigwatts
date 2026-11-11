# Zone time (built on branch zone-time; counting not yet tried on a recorded ride)

Rider wish: the gauge segments grow with the time spent in each zone during the ride.

**Idea:** the segments get **thicker**, not longer (longer would make other zones shorter than today).
- Today's size is the minimum; a zone you haven't ridden in looks exactly like today.
- The zone with the most time gets 0.8 × the current zone's thickness extra (0.6 was a tad weak, 2026-11-08); the others scale
  to their time relative to it (so a long ride doesn't keep growing).
- Grows on both sides of the line. The needle reaches further so it still crosses the thickest segment.
- Eases in over the first 5 minutes so the start doesn't jump.
- Counts only while recording. Setting **Zone time: Off · All time · Pedalling** under Gauge
  (only when the number is a gauge), off by default. **All time** (0 W counted as zone 1) is the
  standard choice when on; **Pedalling** leaves 0 W out so coasting doesn't make Z1 big (2026-11-08).
  The Example card on the Settings page shows the growth.
- Not in one-row fields (no room).

Drawn by the real renderer (`WattsRenderer`, `zoneSeconds` in the gallery state):
4 min in = 60/120/60/20 s in Z1–Z4; 1 h tempo = 13/15/25/10/3/1/0.3 min in Z1–Z7 (Z1 includes 8 min
coasting), now at 280 W (Z4).

| Picture | What |
| --- | --- |
| `01-arc.png` | Arc: today, 4 min in, 1 h; light and dark |
| `02-bar.png` | Bar: today vs 1 h; light and dark |
| `03-karoo-bar-hr.png` | Our setup (Karoo look, bar, heart rate), 1 h |
| `04-smaller.png` | Full page, quarter (arc and bar), one row |
| `05-settings.png` | Settings page: Zone time, All time and Pedalling, light and dark |
| `06-device-our-test-karoo.png` | On our test Karoo (demo data, Karoo look, bar, heart rate), All time |
