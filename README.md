# Welcome to Advancement-Based Spawning!
### Advancement-Based Spawning is a small mod that allows one to control mob spawning of all varieties through simplistic but powerful datapacks!

In this page, we'll go over what the mod can do and how to use it.
## Getting Started
On it's own, ABS does nothing. To begin, you'll need to make a datapack! In this datapack, create the directory:
> data/modid/abs/

###### Note that here, replace `modid` with your datapack's modid

From here, you can start writing JSON files that will control mob spawning!

###### Please familiarize yourself with the JSON language and ensure that you are editing your files with a program that can read and catch errors in them, such as VSCode. Or use [An Online Json Verifier](https://jsonlint.com/) if needed. If your datapack doesn't work, it's likely down to a small formatting error that can be easily caught and fixed with the appropriate file editor!

You can make any number of JSON files, and those files can have any number of entries, though you only really *need* one file to do anything.

###### Though, letting you have multiple is good for organization, if necessary.

These files can be named whatever you like, but you'll ideally want to title them appropriately.

## File Structure

ABS files have numerous fields that define what happens with or without a specific advancement. The structure should be fairly straightforward and easy to understand. ***<span style="color:#BDFF44">Every field is optional.</span>***

###### JSON files don't use //comments, be sure to remove them if you use the following example as a template!

## Root

#### Advancement

The filepath to the advancement that will be controlling spawning rules. 

The [provided example](#example-file), `minecraft:story/enter_the_end`, points to `data/minecraft/advancements/story/enter_the_end.json`

###### This should also work for modded advancements too. For example, the mod Mining Master adds the advancement 'A Sword of Fire and Ice'. In this instance, we'd want to target the file `data/miningmaster/advancements/miningmaster/a_sword_of_ice_and_fire.json`, so, format it as `"miningmaster:a_sword_of_fire_and_ice"`

<hr>

##### Priority

Defines the power of the rule when conflicting rules emerge. The higher the priority is, the more important it is.

For example, a rule with priority `50` that prevents zombies from spawning will be overridden as soon as a rule with priority `100` that explicitly allows zombies to spawn is activated. 

If undefined, defaults to `1000`

<h5>
<details>
  <summary>Priority Claiming: <h3></summary>

  ***<h3><span style="color:red">Only use if you know what you're doing!</span><h4>***

  When comparing two conflicting conditions, the two rules are merged with one another to handle conflicts.
  
  The priority system takes over and modifies the include and exclude list based on the priorities of the two rules. Then, it creates a new rule. The merged rule will retain the higher priority of the two rules.
  
  For example, a rule with priority `50` conflicting with a rule of priority `100`, when merging the rules, the resulting rule will have a priority `100`.

  However,
  `"claim_priority": true`, will force the rule's priority.
  
  For example, a rule with priority `50` that wants to claim the priority, will, when compared against a rule with priority `100`; the resulting rule will use `50` instead of `100`. If both rules claim priority, it again uses the higher one of the two.

</details>

---

#### With/Without

`with:` If the nearest player `to where the entity is trying to spawn at, in a 64 block radius` has the defined advancement, the following rules will be enacted.

`without:` If the nearest player does not have the defined advancement, the following rules will be enacted. 

One or both of these need to be defined for the rule to take effect. Technically, you can include neither, but that does nothing, so, include at least one of them!

<hr>

 - ##### Filter Rules
   - ###### Include/Exclude
     - `include`: What mobs excplicitly will spawn with/without the defined advancement.
   
     - `exclude`: What mobs excplicitly won't spawn with/without the defined advancement.

   - ###### Entities
     - An array of `ResourceLocation/Identifier` entities that will/won't spawn with/without the advancement!

   - ###### Types
     - A list of spawning types that will be checked when passing the rule.

       <details>
          <summary>The list of possible spawning types is as follows: </summary>
          
          - `NATURAL` - Mobs that spawn from ticking chunks, ie ambient spawning
          - `CHUNK_GENERATION` - Mobs that spawn immediately on chunk creation
          - `SPAWNER` - Mobs that spawn from Spawner blocks
          - `STRUCTURE` - Mobs that spawn from a structure
          - `BREEDING` - Mobs that come from bred animals
          - `MOB_SUMMONED` - Mobs that are summoned for backup, eg Evokers summoning Vex's
          - `JOCKEY` - Mobs that ride other mobs, eg Skeleton riding a Spider
          - `EVENT` - Mobs that spawn from an event, eg raids
          - `CONVERSION` - Mobs that are being converted, eg Zombie Villager converting to Villager
          - `REINFORCEMENT` - Mobs that are spawned for reinforcement, eg Zombies spawning more Zombies
          - `TRIGGERED` - Mobs that come from a specific thing, eg Wardens spawning from a shrieker
          - `BUCKET` - Mobs that come from buckets, eg Bucket of Fish
          - `SPAWN_EGG` - Mobs that come from a spawn egg
          - `COMMAND` - Mobs that come from a command, eg /summon
          - `DISPENSER` - Mobs that come from a dispenser
          - `PATROL` - Mobs that come from a patrol, eg Illagers
        </details>
   
       If the spawning types are not defined, the mod will default to checking the `CHUNK_GENERATION, JOCKEY, NATURAL, REINFORCEMENT,` and `PATROL` types, which should cover most typical use-cases. 

       You normally wouldn't want to limit what mobs can be spawned from spawn eggs or commands, and it might be hard to justify an in-universe reason as to why certain mobs can't be properly bred or dropped from a bucket until an advancement is unlocked. Mob spawns from structures and spawners being disabled would allow those structures to be easily raided, like Pillager Outposts, Nether Fortresses and Ocean Monuments. Do what you want though! I'm not your mum!

---

<a id="example-file"></a>
# Example file
```json
{
    "minecraft:story/enter_the_end": {
        // Where do we rank this against other rules, with their own inclusion and exclusion rules?
        "priority": 1000,
        // What do we do when we have this advancement?
        "with": {
            // Make sure that we include these entities to spawn
            "include": {
                "entities": [
                    "minecraft:enderman"
                ],
                // Only if it comes from this spawning type, though.
                "types": [
                    "JOCKEY"
                ]
            },
            // Lets try to make sure these DON'T spawn
            "exclude": {
                "entities": [
                    "minecraft:creeper",
                    "minecraft:zombie"
                ],
                // Only on chunk generation, specifically! 
                "types": [
                    "CHUNK_GENERATION"
                ]
            }
        },
        // What do we do when we DON"T have this advancement?
        "without": {
            "include": [
                "minecraft:spider"
            ],
            // Without this advancement, lets just disable skeleton spawners
            "exclude": {
                "entities": [
                    "minecraft:skeleton"
                ],
                "types": [
                    "SPAWNER"
                ]
            }
        }
    }
}
```