#!/usr/bin/env python3
"""Validate the normalized APOI master dataset without network access or build-time rewriting."""

from __future__ import annotations

import json
import sys
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
DEFAULT_DATASET = ROOT / "data" / "apoi" / "apoi-master.json"

CATEGORIES = {
    "ALIMENTACAO", "AGUA", "DESCANSO", "PERNOITA",
    "DUCHES", "CARREGAMENTO", "TRANSPORTE", "EMERGENCIA",
}
PRECISIONS = {"EXACT", "APPROXIMATE", "LOCALITY_ONLY", "UNKNOWN"}
RELATIONS = {
    "ON_ROUTE", "NEAR_ROUTE", "ACCESSIBLE_WITH_DETOUR",
    "DISTANT_POTENTIAL_SUPPORT", "LOCATION_UNCERTAIN", "OUTSIDE_ROUTE",
}
AVAILABILITY = {
    "CURRENT", "FUTURE_CONFIRMED", "RECURRING", "HISTORICAL",
    "EXPIRED", "AWAITING_CONFIRMATION", "CLOSED",
}
PUBLICATION = {
    "CANDIDATE", "REVIEW", "PUBLISHED", "PUBLISHED_WITH_WARNING",
    "HISTORICAL", "CLOSED", "EXCLUDED",
}
COST = {"FREE", "OPTIONAL_CONTRIBUTION", "PAID", "UNKNOWN"}
RESERVATION = {"NOT_REQUIRED", "RECOMMENDED", "REQUIRED", "UNKNOWN"}
ENVIRONMENTS = {"development", "sr", "hf", "production"}


def fail(errors: list[str], message: str) -> None:
    errors.append(message)


def validate(dataset_path: Path) -> list[str]:
    errors: list[str] = []
    try:
        data = json.loads(dataset_path.read_text(encoding="utf-8"))
    except (OSError, json.JSONDecodeError) as exc:
        return [f"invalid JSON: {exc}"]

    if data.get("schema_version") != "1.0-master":
        fail(errors, "schema_version must be 1.0-master")
    if data.get("route_id") != "caminho-centenario":
        fail(errors, "route_id must be caminho-centenario")
    if data.get("environment") not in ENVIRONMENTS:
        fail(errors, "environment is invalid")
    if not isinstance(data.get("items"), list):
        return errors + ["items must be a list"]

    ids: set[str] = set()
    for index, item in enumerate(data["items"]):
        prefix = f"items[{index}]"
        if not isinstance(item, dict):
            fail(errors, f"{prefix} must be an object")
            continue

        item_id = item.get("id")
        if not isinstance(item_id, str) or not item_id.strip():
            fail(errors, f"{prefix}.id must be non-empty")
        elif item_id in ids:
            fail(errors, f"duplicate id: {item_id}")
        else:
            ids.add(item_id)

        if not isinstance(item.get("name"), str) or not item["name"].strip():
            fail(errors, f"{prefix}.name must be non-empty")

        if item.get("main_category") not in CATEGORIES:
            fail(errors, f"{prefix}.main_category is invalid")

        services = item.get("services")
        if not isinstance(services, list) or not services:
            fail(errors, f"{prefix}.services must be a non-empty list")
        elif any(service not in CATEGORIES for service in services):
            fail(errors, f"{prefix}.services contains an invalid category")

        location = item.get("location", {})
        if not isinstance(location, dict):
            fail(errors, f"{prefix}.location must be an object")
            location = {}
        if location.get("precision") not in PRECISIONS:
            fail(errors, f"{prefix}.location.precision is invalid")
        if location.get("route_relation") not in RELATIONS:
            fail(errors, f"{prefix}.location.route_relation is invalid")
        if location.get("route_id") != data.get("route_id"):
            fail(errors, f"{prefix}.location.route_id must match dataset route_id")

        lat, lon = location.get("latitude"), location.get("longitude")
        if (lat is None) != (lon is None):
            fail(errors, f"{prefix}.location latitude/longitude must be both present or both null")
        if lat is not None and not (-90 <= lat <= 90):
            fail(errors, f"{prefix}.location.latitude out of range")
        if lon is not None and not (-180 <= lon <= 180):
            fail(errors, f"{prefix}.location.longitude out of range")

        cost = item.get("cost", {})
        if not isinstance(cost, dict) or cost.get("model") not in COST:
            fail(errors, f"{prefix}.cost.model is invalid")
        elif cost.get("model") == "FREE" and cost.get("amount") not in (None, 0, 0.0):
            fail(errors, f"{prefix}.cost FREE cannot have a non-zero amount")
        elif cost.get("model") == "PAID" and cost.get("amount") is None:
            fail(errors, f"{prefix}.cost PAID must provide an amount or be corrected to UNKNOWN")

        reservation = item.get("reservation", {})
        if not isinstance(reservation, dict) or reservation.get("policy") not in RESERVATION:
            fail(errors, f"{prefix}.reservation.policy is invalid")

        availability = item.get("availability", {})
        if not isinstance(availability, dict) or availability.get("status") not in AVAILABILITY:
            fail(errors, f"{prefix}.availability.status is invalid")

        publication = item.get("publication", {})
        if not isinstance(publication, dict) or publication.get("status") not in PUBLICATION:
            fail(errors, f"{prefix}.publication.status is invalid")

        if data.get("environment") == "production" and publication.get("status") in {"CANDIDATE", "REVIEW"}:
            fail(errors, f"{prefix} cannot be candidate/review in production")
        if publication.get("status") == "PUBLISHED" and availability.get("status") in {"HISTORICAL", "EXPIRED", "CLOSED", "AWAITING_CONFIRMATION"}:
            fail(errors, f"{prefix} cannot be normally published with non-current/confirmed availability")

    return errors


def main() -> int:
    dataset = Path(sys.argv[1]) if len(sys.argv) > 1 else DEFAULT_DATASET
    errors = validate(dataset)
    if errors:
        print("APOI master validation FAILED")
        for error in errors:
            print(f"- {error}")
        return 1
    print(f"APOI master validation OK: {dataset}")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
