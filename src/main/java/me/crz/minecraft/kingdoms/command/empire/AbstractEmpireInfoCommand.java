package me.crz.minecraft.kingdoms.command.empire;

import me.crz.minecraft.kingdoms.command.SubCommand;
import me.crz.minecraft.kingdoms.empire.Empire;
import me.crz.minecraft.kingdoms.rank.VillageRank;
import me.crz.minecraft.kingdoms.utils.NameTag;
import me.crz.minecraft.kingdoms.village.Resident;
import me.crz.minecraft.kingdoms.village.Village;

import java.text.DecimalFormat;
import java.util.stream.Collectors;

import static me.crz.minecraft.kingdoms.utils.MessageUtils.yesOrNo;

public abstract class AbstractEmpireInfoCommand extends SubCommand {
    private final static DecimalFormat DECIMAL_FORMAT = new DecimalFormat("#,###");

    public AbstractEmpireInfoCommand(SubCommand parent, String usage, String... aliases) {
        super(parent, usage, aliases);
    }

    protected String[] getReplacements(Empire empire) {
        Village capital = empire.getCapital();
        Resident chief = capital.getChief();
        return new String[] {
                "name", empire.getName(),
                "description", empire.getDescription(),
                "level", String.valueOf(empire.getLevel()),
                "pvp", yesOrNo(empire.isPvp()),
                "balance", DECIMAL_FORMAT.format(empire.getBank()),
                "joinfee", DECIMAL_FORMAT.format(empire.getBank()),
                "tax", DECIMAL_FORMAT.format(empire.getBank()),
                "mobspawning", yesOrNo(empire.isMobSpawning()),
                "firespread", yesOrNo(empire.isFireSpread()),
                "explosions", yesOrNo(empire.isExplosions()),
                "buff", yesOrNo(empire.isBuff()),
                "bonusclaims", empire.getBonusClaims() > 0 ? getMessage("bonus-claims").getReplacedText("amount", String.valueOf(empire.getBonusClaims())) : "",
                "capital", capital.getName(),
                "capitalstate", capital.getState().getTag(),
                "perks", empire.getPerks().stream().map(NameTag::getTag).collect(Collectors.joining(", ")),
                "chiefrank", VillageRank.CHIEF.getTag(capital),
                "chieftitle", chief.getTitle(),
                "chief", chief.getName(),
                "members", empire.getMembers().stream().map(v -> v.getState().getTag() + " " + v.getName()).collect(Collectors.joining(", "))
        };
    }
}
