# CloudSMP Weapons

Custom PvP weapons for Paper 1.21.11.

## Weapons
- **Skybreaker Mace** — launches a player and slams them down.
- **Tempest Repeater** — right-click to fire rapidly for five seconds.
- **Cataclysm Bow** — explosive arrows damage nearby entities without damaging blocks.
- **Titan's Grasp** — briefly roots a player when hit.
- **Graviton Trident** — pull a target toward you when a thrown trident hits them.

Every weapon has a custom name, lore, persistent item identifier, and a slot in the `/weapons` GUI.

## Commands
- `/weapons` — open the weapon vault.
- `/weapon <mace|rapidcrossbow|explosivebow|stunaxe|pulltrident>` — give yourself a weapon (operator permission).

## Build
The repository's GitHub Actions workflow compiles the project using Java 21 and Gradle. The JAR will be available as a workflow artifact. Install `CloudSMPWeapons-1.0.0.jar` in the server's `plugins` folder and restart Paper.

Explosive arrows do not break blocks by design.
