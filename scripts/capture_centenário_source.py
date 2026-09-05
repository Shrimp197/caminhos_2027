#!/usr/bin/env python3
"""Capture and structurally validate an official Centenario GPX/KML source.

This tool never rewrites source bytes and never creates or repairs route geometry.
It records byte identity and, for GPX input, calculates basic structural metrics
that can be reviewed before promotion to production.
"""
from __future__ import annotations

import argparse
import hashlib
import json
import math
import xml.etree.ElementTree as ET
from datetime import datetime, timezone
from pathlib import Path

OFFICIAL = {
    "Caminho_do_centenario.gpx": {
        "format": "gpx",
        "url": "https://caminhosdefatima.com/wp-content/uploads/2024/10/Caminho_do_centenario.gpx",
    },
    "caminho-do-centenario.kml": {
        "format": "kml",
        "url": "https://caminhosdefatima.com/wp-content/uploads/2024/09/caminho-do-centenario.kml",
    },
}
GPX_NS = "http://www.topografix.com/GPX/1/1"
EARTH_RADIUS_KM = 6371.0088


def sha256(path: Path) -> str:
    digest = hashlib.sha256()
    with path.open("rb") as handle:
        for chunk in iter(lambda: handle.read(1024 * 1024), b""):
            digest.update(chunk)
    return digest.hexdigest()


def haversine_km(a: tuple[float, float], b: tuple[float, float]) -> float:
    lat1, lon1 = math.radians(a[0]), math.radians(a[1])
    lat2, lon2 = math.radians(b[0]), math.radians(b[1])
    dlat = lat2 - lat1
    dlon = lon2 - lon1
    h = math.sin(dlat / 2) ** 2 + math.cos(lat1) * math.cos(lat2) * math.sin(dlon / 2) ** 2
    return 2 * EARTH_RADIUS_KM * math.asin(math.sqrt(h))


def gpx_metrics(data: bytes) -> dict[str, object]:
    root = ET.fromstring(data)
    if root.tag != f"{{{GPX_NS}}}gpx":
        raise SystemExit("Source is not a GPX 1.1 document")

    track_segments = root.findall(f".//{{{GPX_NS}}}trkseg")
    points = []
    for point in root.findall(f".//{{{GPX_NS}}}trkpt"):
        try:
            lat = float(point.attrib["lat"])
            lon = float(point.attrib["lon"])
        except (KeyError, ValueError) as exc:
            raise SystemExit("GPX contains a track point without numeric lat/lon") from exc
        if not (math.isfinite(lat) and math.isfinite(lon)):
            raise SystemExit("GPX contains a non-finite coordinate")
        if not -90 <= lat <= 90 or not -180 <= lon <= 180:
            raise SystemExit("GPX contains an out-of-range coordinate")
        points.append((lat, lon))

    if len(track_segments) != 1:
        raise SystemExit(f"Expected exactly one track segment, found {len(track_segments)}")
    if len(points) < 2:
        raise SystemExit("GPX track must contain at least two points")

    segment_lengths = [haversine_km(a, b) for a, b in zip(points, points[1:])]
    duplicate_count = sum(a == b for a, b in zip(points, points[1:]))
    if duplicate_count:
        raise SystemExit(f"GPX contains {duplicate_count} consecutive duplicate point(s)")

    return {
        "track_count": len(root.findall(f".//{{{GPX_NS}}}trk")),
        "track_segment_count": len(track_segments),
        "point_count": len(points),
        "summed_geometry_km": sum(segment_lengths),
        "max_segment_m": max(segment_lengths) * 1000,
        "first_point": list(points[0]),
        "last_point": list(points[-1]),
    }


def main() -> None:
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("source", type=Path, help="Original GPX/KML file")
    parser.add_argument(
        "--captured-at",
        help="Capture timestamp in ISO-8601 UTC; defaults to current UTC time.",
    )
    parser.add_argument(
        "--output",
        type=Path,
        default=Path("data/routes/centenario-source-capture.json"),
    )
    args = parser.parse_args()

    source = args.source.resolve()
    if not source.is_file():
        raise SystemExit(f"Source file not found: {source}")

    evidence = OFFICIAL.get(source.name)
    if evidence is None:
        allowed = ", ".join(sorted(OFFICIAL))
        raise SystemExit(f"Unexpected filename {source.name!r}; expected one of: {allowed}")

    data = source.read_bytes()
    if not data:
        raise SystemExit("Source file is empty")
    try:
        data.decode("utf-8")
    except UnicodeDecodeError as exc:
        raise SystemExit("Source file is not valid UTF-8 XML text") from exc

    captured_at = args.captured_at or datetime.now(timezone.utc).replace(microsecond=0).isoformat()
    record = {
        "schema_version": 2,
        "route_id": "caminho-do-centenario",
        "source_file": source.name,
        "format": evidence["format"],
        "official_url": evidence["url"],
        "captured_at": captured_at,
        "byte_count": len(data),
        "sha256": sha256(source),
        "source_bytes_unchanged": True,
        "promotion_status": "awaiting_full_route_validation",
    }
    if evidence["format"] == "gpx":
        record["gpx_metrics"] = gpx_metrics(data)

    args.output.parent.mkdir(parents=True, exist_ok=True)
    args.output.write_text(json.dumps(record, ensure_ascii=False, indent=2) + "\n", encoding="utf-8")
    print(json.dumps(record, ensure_ascii=False, indent=2))


if __name__ == "__main__":
    main()
