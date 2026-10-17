# Settlements

Land protection and group management plugin

## Placeholders

The plugin provides placeholders via PlaceholderAPI with the prefix `settlements_` (and for legacy usage `kingdoms_`) which can be used by other plugins.

It can also integrate with PlaceholderAPI in the chat and the scoreboard if enabled in the config.

### Player Info

General information about the player can be queried with `settlements_player_<var>`.

Settlement info via `settlements_player_settlement_<var>`

| Placeholder                               | Functionality                                                                                             |
|-------------------------------------------|-----------------------------------------------------------------------------------------------------------|
| `settlements_player_info_name`            | Player name                                                                                               |
| `settlements_player_info_title`           | The full title the player has                                                                             |
| `settlements_player_info_settlement`      | Name of the player's settlement                                                                           |
| `settlements_player_info_settlementrank`  | Name of the rank that the player has in the settlement                                                    |
| `settlements_player_info_settlementtitle` | The settlement title the player has selected                                                              |
| `settlements_player_info_balance`         | The players economy balance                                                                               |
| `settlements_player_info_tax`             | The amount of daily tax the player has to pay                                                             |
| `settlements_player_info_bonus`           | The amount of bonus claim points the player owns                                                          |
| `settlements_player_info_joindate`        | The date the player joined at                                                                             |
| `settlements_player_info_lastlogin`       | The date of the player's last login                                                                       |
| `settlements_player_info_activechannel`   | The player's active chat channel                                                                          |
| `settlements_player_settlement_<var>`     | Information about the player's settlement or empty if not in one. See [Settlement Info](#settlement-info) |

#### Relation info

Get information about the relation of another player to the viewer.

Relations can be `friendly` (when in same settlement), `ally` (when allied), or `other` (if no relation exists).

Colors can be set via `integration.placeholder-api.relation-color` in the `messages.yml`.

