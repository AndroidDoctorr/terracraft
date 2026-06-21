# Multi-version layout

Terracraft is split into **one Forge Gradle module per Minecraft version**. Each folder has its own Gradle wrapper and can be built independently.

| Folder | Minecraft | Forge | Java | Status |
|--------|-----------|-------|------|--------|
| `1.18.2/` | 1.18.2 | 40.3.0 | 17 | Bootstrap only |
| `1.19.2/` | 1.19.2 | 43.5.0 | 17 | Bootstrap only |
| `1.20.1/` | 1.20.1 | 47.4.10 | 17 | **Active development** (v0.5.x) |
| `1.21.1/` | 1.21.1 | 52.1.0 | 21 | Bootstrap only |

Shared across versions:

- `shared/src/main/resources/` — lang files and other version-agnostic assets
- `documentation/` — design docs
- `tools/` — biome/ecoregion generators (used when porting datapacks)
- `LICENSE` — bundled into each module’s JAR

Version-specific code and datapacks live under each module’s `src/main/`.

---

## Build & run

From the version folder:

```bat
cd 1.18.2
gradlew build
gradlew runClient
```

Same pattern for `1.19.2`, `1.20.1`, and `1.21.1`.

First run downloads Minecraft/Forge and decompiles sources — expect a long wait.

---

## Bootstrap modules (1.18 / 1.19 / 1.21)

These load the `terracraft` mod id and shared lang, but **do not** include Planet Earth generation yet. Port order is likely:

1. **1.21.1** — align with current Materia target
2. **1.19.2** — intermediate Forge API
3. **1.18.2** — match Materia 1.18 if needed

When porting from `1.20.1/`:

1. Copy/adapt Java under `com.torr.terracraft` (registry and worldgen APIs differ per version).
2. Copy version-specific datapacks from `1.20.1/src/main/resources/data/` (worldgen JSON formats change).
3. Re-run `tools/generate_terracraft_biomes.ps1` after adjusting templates.
4. Bump `mod_version` in that module’s `gradle.properties`.

---

## MDK notes

- **1.21.1** uses `reobf = false` (official mappings at runtime) and **Java 21** — see that module’s `build.gradle`.
- **1.18–1.20** use Java 17 and `finalizedBy 'reobfJar'`.
- Example mod sources from the unpacked MDKs were removed; only `com.torr.terracraft` remains.
