#!/usr/bin/env python3
"""Record an unmodified official Centenario GPX/KML source for later V1 promotion.

This tool never rewrites source bytes and never creates route geometry. It only
records evidence (file metadata + SHA-256) and validates the expected filename.
"""
from __future__ import annotations

import argparse
import hashlib
import json
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


def sha256(path: Path) -> str:
    digest = hashlib.sha256()
    with path.open("rb") as handle:
        for chunk in iter(lambda: handle.read(1024 * 1024), b""):
            digest.update(chunk)
    return digest.hexdigest()


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
        "schema_version": 1,
        "route_id": "caminho-do-centenario",
        "source_file": source.name,
        "format": evidence["format"],
        "official_url": evidence["url"],
        "captured_at": captured_at,
        "byte_count": len(data),
        "sha256": sha256(source),
        "source_bytes_unchanged": True,
        "promotion_status": "awaiting_parse_and_validation",
    }

    args.output.parent.mkdir(parents=True, exist_ok=True)
    args.output.write_text(json.dumps(record, ensure_ascii=False, indent=2) + "\n", encoding="utf-8")
    print(json.dumps(record, ensure_ascii=False, indent=2))


if __name__ == "__main__":
    main()
