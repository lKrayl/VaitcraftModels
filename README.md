# Vait Models — entidad genérica para modelos de Blockbench + GeckoLib

Un solo mod, una sola entidad (`vaitmodels:npc`). El modelo que muestra depende de su dato `ModelId`,
así que **añadir un modelo nuevo no requiere programar ni recompilar**: solo soltar 3 archivos.

Comportamiento incluido: idle, caminar, **correr** (cuando le pegan), **mirarte** (cabeza) y
**animación al interactuar** (clic derecho).

## Objetivo (sin confirmar con tu modpack)
- NeoForge **1.21.1**, Java 21, GeckoLib **4.9.3** (rama 1.21.1). Si tu pack es otro, hay que portar el código.
- `gradle.properties → geckolib_version` debe ser la versión del `.jar` de GeckoLib que tienes en `mods/`.
- Este código NO se ha compilado todavía. El primer build puede dar errores menores que se corrigen con el log.

## Compilar
- Con GitHub Actions: sube la carpeta a un repo → pestaña Actions → "Build mod" → descarga el artefacto `vaitmodels-jar`.
- O en local: `./gradlew build` → `build/libs/vaitmodels-1.0.0.jar` → cópialo a `mods/`.

## Probar
```
/summon vaitmodels:npc ~ ~ ~ {ModelId:"example"}
```
También hay un huevo de spawn en la pestaña de creativo (usa el modelo `example`).
- Pasea → `move.walk` · quieto → `misc.idle` · te acercas → gira la cabeza hacia ti
- Clic derecho → `misc.interact` · le pegas → huye con `move.run`

## Añadir tu modelo (Blockbench)
1. Instala el plugin de GeckoLib (File → Plugins → busca "GeckoLib") y crea el proyecto con
   File → New → **GeckoLib Animated Model**. Si ya tienes un modelo en formato Bedrock o Modded Entity:
   File → Convert Project → GeckoLib Animated Model.
2. Rig: todo cubo debe estar dentro de un grupo (solo los grupos se animan), un grupo raíz con hijos anidados,
   y los pivotes en las articulaciones. **El hueso de la cabeza debe llamarse `head`** (minúsculas).
3. Animaciones (pestaña Animate) con estos nombres exactos:
   | Nombre            | Loop           | Cuándo se reproduce             |
   |-------------------|----------------|---------------------------------|
   | `misc.idle`       | sí             | quieto                          |
   | `move.walk`       | sí             | caminando                       |
   | `move.run`        | sí             | corriendo                       |
   | `misc.interact`   | no (play once) | al hacer clic derecho           |
   No animes la rotación de `head` en idle/walk/run: se sumaría al giro hacia el jugador.
   Si falta una animación, simplemente no se reproduce.
4. Exporta: File → Export → **GeckoLib Model** (`.geo.json`), guarda la textura (clic derecho en la textura → Save As)
   y exporta las animaciones desde el panel de animaciones (`.animation.json`).
5. Coloca los archivos con el mismo nombre base (`robot` en el ejemplo):
   ```
   assets/vaitmodels/geo/entity/robot.geo.json
   assets/vaitmodels/animations/entity/robot.animation.json
   assets/vaitmodels/textures/entity/robot.png
   ```
   Puede ir dentro del mod (`src/main/resources/`) **o en un resourcepack** sin recompilar.
   Para usar otro namespace: `{ModelId:"minamespace:robot"}` → `assets/minamespace/...`.
6. `/summon vaitmodels:npc ~ ~ ~ {ModelId:"robot"}`

Guarda siempre tus `.bbmodel`. Puedes dejarlos en `src/main/resources`: el build los excluye del `.jar`.

## Estructura del código
```
VaitModels.java                  entrada del mod (registros + eventos)
registry/ModEntities.java        EntityType "npc" + atributos
registry/ModItems.java           huevo de spawn + pestaña creativa
entity/ModelNpcEntity.java       IA, ModelId sincronizado, controladores de animación, interacción
client/VaitModelsClient.java     registra el renderer (solo cliente)
client/ModelNpcRenderer.java     GeoEntityRenderer
client/ModelNpcModel.java        rutas según ModelId + giro de cabeza
```

## Ajustes típicos
- Hitbox / tamaño: `.sized(0.6F, 1.8F)` en `ModEntities`.
- Más conducta (seguir al jugador, sentarse, atacar): añade goals en `registerGoals()` y controladores en `registerControllers()`.
- Otra animación por interacción: añade otro `.triggerableAnim("nombre", RawAnimation...)` y llámala con `triggerAnim("action", "nombre")`.
