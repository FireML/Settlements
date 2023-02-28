package de.themoep.settlements.integration.denizen;

import com.denizenscript.denizen.Denizen;
import com.denizenscript.denizencore.events.ScriptEvent;
import com.denizenscript.denizencore.objects.ObjectFetcher;
import com.denizenscript.denizencore.tags.TagManager;
import de.themoep.settlements.SettlementsPlugin;
import de.themoep.settlements.integration.denizen.events.SettlementChatScriptEvent;
import de.themoep.settlements.integration.denizen.events.SettlementScriptEvent;
import de.themoep.settlements.integration.denizen.objects.EmpireTag;
import de.themoep.settlements.integration.denizen.objects.LandTag;
import de.themoep.settlements.integration.denizen.objects.ResidentTag;
import de.themoep.settlements.integration.denizen.objects.SettlementTag;
import org.bukkit.event.Listener;

public class DenizenIntegration {

    private final Denizen denizen;

    public DenizenIntegration() {
        denizen = Denizen.getInstance();

        ObjectFetcher.registerWithObjectFetcher(LandTag.class, LandTag.tagProcessor);
        ObjectFetcher.registerWithObjectFetcher(ResidentTag.class, ResidentTag.tagProcessor);
        ObjectFetcher.registerWithObjectFetcher(SettlementTag.class, SettlementTag.tagProcessor);
        ObjectFetcher.registerWithObjectFetcher(EmpireTag.class, EmpireTag.tagProcessor);

        ScriptEvent.registerScriptEvent(new SettlementScriptEvent());
        ScriptEvent.registerScriptEvent(new SettlementChatScriptEvent());

        // <--[tag]
        // @attribute <resident[<name>]>
        // @returns ResidentTag
        // @plugin Settlements
        // @description
        // Returns a ResidentTag object constructed from the input value.
        // Refer to <@link objecttype ResidentTag>.
        // -->
        TagManager.registerTagHandler(ResidentTag.class, "resident", (attribute) -> {
            if (!attribute.hasParam()) {
                attribute.echoError("ResidentTag base must have input.");
                return null;
            }
            return ResidentTag.valueOf(attribute.getParam(), attribute.context);
        });

        // <--[tag]
        // @attribute <settlement[<name>]>
        // @returns SettlementTag
        // @plugin Settlements
        // @description
        // Returns a SettlementTag object constructed from the input value.
        // Refer to <@link objecttype SettlementTag>.
        // -->
        TagManager.registerTagHandler(SettlementTag.class, "settlement", (attribute) -> {
            if (!attribute.hasParam()) {
                attribute.echoError("SettlementTag base must have input.");
                return null;
            }
            return SettlementTag.valueOf(attribute.getParam(), attribute.context);
        });

        // <--[tag]
        // @attribute <empire[<name>]>
        // @returns EmpireTag
        // @plugin Settlements
        // @description
        // Returns a EmpireTag object constructed from the input value.
        // Refer to <@link objecttype EmpireTag>.
        // -->
        TagManager.registerTagHandler(EmpireTag.class, "empire", (attribute) -> {
            if (!attribute.hasParam()) {
                attribute.echoError("EmpireTag base must have input.");
                return null;
            }
            return EmpireTag.valueOf(attribute.getParam(), attribute.context);
        });

        // <--[tag]
        // @attribute <land[<world,x,z>]>
        // @returns LandTag
        // @plugin Settlements
        // @description
        // Returns a LandTag object constructed from the input value.
        // Refer to <@link objecttype LandTag>.
        // -->
        TagManager.registerTagHandler(LandTag.class, "land", (attribute) -> {
            if (!attribute.hasParam()) {
                attribute.echoError("LandTag base must have input.");
                return null;
            }
            return LandTag.valueOf(attribute.getParam(), attribute.context);
        });
    }

    public String getDenizenVersion() {
        return denizen.getDescription().getVersion();
    }

}
