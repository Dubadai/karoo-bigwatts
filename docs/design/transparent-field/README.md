# Transparent field (bug fix)

**Bug:** the field painted a solid white (black at night) rectangle over its whole box, so its
square corners and edges stuck out past the Karoo's rounded tile border (same bug as ClearWind
v0.2.1).

**Fix:** no background of our own, as the Karoo does for its own fields. The bitmap and the root
view stay transparent, so the Karoo draws the tile (rounded corners, border, white or black). The
Karoo SDK has no way for an extension to colour its tile (`UpdateGraphicConfig` only has
`showHeader` and `formatDataTypeId`).

Screenshots (Karoo 3, light mode, Karoo look, gauge bar; corners enlarged 6×):

- `01-before-ride-page-light.png`, `02-…`, `03-before-corner-…`: white edges outside the border
- `04-after-ride-page-light.png`, `05-…`, `06-after-corner-…`: the field follows the tile
