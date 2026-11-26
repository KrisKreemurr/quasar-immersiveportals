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

- 2026-11-25: background worlds tick their particles without Immersive Portals' camera hack.
  - New `PortalParticles`: each background world (another dimension seen through a portal) gets its own
    `ParticleEngine`. `MixinClientLevel_Particles` and `MixinClientPacketListener` route a background world's
    `Minecraft.particleEngine` reads to it, so its particles (lava pops, ash, mob crits, block-break bits) no longer
    appear in the player's world at the other world's coordinates.
  - `tickRemoteWorldRandomTicksClient` no longer moves the main camera or ticks the main particle engine a second
    time (particles near a portal used to age twice as fast). It sets the background world's observer position (the
    player mapped through the portal), which the particle distance check in `ClientLevel.doAddParticle` now uses
    for background worlds, and runs `animateTick` there. `tickRemoteWorld` ticks the world's own engine.
  - Cross-dimension portal views draw the destination world's particles.
  - Engines are dropped when their world is disposed or becomes the player's world.
  - `tickWeatherEffects` is skipped for background worlds (it spawns rain splashes and rain sounds around the
    main camera: Overworld rain was audible from the Nether).

- 2026-11-25: portal views decide entity visibility with the entity renderer's own frustum check instead of
  `EntityRenderDispatcher.shouldRender`. Occlusion-culling mods (Entity Culling) hook the dispatcher and answer for the
  player's eye in the player's world; through a portal that made mobs blink as their culled flag flipped.

- 2026-11-25: background-world client ticking (`IPCGlobal.isClientRemoteTickingEnabled`) is on again. It had been
  turned off because of the camera hack removed above. Without it, background entities never advanced their previous
  position (mobs jumped back and forth in portal views), queued light packets of background worlds were never applied
  (`PortalWorldTerrain` no longer polls them), and background particles never spawned or aged.

- 2026-11-25: entities, block entities and particles of a background world in a cross-dimension portal view are lit
  with that world's lightmap (the one `PortalWorldTerrain` lights its terrain with): `MixinGameRenderer_PortalLightmap`
  returns `PortalEntities.lightmapOverride()` from `GameRenderer.lightmap()` while they draw. With the Overworld's
  lightmap, Nether mobs and chests came out near black.

- 2026-11-26: background-world entity/world ticking is off again (player animations were choppy with it on, and
  background mobs stood still anyway). Background worlds now always get `tickRemoteWorldLight`: their queued light
  packets (inside `withSwitchedWorld`), their own particle engine, and ambient particles around the portal.

- 2026-11-26: with full background ticking off, background entities still get the movement part of a tick
  (`ClientWorldLoader.stepRemoteEntities`): previous position/rotation (and head/body yaw) set to current, then one
  interpolation step toward the server's last position. Stops mobs jumping back and forth in portal views.

- 2026-11-26: mobs seen through a portal from another dimension keep wandering. Vanilla resets a mob's idle timer
  (`noActionTime`) only when a player in the same dimension is within 32 blocks; otherwise `RandomStrollGoal` stops after
  100 ticks (mobs in the portal-loaded Nether stood still, only looking around). `MixinMob_PortalWatchers` (end of
  `Mob.checkDespawn`) also resets it when a player in another dimension watches the mob's chunk within two chunks of the
  portal's far side (`ImmPtlChunkTracking` watch record, loaded to the player).
- 2026-11-26: `stepRemoteEntities` also advances `tickCount` and the walk animation (`calculateEntityAnimation`), so
  background mobs' legs move.
- 2026-11-26: `stepRemoteEntities` also turns background mobs: the head eases toward the yaw the server sent
  (`lerpHeadRotationStep`, as in `LivingEntity.aiStep`) and the body turns toward the walking direction within reach of
  the head (`tickHeadTurn`, as in `LivingEntity.tick`), through the `IELivingEntity_QuasarTurn` accessor.

- 2026-11-26: crossing a portal into another dimension hands the render side over (`ClientTeleportationManager
  .swapRenderWorlds`, step 4). The entered world's `levelExtractor` field becomes the main `LevelExtractor`, the world
  left behind takes the entered world's `SecondaryLevelExtractor` (all dirty) and is registered as foreign, and the main
  extractor, particle engine and game renderer are pointed at the entered world (vanilla `updateLevelInEngines` minus
  sound stop and camera reset). `MixinClientLevel` exposes the extractor field (`IEClientWorld.ip_get/setLevelExtractor`).
- 2026-11-26: removed the guard in `Portal.canTeleportEntity` that kept cross-dimension portals from teleporting
  (it was waiting for step 4).
- 2026-11-26: background client worlds now share the player's world game time (copied at creation and every tick).
  Time packets only reach the listener's level, so a background world sat at game time 0 and crossing into it reset
  `StableClientTimer` ("target time is too far behind"). Day/night clocks are already shared via `ClientClockManager`.
- 2026-11-26: crossing back into a world showed no terrain (F3 `C: 0/26136`). 26.x's `LevelExtractor.allChanged`
  starts a fresh `SectionUpdateTracker` that only learns chunks from the chunk cache's added-chunk/empty-section
  sets; the world's chunks had been announced before it went to the background. `swapRenderWorlds` now calls
  `ImmPtlClientChunkMap.ip_reannounceAllChunks()` after `main.setLevel`.
