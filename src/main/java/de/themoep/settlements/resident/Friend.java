package de.themoep.settlements.resident;

import de.themoep.settlements.SettlementsPlugin;
import de.themoep.settlements.utils.ConfigMessages;
import de.themoep.settlements.utils.ItemUtils;
import de.themoep.settlements.utils.Sortable;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.Comparator;
import java.util.Map;
import java.util.UUID;

public class Friend implements Sortable<Friend> {

    private final String playerName;
    private final UUID playerId;

    public Friend(Resident sender) {
        playerId = sender.getUUID();
        playerName = sender.getName();
    }

    public Friend(UUID playerId, String playerName) {
        this.playerId = playerId;
        this.playerName = playerName;
    }

    public Player getPlayer() {
        return SettlementsPlugin.getInstance().getServer().getPlayer(playerId);
    }

    public Resident getResident() {
        return ResidentManager.getInstance().getResident(playerId);
    }

    @Override
    public String name() {
        return "friend";
    }

    public static Friend deserialize(Map<String, Object> map) {
        UUID id;
        if (map.get("friendId") instanceof UUID) {
            id = (UUID) map.get("friendId");
        } else if (map.get("friendId") instanceof String) {
            id = UUID.fromString((String) map.get("friendId"));
        } else {
            id = null;
        }
        String name = (String) map.get("friendName");

        return new Friend(id, name);
    }


    @Override
    public String[] getReplacements(ConfigMessages configMessages, String... initial) {
        return getResident().getReplacements(
                "online", configMessages.getText(getPlayer() != null ? "online" : "offline")
        );
    }

    @Override
    public Sortable.Sort<Friend>[] getSorts() {
        return Sort.values();
    }

    public enum Sort implements Sortable.Sort<Friend> {
        NAME((e1, e2) -> e1.getPlayerName().compareToIgnoreCase(e2.getPlayerName())),
        ;

        private final Comparator<Friend> comparator;

        Sort(Comparator<Friend> comparator) {
            this.comparator = comparator;
        }

        @Override
        public Sortable.Sort<Friend>[] getValues() {
            return values();
        }

        @Override
        public Comparator<Friend> getComparator() {
            return comparator;
        }
    }

    @Override
    public ItemStack getIcon() {
        return ItemUtils.getHead(playerId, playerName);
    }

    public UUID getPlayerId() {
        return playerId;
    }

    public String getPlayerName() {
        return playerName;
    }

}
