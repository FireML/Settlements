# Settlements

Land protection and group management plugin

## Placeholders

The plugin provides placeholders via PlaceholderAPI with the prefix `settlements_` (and for legacy usage `kingdoms_`) which can be used by other plugins.

It can also integrate with PlaceholderAPI in the chat and the scoreboard if enabled in the config.

### Player Info

General information about the player can be queried with `settlements_player_<var>`.

Settlement or empire info via `settlements_player_settlement_<var>`/`settlements_player_empire_<var>`

| Placeholder                               | Functionality |
|-------------------------------------------|--------------------- |
|`settlements_player_info_name`             | Player name |
|`settlements_player_info_title`            | The full title the player has |
|`settlements_player_info_settlement`       | Name of the player's settlement |
|`settlements_player_info_settlementrank`   | Name of the rank that the player has in the settlement |
|`settlements_player_info_settlementtitle`  | The settlement title the player has selected |
|`settlements_player_info_empire`           | Name of the player's empire |
|`settlements_player_info_empirerank`       | Name of the rank that the player has in the empire |
|`settlements_player_info_empiretitle`      | The empire title the player has selected |
|`settlements_player_info_balance`          | The players economy balance |
|`settlements_player_info_tax`              | The amount of daily tax the player has to pay |
|`settlements_player_info_bonus`            | The amount of bonus claim points the player owns |
|`settlements_player_info_joindate`         | The date the player joined at |
|`settlements_player_info_lastlogin`        | The date of the player's last login |
|`settlements_player_info_activechannel`    | The player's active chat channel |
|`settlements_player_settlement_<var>`      | Information about the player's settlement or empty if not in one. See [Settlement Info](#settlement-info) |
|`settlements_player_empire_<var>`          | Information about the player's empire or empty if not in one. See [Empire Info](#empire-info) |

#### Relation info

Get information about the relation of another player to the viewer.

Relations can be `friendly` (when in same settlement), `ally` (when allied), `empire` (when in same empire) or `other` (if no relation exists).

Colors can be set via `integration.placeholder-api.relation-color` in the `messages.yml`.

