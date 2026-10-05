# Mystic Arena

A turn-based console RPG in Java. Heroes, battles and rankings are stored in a SQLite database through JDBC.

## Running

Requires a JDK (17 or newer). The SQLite JDBC driver (`sqlite-jdbc-3.53.2.1.jar`) is already in the repo.

| Windows        | macOS / Linux / Git Bash |
|----------------|--------------------------|
| `run.bat`      | `./run.sh`               |
| `run-tests.bat`| `./run-tests.sh`         |

The run scripts compile into `out/` and start the game. Saves go to `game_data.db` in this folder.
The self-test uses a throwaway database (`-Drpg.db=out/selftest.db`), so it never touches your saves.

The scripts cap the JVM heap at 128 MB. The game needs very little memory, and Java's default heap
sizing can crash on machines with little free memory.

## Gameplay

- **4 classes**: Warrior, Mage, Rogue and Paladin. Each has 10 moves. Ultimates unlock at level 10 and master moves at level 20.
- **Signature ability**: one of your class's core moves. Once per battle it can be used at 1.5x power with no HP cost.
- **Stats**: you get 50 points at level 1 and 10 more each level. Spend them manually or use the class's recommended build.
- **Combat**: speed decides who acts first, and Quick Strike and potions always act first. Combat includes buffs and debuffs, stuns, poison, dodges, counters, crits, multi-hits, heals and extra turns.
- **Training Grounds (PvE)**: fight monsters at four difficulty levels for XP and gold.
- **Multiplayer Arena (PvP)**:
  - *Ranked Challenge*: fight any hero in the database. The defender is AI-controlled, so you can battle friends' heroes while they're away. Both ratings change (Elo, K = 32).
  - *Local Duel*: two players on one keyboard, each using their own saved hero.
- **Shop**: Health Potions (up to 2 per battle) and a Scroll of Respec.
- **Leaderboard and battle history**: everything is recorded in the database.
- 8 arena bots are created on first launch, so the arena is never empty.

## Code layout

| File | Purpose |
|------|---------|
| `Main.java` | Main menu, hero create/switch/delete, move management, shop |
| `Arena.java` | Game modes: training, ranked challenges, local duels, rewards, Elo |
| `Combat.java` | Battle engine, move effects and AI opponent |
| `Moves.java` | Move catalog for every class |
| `Character.java` | Class definitions, signature abilities, recommended builds |
| `Player.java` | Hero model, stat allocation, display |
| `LevelSystem.java` | XP curve, rewards, level-ups and unlocks |
| `Database.java` | All JDBC access: schema, saves, queries, migration, bot seeding |
| `Input.java` | Validated console input |
| `tests/SelfTest.java` | Database, migration, balance and battle self-test |

## Database schema

- `players`: one row per hero. `name` is unique (case-insensitive). The table holds stats, moves, wins/losses, rating, gold, potions, `is_bot`, `created_at` and `last_played`.
- `battles`: one row per battle, with mode, both names, winner, number of turns, XP gained and rating change.

Saves from older versions (the `player_stats` table, which added a new row on every save) are migrated automatically on first launch. The newest row for each hero is kept, and the old table is renamed to `legacy_player_stats`.
