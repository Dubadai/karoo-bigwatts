# Field gallery

BigWatts at every Karoo 3 field size, light and dark, drawn by `WattsRenderer` on the Karoo 3
emulator with the showcase values (280 W = zone 4 at FTP 280, 132 bpm, average 282 W):
`arc`, `bar`, `number` (just the watts in zone colour), `karoo` (Karoo look), `hr` (zone-colour
number with heart rate under it). Half page is BigWatts' main size (the README picture).

- `fields/`: the field alone · `screens/`: on a Karoo page (boundaries on) · `artboard/` (not in
  git): in the Karoo 3 frame on Sand / Night

```bash
bash ../design-assets/tools/field-gallery/gallery.sh karoo-bigwatts
```

What's drawn: `app/src/debug/kotlin/.../GalleryFields.kt`.