| Placeholder                           | Functionality |
|---------------------------------------|--------------------- |
|`rel_settlements_relation`             | For plugins supporting [relational placeholders](https://github.com/PlaceholderAPI/PlaceholderAPI/wiki/PlaceholderExpansion#relational-placeholders) |
|`rel_settlements_relationcolor`        | For plugins supporting relational placeholders |
|`settlements_relation_<player>`        | For plugins not supporting relational placholders (requires including the name of the player that the placeholder is for) |
|`settlements_relationcolor_<player>`   | For plugins not supporting relational placholders (requires including the name of the player that the placeholder is for) |

### Settlement Info

Query information about the player's settlement with `settlements_player_settlement_<var>` or
information about the owner of the land at the player's location with `settlements_landowner_<var>`.

Available variables:

| Variable               | Functionality |
|------------------------|--------------- |
|`name`                  | Settlement name |
|`description`           | Set description of the settlement |
|`empire`                | The empire the settlement is in (or empty) |
|`empirelevel`           | The level of the empire the settlement is in |
|`state`                 | The state (Village or Kingdom) of the settlement |
|`level`                 | The level of the settlement |
|`abandoned`             | Whether or not the the settlement is abandoned |
|`abandonedcountdown`    | How long the settlement will be abandoned for until it gets removed |
|`missionpoints`         | How many empire mission points the settlement has |
|`extramissions`         | How many extra empire missions the settlement can do based on its level |
|`land`                  | The amount of claimed land |
|`maxland`               | The maximum amount of land the settlement can claim |
|`outposts`              | The amount of claimed outposts |
|`maxoutposts`           | The maximum amount of outposts the settlement can claim |
|`balance`               | The approximate balance of the settlement bank, shortened with a suffix |
|`bank`                  | The exact balance of the settlement bank |
|`tax`                   | The per settlement plot tax of the settlement |
|`taxdebtaction`         | The per settlement action if a player can't pay their taxes |
|`joinfee`               | The amount of money to pay to join the settlement |
|`upkeep`                | The amount of money the settlement has to pay each day |
|`allowallyclaim`        | Whether or not allys can claim right next to this settlement's land |
|`hostilespawning`       | Whether or not hostile mobs can spawn on land of this settlement |
|`passivespawning`       | Whether or not passive mobs can spawn on land of this settlement |
|`firespread`            | Whether or not fire can spread on land of this settlement |
|`explosions`            | Whether or not explosions work on land of this settlement |
|`pvp`                   | Whether or not players can damage each other on land of this settlement |
|`buff`                  | Whether or not players will gain purchased effect buffs on land of this settlement |
|`bonusclaims`           | How many extra land claims have been awarded to this settlements |
|`bonusoutposts`         | How many extra outposts have been awarded to this settlements |
|`access`                | Who can join this settlement |
|`perks`                 | The unlocked perks of this settlement |
|`chief`                 | The name of the settlement's chief |
|`chiefonline`           | Whether or not the settlement's chief is online |
|`chiefdisplaytitle`     | The displayed title of the settlement's chief |
|`chiefrank`             | The name of the rank of the settlement's chief |
|`chieftitle`            | The settlement title of the settlement's chief based on the chief rank |
|`residents`             | A list of residents including their ranks and online status |
|`size`                  | The amount of residents in this settlement |
|`resonlinecount`        | Amount of residents online |
|`resotheronlinecount`   | Amount of residents online except the player (only works if querying information about a player's settlement) |
|`officerranks`          | A list of ranks that have officer status |

### Empire Info

Query information about the player's empire with `settlements_player_empire_<var>` or
information about the owner of the land at the player's location with `settlements_landowner_<var>`.

Available variables:

| Variable               | Functionality |
|------------------------|--------------- |
|`name`                  | Settlement name |
|`description`           | Set description of the empire |
|`state`                 | The state tag of the empire |
|`symbol`                | The symbol of the empire as used on the map |
|`level`                 | The level of the empire |
|`abandoned`             | Whether or not the the empire is abandoned |
|`abandonedcountdown`    | How long the empire will be abandoned for until it gets removed |
|`dailymissions`         | How many empire missions are offered to settlements |
|`missions`              | How many empire missions were finished by settlements |
|`balance`               | The approximate balance of the empire bank, shortened with a suffix |
|`bank`                  | The exact balance of the empire bank |
|`tax`                   | The per empire plot tax of the empire |
|`taxdebtaction`         | The per empire action if a player can't pay their taxes |
|`joinfee`               | The amount of money to pay to join the empire |
|`upkeep`                | The amount of money the empire has to pay each day |
|`hostilespawning`       | Whether or not hostile mobs can spawn in the world of this empire |
|`passivespawning`       | Whether or not passive mobs can spawn in the world of this empire |
|`firespread`            | Whether or not fire can spread in the world of this empire |
|`explosions`            | Whether or not explosions work in the world of this empire |
|`pvp`                   | Whether or not players can damage each other in the world of this empire |
|`buff`                  | Whether or not players will gain purchased effect buffs in the world of this empire |
|`bonusclaims`/`bonus`   | How many extra land claims this empire offers to member settlements |
|`bonusoutposts`         | How many extra outposts this empire offers to member settlements |
|`access`                | Who can join this empire |
|`perks`                 | The unlocked perks of this empire |
|`capital`               | The name of the empire's capital |
|`capitalstate`          | The state of the empire's capital |
|`chief`                 | The name of the empire's leader |
|`chiefonline`           | Whether or not the empire's leader is online |
|`chiefdisplaytitle`     | The displayed title of the empire's leader |
|`chiefrank`             | The name of the rank of the empire's leader |
|`chiefsettlementtitle`  | The empire title of the empire's chief based on their settlement rank |
|`chiefempiretitle`      | The empire title of the empire's chief based on the empire leader rank |
|`specialresidents`      | A list of residents with special empire ranks |
|`members`               | A list of member settlements |
|`size`                  | The amount of member settlements in this empire |
|`resonlinecount`        | Amount of residents in member settlements that are online |
|`resotheronlinecount`   | Amount of residents in member settlements that are online except the player (only works if querying information about a player's empire) |
|`officerranks`          | A list of ranks that have officer status |

### Top List

Information about the top 

| Placeholder                    | Functionality |
|--------------------------------|------------------------------------------------------- |
|`top_settlements`               | Display information about the top 10 settlements. Format is defined by the `placeholderapi.top-ten-settlements.entry` message. |
|`top_settlements_<place>`       | The name of the settlement in the place. |
|`top_settlements_<place>_<var>` | Display specific data about a top settlement. See [Player Settlement Info](#settlement-info) for available info variables. |
|`top_villages`                  | Display information about the top 10 villages. Format is defined by the `placeholderapi.top-ten-villages.entry` message. |
|`top_villages_<place>`          | The name of the village in the place. |
|`top_villages_<place>_<var>`    | Display specific data about a top village. See [Player Settlement Info](#settlement-info) for available info variables. |
|`top_kingdoms`                  | Display information about the top 10 kingdoms. Format is defined by the `placeholderapi.top-ten-kingdoms.entry` message. |
|`top_kingdoms_<place>`          | The name of the kingdom in the place. |
|`top_kingdoms_<place>_<var>`    | Display specific data about a top kingdom. See [Player Settlement Info](#settlement-info) for available info variables. |
|`top_empires`                   | Display information about the top 10 empires. Format is defined by the `placeholderapi.top-ten-empires.entry` message. |
|`top_empires_<place>`           | The name of the empire in the place. |
|`top_empires_<place>_<var>`     | Display specific data about a top empire. See [Player Empire Info](#empire-info) for available info variables. |

### Plot Info

Query information about the claimed land at the player's current location with `settlements_land_<var>`/`settlements_plot_<var>` (if a plot).

If there is none then the values will be empty.

Available variables:

| Variable           | Functionality |
|--------------------|--------------- |
|`name`              | The name of this land if set |
|`description`       | The description of this land if set |
|`plotowner`         | The name of the player that owns the land |
|`members`           | Amount of players that are members of this land |
|`state`             | The state of the settlement/empire that owns the land |
|`landowner`         | Name of the owner of this land |
|`x`                 | X value of the chunk's location |
|`z`                 | Z value of the chunk's location |
|`world`             | Name of the land's world |
|`hostilespawning`   | Whether or not hostile mobs can spawn in the world of this empire |
|`passivespawning`   | Whether or not passive mobs can spawn in the world of this empire |
|`firespread`        | Whether or not fire can spread in the world of this empire |
|`explosions`        | Whether or not explosions work in the world of this empire |
|`pvp`               | Whether or not players can damage each other in the world of this empire |
|`buff`              | Whether or not players will gain purchased effect buffs in the world of this empire |
|`build`             | Who is allowed to build on this land |
|`buildsource`       | Where the build state stems from (the plot settings itself or inherited from the landowner) |
|`embassy`           | Whether or not this land is an embassy and can be purchased by players not part of the landowner |
|`tax`               | Amount of extra tax to play when owning this land |
|`price`             | The price to pay when purchasing this land |