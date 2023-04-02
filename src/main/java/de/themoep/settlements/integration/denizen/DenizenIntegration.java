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
import de.themoep.settlements.integration.denizen.objects.ResidentTag;
import de.themoep.settlements.integration.denizen.objects.SettlementTag;

public class DenizenIntegration {

    private final Denizen denizen = Denizen.getInstance();

    public void init() {

        ObjectFetcher.registerWithObjectFetcher(LandTag.class, LandTag.tagProcessor);
        ObjectFetcher.registerWithObjectFetcher(SettlementTag.class, SettlementTag.tagProcessor);
        SettlementTag.register();
        ObjectFetcher.registerWithObjectFetcher(EmpireTag.class, EmpireTag.tagProcessor);
        EmpireTag.register();
        ObjectFetcher.registerWithObjectFetcher(ResidentTag.class, ResidentTag.tagProcessor);
        ResidentTag.register();

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
        TagManager.registerTagHandler(LandTag.class, "land", (attribute) -> LandTag.valueOf(attribute.getParam(), attribute.context));

        // <--[tag]
        // @attribute <settlement[<name>]>
        // @returns SettlementTag
        // @plugin Settlements
        // @description
        // Returns the SettlementTag with the given name.
        // -->
        TagManager.registerTagHandler(SettlementTag.class, "settlement", (attribute) -> SettlementTag.valueOf(attribute.getParam(), attribute.context));

        // <--[tag]
        // @attribute <empire[<name>]>
        // @returns EmpireTag
        // @plugin Settlements
        // @description
        // Returns the EmpireTag with the given name.
        // -->
        TagManager.registerTagHandler(EmpireTag.class, "empire", (attribute) -> EmpireTag.valueOf(attribute.getParam(), attribute.context));

        // <--[tag]
        // @attribute <resident[<name>]>
        // @returns ResidentTag
        // @plugin Settlements
        // @description
        // Returns the ResidentTag with the given name.
        // -->
        TagManager.registerTagHandler(ResidentTag.class, "resident", (attribute) -> ResidentTag.valueOf(attribute.getParam(), attribute.context));
    }

    public String getDenizenVersion() {
        return denizen.getDescription().getVersion();
    }

}
