# Route KML segment analysis

## Source

Technical fixture: `caminho-do-centenario(1).kml`, identified as the historical `ACF_2020.kml` source. It is **not approved for production geometry**.

## Measured structure

- LineString segments: **371**
- Coordinate points: **7,924**
- Summed LineString geometry: **216.099 km**
- Published official route distance: **211.87 km**
- Difference: **4.229 km (~2.0%)**
- Segment length minimum: **0.013 km**
- Segment length median: **0.290 km**
- Segment length maximum: **5.448 km**

## Endpoint connectivity

Nearest-endpoint analysis shows that the source is highly connected locally, but not safe to reconstruct with a blind nearest-neighbour chain:

- 311/371 segment endpoints have another segment endpoint within 1 m.
- 351/371 are within 5 m.
- 365/371 are within 10 m.
- 369/371 are within 25 m.
- 370/371 are within 250 m.
- All 371 have another endpoint within 500 m.

The remaining large nearest-endpoint distances include approximately **132.7 m** and **350.4 m**. These are evidence that some segments cannot be joined solely by a proximity rule without risking invented route geometry.

## Interpretation

The KML is suitable as a technical/reference fixture and demonstrates that a route can be represented by many LineStrings. However, the source does not provide sufficient evidence for a production-safe ordered path simply by concatenating segments.

In particular:

1. Do not greedily join segments by nearest endpoint.
2. Do not bridge large gaps because they appear geographically plausible.
3. Do not replace the official published distance with the summed LineString distance.
4. Preserve the distinction between **source geometry length** and **official route distance**.
5. Production geometry must come from a validated current official GPX/KML source and must be checked for origin, destination, continuity and compatibility with the official route/stage model.

## Next technical step

When the current official geometry is available, compare its topology against this historical fixture only as a diagnostic aid. Build the production `RouteGeometry` from the validated official source rather than trying to repair the historical source into production geometry.
