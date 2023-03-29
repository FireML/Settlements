package de.themoep.settlements.integration.denizen;

import com.denizenscript.denizen.Denizen;
import com.denizenscript.denizencore.events.ScriptEvent;
import com.denizenscript.denizencore.objects.ObjectFetcher;
import com.denizenscript.denizencore.tags.TagManager;
import de.themoep.settlements.integration.denizen.events.SettlementsChatScriptEvent;
import de.themoep.settlements.integration.denizen.events.SettlementsEmpireScriptEvent;
import de.themoep.settlements.integration.denizen.events.SettlementsSettlementScriptEvent;
import de.themoep.settlements.integration.denizen.objects.EmpireTag;
import de.themoep.settlements.integration.denizen.objects.LandTag;
import de.themoep.settlements.integration.denizen.objects.SettlementTag;

public class DenizenIntegration {

    private final Denizen denizen = Denizen.getInstance();

    public void init() {

        ObjectFetcher.registerWithObjectFetcher(LandTag.class, LandTag.tagProcessor);
        ObjectFetcher.registerWithObjectFetcher(SettlementTag.class, SettlementTag.tagProcessor);
        ObjectFetcher.registerWithObjectFetcher(EmpireTag.class, EmpireTag.tagProcessor);

        ScriptEvent.registerScriptEvent(new SettlementsSettlementScriptEvent());
        ScriptEvent.registerScriptEvent(new SettlementsChatScriptEvent());
        ScriptEvent.registerScriptEvent(new SettlementsEmpireScriptEvent());

        // <--[tag]
        // @attribute <land[world,x,z]>
        // @returns LandTag
        // @plugin Settlements
        // @description
        // Returns the LandTag with the given location.
        // -->
        TagManager.registerTagHandler(LandTag.class, "land", attribute -> {
            if (attribute.hasParam()) {
                return LandTag.valueOf(attribute.getParam(), attribute.context);
            }
            return null;
        });

        // <--[tag]
        // @attribute <settlement[<name>]>
        // @returns SettlementTag
        // @plugin Settlements
        // @description
        // Returns the SettlementTag with the given name.
        // -->
        TagManager.registerTagHandler(SettlementTag.class, "settlement", attribute -> {
            if (attribute.hasParam()) {
                return SettlementTag.valueOf(attribute.getParam(), attribute.context);
            }
            return null;
        });

        // <--[tag]
        // @attribute <empire[<name>]>
        // @returns EmpireTag
        // @plugin Settlements
        // @description
        // Returns the EmpireTag with the given name.
        // -->
        TagManager.registerTagHandler(EmpireTag.class, "empire", attribute -> {
            if (attribute.hasParam()) {
                return EmpireTag.valueOf(attribute.getParam(), attribute.context);
            }
            return null;
        });
    }

    public String getDenizenVersion() {
        return denizen.getDescription().getVersion();
    }

}
