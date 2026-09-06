#!/usr/bin/env python3
"""Validate the synthetic APOI dataset used only by debug SR/HF environments."""

from __future__ import annotations

import json
from pathlib import Path


ROOT = Path(__file__).resolve().parents[1]
DATASET = ROOT / "app" / "src" / "debug" / "assets" / "data" / "qa" / "apoi-qa.json"
ROUTES = {"sr-test", "hf-test"}
SERVICES = {
    "ALIMENTACAO",
    "AGUA",
    "DESCANSO",
    "PERNOITA",
    "DUCHES",
    "CARREGAMENTO",
    "TRANSPORTE",
    "EMERGENCIA",
}
COST_MODELS = {"FREE", "OPTIONAL_CONTRIBUTION", "PAID", "UNKNOWN"}
RESERVATION_POLICIES = {"NOT_REQUIRED", "RECOMMENDED", "REQUIRED", "UNKNOWN"}
AVAILABILITY_STATUSES = {"CURRENT", "RECURRING", "AWAITING_CONFIRMATION", "HISTORICAL", "EXPIRED", "CLOSED"}
PUBLICATION_STATUSES = {"PUBLISHED", "PUBLISHED_WITH_WARNING", "HISTORICAL", "CLOSED", "REVIEW"}


def fail(message: str) -> None:
    raise SystemExit(f"QA APOI validation failed: {message}")


def main() -> None:
    if not DATASET.is_file():
        fail(f"dataset not found: {DATASET}")

    data = json.loads(DATASET.read_text(encoding="utf-8"))
    if data.get("schema_version") != "1.0-qa":
        fail("schema_version must be 1.0-qa")
    if data.get("environment") != "TEST":
        fail("environment must be TEST")
    if set(data.get("items", [])) == set():
        fail("dataset must contain QA items")

    items = data["items"]
    ids = [item.get("id") for item in items]
    if len(ids) != len(set(ids)):
        fail("item ids must be unique")

    seen_services_by_route = {route: set() for route in ROUTES}
    seen_costs = set()
    seen_reservations = set()
    seen_availability = set()
    seen_publications = set()
    multi_service = False

    for item in items:
        location = item.get("location", {})
        route_id = location.get("route_id")
        if route_id not in ROUTES:
            fail(f"item {item.get('id')} uses unsupported route_id {route_id!r}")
        if not str(item.get("name", "")).endswith("TESTE"):
            fail(f"item {item.get('id')} must be visibly marked TESTE")
        route_km = location.get("route_km")
        if not isinstance(route_km, (int, float)) or route_km < 0:
            fail(f"item {item.get('id')} must have a non-negative route_km")
        relation = location.get("route_relation")
        if relation not in {"ON_ROUTE", "NEAR_ROUTE", "ACCESSIBLE_WITH_DETOUR", "LOCATION_UNCERTAIN"}:
            fail(f"item {item.get('id')} has unsupported route relation {relation!r}")

        services = set(item.get("services", []))
        if not services or not services <= SERVICES:
            fail(f"item {item.get('id')} has invalid services {sorted(services)}")
        seen_services_by_route[route_id].update(services)
        multi_service = multi_service or len(services) > 1

        cost_model = item.get("cost", {}).get("model", "UNKNOWN")
        if cost_model not in COST_MODELS:
            fail(f"item {item.get('id')} has invalid cost model {cost_model!r}")
        seen_costs.add(cost_model)

        reservation = item.get("reservation", {}).get("policy", "UNKNOWN")
        if reservation not in RESERVATION_POLICIES:
            fail(f"item {item.get('id')} has invalid reservation policy {reservation!r}")
        seen_reservations.add(reservation)

        availability = item.get("availability", {}).get("status", "AWAITING_CONFIRMATION")
        if availability not in AVAILABILITY_STATUSES:
            fail(f"item {item.get('id')} has invalid availability {availability!r}")
        seen_availability.add(availability)

        publication = item.get("publication", {}).get("status", "REVIEW")
        if publication not in PUBLICATION_STATUSES:
            fail(f"item {item.get('id')} has invalid publication status {publication!r}")
        seen_publications.add(publication)

    for route_id in ROUTES:
        missing = SERVICES - seen_services_by_route[route_id]
        if missing:
            fail(f"{route_id} is missing service scenarios: {sorted(missing)}")

    if not multi_service:
        fail("dataset must contain at least one multi-service APOI")
    if seen_costs != COST_MODELS:
        fail(f"cost coverage incomplete: {sorted(seen_costs)}")
    if seen_reservations != RESERVATION_POLICIES:
        fail(f"reservation coverage incomplete: {sorted(seen_reservations)}")
    if seen_availability != AVAILABILITY_STATUSES:
        fail(f"availability coverage incomplete: {sorted(seen_availability)}")
    if seen_publications != PUBLICATION_STATUSES:
        fail(f"publication coverage incomplete: {sorted(seen_publications)}")

    print("QA APOI validation OK: SR/HF synthetic coverage is complete and isolated")


if __name__ == "__main__":
    main()
