package de.themoep.settlements.integration.denizen.properties;

import com.denizenscript.denizen.objects.PlayerTag;
import com.denizenscript.denizencore.objects.ObjectTag;
import com.denizenscript.denizencore.objects.properties.Property;
import com.denizenscript.denizencore.objects.properties.PropertyParser;
import de.themoep.settlements.integration.denizen.objects.ResidentTag;
import de.themoep.settlements.resident.Resident;
import de.themoep.settlements.resident.ResidentManager;
import de.themoep.settlements.utils.Utils;
import org.bukkit.OfflinePlayer;

import java.util.concurrent.atomic.AtomicReference;

public class SettlementsPlayerProperties implements Property {

    @Override
    public String getPropertyString() {
        return null;
    }

    @Override
    public String getPropertyId() {
        return "Resident";
    }

    public static boolean describes(ObjectTag object) {
        return object instanceof PlayerTag;
    }

    public static SettlementsPlayerProperties getFrom(ObjectTag object) {
        if (!describes(object)) {
            return null;
        }
        else {
            return new SettlementsPlayerProperties((PlayerTag) object);
        }
    }

    public static final String[] handledTags = new String[] {
            "resident"
    };

    public static final String[] handledMechs = new String[] {
    };

    public SettlementsPlayerProperties(PlayerTag player) {
        this.resident = getResident(player.getOfflinePlayer());
    }

    Resident resident;

    public static void register() {

        // <--[tag]
        // @attribute <PlayerTag.resident>
        // @returns LocationTag
        // @plugin Settlements
        // @description
        // Returns the Player's ResidentTag
        // -->
        PropertyParser.registerTag(SettlementsPlayerProperties.class, ResidentTag.class, "resident", (attribute, object) -> new ResidentTag(object.resident));
    }

    private Resident getResident(OfflinePlayer player) {
        for (Resident res : ResidentManager.getInstance().getCachedResidents()) {
            if (res.getPlayer() == player) {
                return res;
            }
        }
        return null;
    }
}