# Terracraft documentation

Design notes and reference for the Terracraft Earth worldgen mod.

## Start here

- [overview.md](overview.md) — project status, repo layout, in-game quick test
- [../README.md](../README.md) — public-facing project README
- [../AGENTS.md](../AGENTS.md) — LLM / agent context

## World generation

- [world-generation-options.md](world-generation-options.md) — Flora, map scale, vertical scale, biome source fields
- [elevation-scaling.md](elevation-scaling.md) — DEM → Y level mapping and config tuning
- [ecoregions-data.md](ecoregions-data.md) — WWF TEOW / ArcGIS GeoJSON setup
- [multi-version-setup.md](multi-version-setup.md) — per-MC-version Gradle modules

## Flora & Materia

- [plant-distribution-reference.md](plant-distribution-reference.md) — **master table** for vanilla and Materia vegetation on Planet Earth; geographic/climate tags; known gaps and maintenance checklist

## Planning

- [design-principles.md](design-principles.md) — resolved decisions, ornamental placement rules, mission-aligned ideas
- [variation-smoothing-detail-plan.md](variation-smoothing-detail-plan.md) — roadmap: biome variants, terrain smoothing, future species

## Agent / developer reference

- [reference/README.md](reference/README.md) — architecture, code map, integration notes
