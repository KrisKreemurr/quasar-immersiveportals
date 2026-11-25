# Quasar MC changes

This is a modified version of Immersive Portals for NeoForge by qouteall, Nick1st and the iPortalTeam contributors
(https://github.com/iPortalTeam/ImmersivePortalsModForNeo, branch `1.21`), licensed under the Apache License 2.0
(see `LICENSE`). Modified for Quasar MC by KrisKreemurr.

## Changes
- Build: ModDevGradle single project for Minecraft 26.3 / NeoForge 26.3.0.115, Java 25 (was NeoGradle, 1.21.1).
- Sodium/Iris compat and the Cloth Config screen are left out of the build (not in Quasar MC's pack).
- Porting to 26.3 in progress.
- 2026-11-25, cross-dimension step 3 (terrain): portal views into other dimensions. `render/quasar/PortalWorldTerrain`
  meshes the background world's sections with vanilla's `SectionCompiler` on two worker threads (marked foreign for
  Quasar Meshelium) and draws them with vanilla's terrain pipelines; section positions are shifted so vanilla's
  Globals (main camera) work for the portal camera. Destination fog colour/distances from that dimension's
  environment attributes; sky only when both worlds have the same kind of sky. Background worlds run their own light
  updates there. Background worlds are registered with Quasar Meshelium so its far field ignores their chunks.
  Not yet: entities/block entities from the other world, its own lightmap, clouds/far terrain there.