- 2026-11-26: `ImmPtlClientChunkMap.onLightUpdate` dirtied sections in `Minecraft.levelExtractor` for every world, so a
  background world's light rebuilt the player's world's sections at the same coordinates (and never reached the
  portal terrain). It now uses the chunk map's own world's extractor.
- 2026-11-26: crossing stand-in. `swapRenderWorlds` calls `PortalWorldTerrain.beginStandIn(toWorld)`: the entered
  world's portal terrain is kept (taken out of `WORLDS`) and `PortalViewRenderer.renderAll` draws it with the main
  camera into the main target after the main opaque terrain, skipping every section the main renderer has had a
  mesh for at least 4 frames (`ViewArea.getRenderSectionAt`, found by type via reflection; origin must match,
  mesh != `CompiledSectionMesh.UNCOMPILED`). Freed when nothing is left, after 10 s, or on leaving that world.
- 2026-11-26: passing through a portal no longer flashes the source world. For the last frame or two before the
  teleport tick the camera is within the near clip distance of the portal plane (or just past it), so the composite
  quad was clipped away / the portal was skipped. `PortalViewRenderer.crossing(portal, cam)` (within 0.25 in front
  to 0.5 behind the plane, inside the frame, not mirrors) now draws that portal's view every frame over the whole
  screen (full-screen quad just past the near plane), and `virtualCamera` clamps the clip plane instead of giving up.
- 2026-11-26: `swapRenderWorlds` resets and re-samples the main camera's `EnvironmentAttributeProbe` for the entered
  world, so the first frames after crossing don't use the old world's sky/fog colours (blended per tick).
- 2026-11-26: step 5 - Nether portals are Immersive Portals portals again: `IPConfig` applies the configured
  `netherPortalMode` (default `adaptive`) instead of forcing vanilla. End portals still forced vanilla.
- 2026-11-26: the passing-through composite is exact: a full-screen quad whose fragment shader keeps the portal
  view only where the pixel's view ray crosses the portal rectangle (camera in front, mode 1), so standing in the
  frame looking along it shows each world on its own side. Just past the plane (before the teleport tick, mode 2)
  the destination fills the screen and its view uses the plain near plane instead of the portal clip plane.
- 2026-11-26: portal views within 16 blocks or covering over 5% of the screen redraw every frame (others keep the
  adaptive every-N rate); a reused picture slid against the portal frame while moving.
- 2026-11-26: crossing hold. For a moment after crossing, `MixinLevelRenderer_CrossingHold` (LevelRenderer.render
  TAIL, after the whole world pass) has `PortalViewRenderer.drawCrossingHold` draw the new world the way the portal
  showed it - sky, the stand-in's terrain, entities/particles, from the main camera - into an offscreen image and
  paste it over the whole frame (`portal_crossing_hold` pipeline: blended, no depth). The main renderer's mixed-world
  frames (old terrain with the new sky, then an empty frame) never reach the screen. Hands over (0.2 s fade, alpha in
  the composite uniform's `uScreen.w`) once at most 2 sections in view lack a main-renderer mesh for 3 frames, or
  after 1.5 s.
- 2026-11-26: crossing freeze-frame (the user's idea). While a portal is within 1.5 blocks of the camera, each
  finished world picture (LevelRenderer.render TAIL, before hand/HUD) is copied to `quasar_crossing_freeze`; right
  after a crossing that frozen picture covers the frame (hand/HUD stay live) until the main renderer has the view, at
  most 0.35 s, then fades over 0.15 s. The live hold remains the fallback if the copy fails.
- 2026-11-26: fix: the crossing stand-in/hold/freeze ended on the first frame after almost every crossing. Right after
  the teleport the main section grid is still centred on the old position, so every lookup missed and read as
  "covered". Until the grid holds the camera's section everything counts as uncovered, and "caught up" needs more
  than 4 frames.
- 2026-11-26: the crossing freeze-frame turns with the camera (composite mode 3 reprojects each pixel's view ray into
  the picture's projection * rotation), and fades out over 0.25 s instead of 0.15 s.
- 2026-11-26: the crossing freeze-frame turns with the camera (composite mode 3 reprojects each pixel's view ray into
  the picture's projection * rotation), and fades out over 0.25 s instead of 0.15 s.
- 2026-11-26: your other half: entities of the player's world standing in a cross-dimension portal's doorway (you in
  third person, mobs) are also drawn in that portal's view, moved through it (translation-only portals, i.e. Nether
  portals); the portal camera's oblique near plane cuts off the part still on this side.
- 2026-11-26: doorway entities only go through the portal face they are entering from its front side (a Nether portal
  is two back-to-back portals; from behind you were drawn in the front face too).
- 2026-11-26: doorway entities are lit with the destination world's block/sky light where their other half is (with
  the Nether's lightmap your Overworld sky light came out near black: "the body disappears").
- 2026-11-26: an entity standing in a cross-dimension doorway is not drawn in the main view when the camera is behind
  the face it walks into (its far half is in the other world, its near half behind the portal surface) - you were
  visible from both sides (mixin EntityRenderDispatcher.shouldRender).
- 2026-11-26: third-person camera through a portal (the line from the eye back to the camera crosses a portal inside
  its frame): the destination world is drawn over the whole screen except where the view looks back through the portal
  (composite mode 4), so you see the other world around you and your own world (and yourself) through the doorway.
  Fix: composite() uploads the full 208-byte uniform block (it was 144 since uPictureViewProj was added).
