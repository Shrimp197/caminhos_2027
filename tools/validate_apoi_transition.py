#!/usr/bin/env python3
"""Validate the controlled 2026-to-2027 APOI transition against the normalized master."""

from __future__ import annotations

import json
import sys
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
DEFAULT_TRANSITION = ROOT / "data" / "apoi" / "apoi-2027-transition.json"
DEFAULT_MASTER = ROOT / "data" / "apoi" / "apoi-master.json"
CATEGORIES = {
    "ALIMENTACAO", "AGUA", "DESCANSO", "PERNOITA",
    "DUCHES", "CARREGAMENTO", "TRANSPORTE", "EMERGENCIA",
}
TARGET_STATUSES = {"awaiting_confirmation", "historical", "candidate"}
EXPECTED_PUBLICATION = {
    "awaiting_confirmation": "REVIEW",
    "historical": "HISTORICAL",
    "candidate": "CANDIDATE",
}
EXPECTED_AVAILABILITY = {
    "awaiting_confirmation": "AWAITING_CONFIRMATION",
    "historical": "HISTORICAL",
    "candidate": "AWAITING_CONFIRMATION",
}


def validate(transition_path: Path, master_path: Path) -> list[str]:
    errors: list[str] = []
    try:
        transition = json.loads(transition_path.read_text(encoding="utf-8"))
        master = json.loads(master_path.read_text(encoding="utf-8"))
    except (OSError, json.JSONDecodeError) as exc:
        return [f"invalid JSON: {exc}"]

    if transition.get("source_year") != 2026:
        errors.append("transition source_year must be 2026")
    if transition.get("target_year") != 2027:
        errors.append("transition target_year must be 2027")
    if transition.get("route_id") != master.get("route_id"):
        errors.append("transition route_id must match master route_id")
    if transition.get("source_dataset") != "app/src/main/assets/data/apoios-2026.json":
        errors.append("transition source_dataset must point to the 2026 reference asset")
    if not isinstance(transition.get("items"), list):
        return errors + ["transition items must be a list"]
    if not isinstance(master.get("items"), list):
        return errors + ["master items must be a list"]

    master_by_id = {item.get("id"): item for item in master["items"] if isinstance(item, dict)}
    seen: set[str] = set()
    for index, item in enumerate(transition["items"]):
        prefix = f"items[{index}]"
        if not isinstance(item, dict):
            errors.append(f"{prefix} must be an object")
            continue

        source_id = item.get("source_id")
        if not isinstance(source_id, str) or not source_id.strip():
            errors.append(f"{prefix}.source_id must be non-empty")
            continue
        if source_id in seen:
            errors.append(f"duplicate transition source_id: {source_id}")
        seen.add(source_id)

        master_item = master_by_id.get(source_id)
        if master_item is None:
            errors.append(f"{prefix}.source_id not found in master: {source_id}")
            continue

        status = item.get("target_status")
        if status not in TARGET_STATUSES:
            errors.append(f"{prefix}.target_status is invalid")
            continue

        expected_publication = EXPECTED_PUBLICATION[status]
        expected_availability = EXPECTED_AVAILABILITY[status]
        actual_publication = master_item.get("publication", {}).get("status")
        actual_availability = master_item.get("availability", {}).get("status")
        if actual_publication != expected_publication:
            errors.append(
                f"{prefix} status {status} expects master publication {expected_publication}, got {actual_publication}"
            )
        if actual_availability != expected_availability:
            errors.append(
                f"{prefix} status {status} expects master availability {expected_availability}, got {actual_availability}"
            )

        services = item.get("published_services")
        if not isinstance(services, list):
            errors.append(f"{prefix}.published_services must be a list")
        elif any(service not in CATEGORIES for service in services):
            errors.append(f"{prefix}.published_services contains an invalid category")
        elif status in {"historical", "candidate"} and services:
            errors.append(f"{prefix} {status} entries cannot expose published services")

    if len(seen) != len(master_by_id):
        missing = sorted(set(master_by_id) - seen)
        errors.append(f"transition must account for every master item; missing: {', '.join(missing)}")

    return errors


def main() -> int:
    transition = Path(sys.argv[1]) if len(sys.argv) > 1 else DEFAULT_TRANSITION
    master = Path(sys.argv[2]) if len(sys.argv) > 2 else DEFAULT_MASTER
    errors = validate(transition, master)
    if errors:
        print("APOI transition validation FAILED")
        for error in errors:
            print(f"- {error}")
        return 1
    print(f"APOI transition validation OK: {transition}")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
