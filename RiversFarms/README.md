# RiversFarms (v1.0)

**RiversFarms** is a Paper 1.21.x farming plugin focused on immersive right‑click harvesting, moisture logic, compost, and a custom watering can.

## Features
- Right‑click harvest with hoes (r=2), sweep particles, 1 durability per swing, Fortune support, 1s cooldown.
- Watering Can (**SOUL_LANTERN** item): r=0, 5‑tick FALLING_WATER particles, hydrates farmland to moisture 7, grows crop +1 with per‑crop (PDC) cooldown, sound **ITEM_BONE_MEAL_USE**.
- Composters: full composter yields **Compost** (**BROWN_DYE**); using Compost on a crop applies AoE growth (r=1, 50% chance), sound **ITEM_BONE_MEAL_USE**.
- Natural crop growth: valid vanilla sound **BLOCK_SWEET_BERRY_BUSH_GROW**; dry farmland halves growth; rain can push +2 growth step.
- Farmland moisture lock at 7 (except our harvest sets 7→0 under harvested crops).
- Crop trampling toggle per‑player with persistence and moderator override.
- Commands: `/farmtrample`, `/wateringcan`.

## Build
```bash
mvn clean package
```
The built jar will be at `target/RiversFarms.jar`.
