# PROJECT STATE — V1

## Branch

- Development branch: `v1-route-import`
- Do not write V1 development changes to `main`.
- State verified against GitHub on 2026-09-07.

## Current functional baseline

- Current branch / functional baseline: `2345301dfe84e1be69e570d07467ccf0c7b50927`.
- The six primary walking surfaces remain the acceptance baseline: preparation, active walking map, contextual bottom sheet, next-APOI horizon, APOI list/cards and walking progress.
- The preparation surface is now composed as a product flow with secondary subsections for route, stage/start/end, audio, map orientation, breaks, APOIs and notes.
- Debug-only QA route/catalog access, deterministic raw GPS simulation controls and visible walking QA controls remain enforced.

## Route

- Production route asset: `app/src/main/assets/data/route.geojson`.
- Source declared by the asset: ACF official GPX.
- Published route distance remains distinct from technical geometry length.
- Historical `ACF_2020` KML remains reference-only.
- The Centenário preparation hero uses the bundled `caminho_centenario_hero.jpg`; QA routes use an explicit non-production visual treatment.

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
- Route stages are loaded from `app/src/main/assets/data/route-stages.json` and enriched into the route model.
- The preparation surface keeps route, stage/start/end and other choices in the walking preparation model instead of introducing a second navigation/domain state machine.
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

- `Build Android APK` #915 — **success** for `2345301dfe84e1be69e570d07467ccf0c7b50927`.
- `V1 Route Source Provenance` #1108 — **success** for the same commit.
- The successful Build run completed JVM tests, debug APK assembly, APK verification and artifact upload.
- Current debug artifact: `Caminhos-do-Peregrino-v1-route-import-debug` from Build #915.
- Physical Android GPS and real-user HF validation are not replaceable by CI; they remain device/human validation steps.

## Acceptance scenario

The executable SR/HF scenario is documented in `docs/QA-SR-HF-SCENARIO.md` and is intended to be run on the current debug APK.

## Next logical block

1. Use the current debug APK for SR/HF execution and record only reproducible defects.
2. Consolidate the preparation visual hierarchy against the approved visual reference without changing domain/state rules.
3. Re-run the physical Android Centenário GPS validation with the current APK.
4. Move to HF human evaluation after the functional SR path is demonstrably stable.

## Integrity rule

The GitHub branch state is authoritative. Documentation, conversation summaries and transfer prompts never override code/tests actually present in the repository.
