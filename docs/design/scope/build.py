"""Inline the Karoo frame, reference screens and karoo-device.js into mockup.html.

Mockup pages are opened as static snapshots that can't load files beside them, so everything is
embedded. Run: python3 build.py  (from this folder)
"""
import base64, json, pathlib
here = pathlib.Path(__file__).parent
assets = here.parents[3] / 'design-assets'          # Karoo/design-assets
def data_url(p): return 'data:image/png;base64,' + base64.b64encode(p.read_bytes()).decode()
refs = {p.stem: data_url(p) for p in sorted((here / 'reference').glob('*.png'))}
html = (here / 'mockup.src.html').read_text()
html = html.replace('/*__KAROO_DEVICE_JS__*/', (assets / 'karoo-device.js').read_text())
html = html.replace('__FRAME__', data_url(assets / 'karoo-3-frame.png'))
html = html.replace('"__REFS__"', json.dumps(refs))
(here / 'mockup.html').write_text(html)
print('wrote', here / 'mockup.html')
