This repository has been replaced with this: [GravityLib](https://github.com/Magicalbananapi/GravityLib)

### gravitylib

This github repo is not up to date, I'm working on that, the below changes are completed but not implemented here:

This mod fixes*: https://bugs.mojang.com/browse/MC-119369
\
**and probably introduces some bugs as a result, create an issue if you notice odd behavior with boats when this mod is installed*

|    ID   |           Name          |  Rank  | test |
|    --   |           ----          |  ----  |  --  |
| gravity | get, reset, rotate, set | target |      |
| gravity | get, reset, rotate, set | target |  hi  |

### Command Syntax:
/gravity \<get, reset, rotate, set> \<target> ...

/gravity \<get, reset, rotate, set> \<target> [\<type>] ...

*suggestions will be organized in alphabetical order in game, and I have no idea how to change this

\
/gravity get \<target> [\<attribute>]

/gravity get \<target> [\<type>] [\<attribute>]

\
/gravity reset \<target> [\<attribute>]

/gravity reset \<target> [\<type>] [\<attribute>]

\
/gravity rotate \<target> \<rotation> [\<time>]

/gravity rotate \<target> \<type> <rotation> [\<time>]

\
/gravity set \<target> \<direction> [\<strength>] [\<time>]

/gravity set \<target> \<attribute> (\<direction>|\<strength>|\<time>)

/gravity set \<target> \<type> \<direction> [\<strength>] [\<time>]

/gravity set \<target> \<type> \<attribute> (\<direction>|\<strength>|\<time>)

---
### Command Arguments:
\<target> = (entity \<target>|block \<targetPos>)

\<type> = (base, target, motion, drops, projectiles, special)

\<attribute> = (direction, strength, time)

\<rotation> = (forward, backwards, left, right)

\<direction> = (UP, DOWN, NORTH, EAST, SOUTH, WEST, NONE)

---
### Gravity Types:
base - all other types combined

target - the gravity used for rendering, hitbox rotation, camera rotation, eye height, aim, etc.

motion - the gravity used for movement of the entity/block

drops - the gravity of items dropped by the entity/block

projectiles - the gravity of projectiles shot by the entity/block

special - the gravity of other entities in special cases where an entity/block's gravity is tied to another's
(entities with the same gravity as the nearest player or the opposite gravity of a random player for example)

---
### Special Cases:
/gravity reset \<target> time - refreshes the remaining time of the currently applied gravity back to it's maximum

In general, block entities will only be affected by direction, as such strength and time will not be an option if a coordinate is chosen as the target.
