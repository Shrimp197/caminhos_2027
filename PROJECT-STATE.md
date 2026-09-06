# PROJECT STATE — V1

## Branch

- Development branch: `v1-route-import`
- Do not write V1 development changes to `main`.
- State verified against GitHub on 2026-09-06.

## Current functional baseline

- Current branch / functional baseline: `9a577f7abdf8175b15e8fb7b433ddc6e8aaf0000`.
- The six primary walking surfaces are the explicit acceptance baseline: preparation, active walking map, contextual bottom sheet, next-APOI horizon, APOI list/cards and walking progress.
- Debug-only QA route/catalog access, deterministic raw GPS simulation controls and visible walking QA controls remain enforced.

## Route

- Production route asset: `app/src/main/assets/data/route.geojson`.
- Source declared by the asset: ACF official GPX.
- Published route distance remains distinct from technical geometry length.
- Historical `ACF_2020` KML remains reference-only.

## Walking V1

Implemented and covered by JVM validation:

- preparation produces `PLANNED` state;
- explicit `startSaved(...)` transition to `ACTIVE`;
- first GPS observation establishes the actual start position;
- start is rejected when the projected GPS position is outside the shared possible-deviation threshold;
- route projection through validated route geometry;
- persistent walking session/checkpoint runtime;
- last reliable position retained during signal loss;
- GPS timestamp/movement protections;
- GPS deviation hysteresis with recovery to `ON_ROUTE`;
- shared `AppState` / `AppStateStore` read model;
- APOI catalogue, filtering, search and next-APOI context;
- APOI detail and decision-support surfaces;
- walking map read model with protection against fabricated geometry;
- six primary walking surfaces integrated into the same V1 state/navigation flow.

## Preparation and QA route execution

- Walking preparation exposes `Caminho do Centenário`, `SR` and `HF` in debug builds.
- `SR` and `HF` are explicitly marked as test environments.
- Preparation allows explicit planned start and destination route km inside the selected route geometry.
- Saving the plan remains distinct from starting the walk; saved plans remain `PLANNED` until valid GPS starts the walk.
- QA route geometry is loaded from the committed GPX assets `percurso-teste-casa-trabalho.gpx` and `percurso-teste-hf.gpx`.
- `GpxSimulationLocationSource` reports raw simulated positions only; QA does not duplicate projection, progress or deviation policy.
- Real Android GPS and QA simulation enter the same downstream walking callback/pipeline.
- Debug walking exposes deterministic advance, signal loss/recovery and deliberate deviation controls.

## APOI QA data

- `app/src/debug/assets/data/qa/apoi-qa.json` is synthetic only and cannot become production data.
- Both SR and HF have coverage for all eight service categories.
- The fixture deliberately exercises multi-service APOI, cost states, reservation states, availability states, publication states, confidence variation, uncertain location, historical records and closed records.
- QA records are visibly labelled `TESTE`.
- `tools/validate_qa_apoi.py` enforces the above coverage and isolation contract.
- `V1 Route Import Validation` runs this validator before JVM tests/build.

## Data / production policy

- Published production APOI catalogue is intentionally empty until qualified 2027 evidence exists.
- 2026 data remains historical/reference data and must not be presented as a 2027 guarantee.
- No invented route, stage or APOI data is to be introduced.
- Navigation turn instructions and external map handoff remain deferred until their geometry/data prerequisites are validated.

## Validation

- `Build Android APK` #864 — **success** for `9a577f7abdf8175b15e8fb7b433ddc6e8aaf0000`.
- `V1 Route Source Provenance` #1006 — **success** for the same commit.
- JVM tests passed; debug APK assembled, verified and uploaded.
- Current debug artifact: `Caminhos-do-Peregrino-v1-route-import-debug`, artifact `9997881198`.
- Artifact SHA-256 digest reported by GitHub Actions: `sha256:bbf56174b3b708b4c1ae4b0b0008ddecdc146675ead879ec30d65ba2f3ceb168`.
- Physical Android GPS and real-user HF validation are not replaceable by CI; they remain device/human validation steps.

## Acceptance scenario

The executable SR/HF scenario is documented in `docs/QA-SR-HF-SCENARIO.md` and is intended to be run on the current debug APK.

## Next logical block

1. Execute SR/HF on the current debug APK: non-zero planned start → first GPS → progress → next APOI → detail → back → decision → signal loss/recovery → deviation/recovery → persistence/resume.
2. Record only reproducible defects from that execution; do not alter thresholds or architecture without evidence.
3. Re-run the physical Android Centenário GPS validation with the current APK.
4. Move to HF human evaluation after the functional SR path is demonstrably stable.

## Integrity rule

The GitHub branch state is authoritative. Documentation, conversation summaries and transfer prompts never override code/tests actually present in the repository.
