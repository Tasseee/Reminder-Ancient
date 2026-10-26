# Ancient Reminder 2.0 (Fabric, Minecraft 1.21.11)

## Bauen
1. JDK 21 installieren.
2. Am einfachsten: auf https://fabricmc.net/develop/template/ ein Projekt für 1.21.11 mit
   **Mojang Mappings** erzeugen (liefert gradlew + Wrapper) und dann in dieses Projekt
   den Ordner `src/` sowie `build.gradle` und `gradle.properties` aus diesem Paket kopieren.
   Alternativ in diesem Ordner einmal `gradle wrapper --gradle-version 9.2.1` ausführen.
3. `./gradlew build` -> fertige Mod in `build/libs/ancient-reminder-2.0.0.jar`.
4. Alte `ancient-reminder-1_0_0.jar` aus dem mods-Ordner löschen (gleiche Mod-ID!).

## Bedienung
- Menü: Rechte Umschalt-Taste oder `/ancient`
- Chunkgrenzen an/aus: F9 (änderbar unter Steuerung)
- `/ancient hud` (HUD-Editor), `/ancient stats`, `/ancient rescan`,
  `/ancient calibrate reset`, `/ancient tunnel clear`, `/ancient chunks`
- Config: `config/ancientreminder.json`, Spot-Log: `config/ancientreminder_data.json`
- RTP-Befehl ändern (falls nicht /rtp): `rtpCommand` in der Config.

## Kalibrierung
10x `/rtp`, jedes Mal auf Y15 gehen. Pro RTP wird eine Messung gespeichert.
