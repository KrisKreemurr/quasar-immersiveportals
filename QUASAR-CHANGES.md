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

- 2026-11-25: fixed black Overworld chunks near cross-dimension portals. `PortalWorldTerrain.prepare` called the
  background world's `pollLightUpdates()` outside `withSwitchedWorld`; those queued tasks are vanilla packet
  handlers that write light data into the packet listener's current world, so the Nether's light (no sky light)
  was written into the Overworld chunks at the same coordinates. Removed; `tickRemoteWorld` already polls them
  each tick with the world switched. The light engine's own `runLightUpdates()` stays.

- 2026-11-25: entities through cross-dimension portals.
  - Server (`MixinTrackedEntity`): 26.x sends every entity update through `TrackedEntity.sendToTrackingPlayers`
    / `sendToTrackingPlayersFiltered` and removes through `ServerEntity.removePairing`. For a watcher in another
    dimension (seeing the entity through a portal) these are now wrapped as redirected packets, so movement,
    metadata, equipment and removal reach that watcher's background world instead of the world the player is in
    (before, only the spawn packet was redirected: mobs appeared frozen in the background world and the client
    logged "missing entity for data tracking"). Same-dimension watchers get plain packets as before.
  - Client: `PortalEntities.draw` takes the world to draw; cross-dimension portal views now draw the background
    world's entities (no particles: those exist for the player's world only).
  - Block entities (chests, signs, banners, beds, heads...) are drawn in all portal views and mirrors within the
    entity distance, extracted with vanilla's `BlockEntityRenderDispatcher` (camera position moved to the portal
    camera, then restored) and submitted like vanilla's `submitBlockEntities`. New setting "Block Entities
    Through Portals" (on by default).