| Placeholder                          | Functionality                                                                                                                                        |
|--------------------------------------|------------------------------------------------------------------------------------------------------------------------------------------------------|
| `rel_settlements_relation`           | For plugins supporting [relational placeholders](https://github.com/PlaceholderAPI/PlaceholderAPI/wiki/PlaceholderExpansion#relational-placeholders) |
| `rel_settlements_relationcolor`      | For plugins supporting relational placeholders                                                                                                       |
| `settlements_relation_<player>`      | For plugins not supporting relational placholders (requires including the name of the player that the placeholder is for)                            |
| `settlements_relationcolor_<player>` | For plugins not supporting relational placholders (requires including the name of the player that the placeholder is for)                            |

### Settlement Info

Query information about the player's settlement with `settlements_player_settlement_<var>` or
information about the owner of the land at the player's location with `settlements_landowner_<var>`.

Available variables:

| Variable              | Functionality                                                                                                 |
|-----------------------|---------------------------------------------------------------------------------------------------------------|
| `name`                | Settlement name                                                                                               |
| `description`         | Set description of the settlement                                                                             |
| `state`               | The state (Village or Kingdom) of the settlement                                                              |
| `level`               | The level of the settlement                                                                                   |
| `abandoned`           | Whether or not the the settlement is abandoned                                                                |
| `abandonedcountdown`  | How long the settlement will be abandoned for until it gets removed                                           |
| `land`                | The amount of claimed land                                                                                    |
| `maxland`             | The maximum amount of land the settlement can claim                                                           |
| `outposts`            | The amount of claimed outposts                                                                                |
| `maxoutposts`         | The maximum amount of outposts the settlement can claim                                                       |
| `balance`             | The approximate balance of the settlement bank, shortened with a suffix                                       |
| `bank`                | The exact balance of the settlement bank                                                                      |
| `tax`                 | The per settlement plot tax of the settlement                                                                 |
| `taxdebtaction`       | The per settlement action if a player can't pay their taxes                                                   |
| `joinfee`             | The amount of money to pay to join the settlement                                                             |
| `upkeep`              | The amount of money the settlement has to pay each day                                                        |
| `allowallyclaim`      | Whether or not allys can claim right next to this settlement's land                                           |
| `hostilespawning`     | Whether or not hostile mobs can spawn on land of this settlement                                              |
| `passivespawning`     | Whether or not passive mobs can spawn on land of this settlement                                              |
| `firespread`          | Whether or not fire can spread on land of this settlement                                                     |
| `explosions`          | Whether or not explosions work on land of this settlement                                                     |
| `pvp`                 | Whether or not players can damage each other on land of this settlement                                       |
| `buff`                | Whether or not players will gain purchased effect buffs on land of this settlement                            |
| `bonusclaims`         | How many extra land claims have been awarded to this settlements                                              |
| `bonusoutposts`       | How many extra outposts have been awarded to this settlements                                                 |
| `access`              | Who can join this settlement                                                                                  |
| `perks`               | The unlocked perks of this settlement                                                                         |
| `chief`               | The name of the settlement's chief                                                                            |
| `chiefonline`         | Whether or not the settlement's chief is online                                                               |
| `chiefdisplaytitle`   | The displayed title of the settlement's chief                                                                 |
| `chiefrank`           | The name of the rank of the settlement's chief                                                                |
| `chieftitle`          | The settlement title of the settlement's chief based on the chief rank                                        |
| `residents`           | A list of residents including their ranks and online status                                                   |
| `size`                | The amount of residents in this settlement                                                                    |
| `resonlinecount`      | Amount of residents online                                                                                    |
| `resotheronlinecount` | Amount of residents online except the player (only works if querying information about a player's settlement) |
| `officerranks`        | A list of ranks that have officer status                                                                      |

### Top List

Information about the top 

| Placeholder                     | Functionality                                                                                                                  |
|---------------------------------|--------------------------------------------------------------------------------------------------------------------------------|
| `top_settlements`               | Display information about the top 10 settlements. Format is defined by the `placeholderapi.top-ten-settlements.entry` message. |
| `top_settlements_<place>`       | The name of the settlement in the place.                                                                                       |
| `top_settlements_<place>_<var>` | Display specific data about a top settlement. See [Player Settlement Info](#settlement-info) for available info variables.     |
| `top_villages`                  | Display information about the top 10 villages. Format is defined by the `placeholderapi.top-ten-villages.entry` message.       |
| `top_villages_<place>`          | The name of the village in the place.                                                                                          |
| `top_villages_<place>_<var>`    | Display specific data about a top village. See [Player Settlement Info](#settlement-info) for available info variables.        |
| `top_kingdoms`                  | Display information about the top 10 kingdoms. Format is defined by the `placeholderapi.top-ten-kingdoms.entry` message.       |
| `top_kingdoms_<place>`          | The name of the kingdom in the place.                                                                                          |
| `top_kingdoms_<place>_<var>`    | Display specific data about a top kingdom. See [Player Settlement Info](#settlement-info) for available info variables.        |

### Plot Info

Query information about the claimed land at the player's current location with `settlements_land_<var>`/`settlements_plot_<var>` (if a plot).

If there is none then the values will be empty.

Available variables:

| Variable          | Functionality                                                                                    |
|-------------------|--------------------------------------------------------------------------------------------------|
| `name`            | The name of this land if set                                                                     |
| `description`     | The description of this land if set                                                              |
| `plotowner`       | The name of the player that owns the land                                                        |
| `members`         | Amount of players that are members of this land                                                  |
| `state`           | The state of the settlement that owns the land                                                   |
| `landowner`       | Name of the owner of this land                                                                   |
| `x`               | X value of the chunk's location                                                                  |
| `z`               | Z value of the chunk's location                                                                  |
| `world`           | Name of the land's world                                                                         |
| `hostilespawning` | Whether or not hostile mobs can spawn in this plot                                               |
| `passivespawning` | Whether or not passive mobs can spawn in this plot                                               |
| `firespread`      | Whether or not fire can spread in this plot                                                      |
| `explosions`      | Whether or not explosions work in this plot                                                      |
| `pvp`             | Whether or not players can damage each other in this plot                                        |
| `buff`            | Whether or not players will gain purchased effect buffs in this plot                             |
| `build`           | Who is allowed to build on this land                                                             |
| `buildsource`     | Where the build state stems from (the plot settings itself or inherited from the landowner)      |
| `embassy`         | Whether or not this land is an embassy and can be purchased by players not part of the landowner |
| `tax`             | Amount of extra tax to play when owning this land                                                |
| `price`           | The price to pay when purchasing this land                                                       |