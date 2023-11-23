package de.themoep.settlements.land.gui;

import de.themoep.settlements.empire.EmpireLevel;
import de.themoep.settlements.land.LandOwner;
import de.themoep.settlements.land.LandownerLevel;
import de.themoep.settlements.leveling.LevelInfo;
import de.themoep.settlements.leveling.Levelable;
import de.themoep.settlements.npc.NpcData;
import de.themoep.settlements.npc.NpcLevel;
import de.themoep.settlements.npc.NpcType;
import de.themoep.settlements.perk.LevelPerk;
import de.themoep.settlements.resident.Resident;
import de.themoep.settlements.settlement.SettlementLevel;
import de.themoep.settlements.utils.ConfigurableGui;
import de.themoep.settlements.utils.HumanReadable;
import de.themoep.settlements.utils.Message;
import org.bukkit.Material;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;
import uk.firedev.poleislib.libs.themoep.inventorygui.DynamicGuiElement;
import uk.firedev.poleislib.libs.themoep.inventorygui.GuiBackElement;
import uk.firedev.poleislib.libs.themoep.inventorygui.GuiElementGroup;
import uk.firedev.poleislib.libs.themoep.inventorygui.StaticGuiElement;
import uk.firedev.poleislib.utils.ItemUtils;

import java.util.*;

public class UpgradeGUI extends ConfigurableGui {

    private Levelable<? extends LevelInfo> selected = null;

    public UpgradeGUI(Resident resident, LandOwner<?, ? extends LandownerLevel> landOwner) {
        super(resident, landOwner.getKey() + ".upgrade", "state", landOwner.getStateTag());

        List<Levelable<? extends LevelInfo>> levelables = new ArrayList<>();
        levelables.add(landOwner);

        NpcType.AppliesTo landOwnerType = NpcType.AppliesTo.fromKey(landOwner.getKey());
        for (NpcType npcType : NpcType.values()) {
            if (npcType.appliesTo(landOwnerType)) {
                NpcData data = landOwner.getNpcData(npcType);
                if (data != null && data.isAvailable() && data.hasLevels()) {
                    levelables.add(data);
                }
            }
        }

        for (Levelable<? extends LevelInfo> levelable : levelables) {
            if (!levelable.isMaxLevel()) {
                selected = levelable;
                break;
            }
        }

        if (selected == null) {
            this.gui = null;
            Message.get("command.settlement.upgrade.not-possible").tell(resident, "reason", Message.get("command.settlement.upgrade.max-level-reached").getText());
            return;
        }

        GuiElementGroup navBar = new GuiElementGroup('l');

        for (Levelable<?> levelable : levelables) {
            navBar.addElement(new DynamicGuiElement('l', who -> new StaticGuiElement('l',
                    ItemUtils.setGlowing(getIcon(levelable), levelable == selected),
                    click -> {
                        if (levelable.getLevel() > 0 && selected != levelable && !levelable.isMaxLevel()) {
                            selected = levelable;
                            click.getGui().draw();
                        }
                        return true;
                    }, getText("navigation.entry",
                                "description", getText("navigation.description." + (levelable.isMaxLevel() ? "upgraded" : levelable.getLevel() > 0 ? "upgradable" : "locked"),
                                        "action", getText("navigation.action." + (levelable == selected ? "current" : "switch"))),
                                "name", levelable instanceof LandOwner
                                        ? ((LandOwner<?, ?>) levelable).getStateTag() + " " + ((LandOwner<?, ?>) levelable).getName()
                                        : levelable.getTag(),
                                "type", levelable instanceof NpcData ? "npc" : levelable.getTag(),
                                "level", String.valueOf(levelable.getLevel())
            ))));
        }

        gui.addElement(navBar);

        gui.addElement(new DynamicGuiElement('r', who -> {
            GuiElementGroup reqGroup = new GuiElementGroup('r');

            if (!selected.isMaxLevel()) {
                for (Map.Entry<String, Long> entry : selected.getNextLevelRequirements().entrySet()) {
                    ItemStack icon;
                    if (entry.getKey().equalsIgnoreCase("level")) {
                        icon = getIcon(landOwner);
                    } else {
                        icon = ItemUtils.toIcon(Material.matchMaterial(entry.getKey()));
                    }

                    if (icon == null) {
                        icon = getIcon("requirement." + entry.getKey().toLowerCase(Locale.ROOT));
                    }

                    boolean hasReached = selected.has(entry.getKey(), entry.getValue());

                    reqGroup.addElement(new StaticGuiElement('r', ItemUtils.setGlowing(icon, hasReached), getText("requirement." + (hasReached ? "reached" : "left"),
                            "name", HumanReadable.humanizedName(entry.getKey()),
                            "required", String.valueOf(entry.getValue()),
                            "progress", String.valueOf(selected.getAmount(entry.getKey())),
                            "amount", String.valueOf(entry.getValue() - selected.getAmount(entry.getKey()))
                    )));
                }
            }

            return reqGroup;
        }));

        gui.addElement(new DynamicGuiElement('i', who -> {
            LevelInfo level = selected.getLevelInfo();
            LevelInfo nextLevel = selected.getNextLevel().orElse(null);
            if (nextLevel != null) {
                Collection<? extends LevelPerk> perks = selected.getPerks(nextLevel.getLevel());
                List<String> benefits = new ArrayList<>();
                if (nextLevel instanceof NpcLevel && ((NpcLevel) nextLevel).getUpkeep() > 0) {
                    benefits.add("NPC Upkeep: " + ((NpcLevel) nextLevel).getUpkeep());
                }
                if (nextLevel instanceof LandownerLevel && level instanceof  LandownerLevel) {
                    if (((LandownerLevel) nextLevel).getSize() > ((LandownerLevel) level).getSize()) {
                        benefits.add(((LandownerLevel) nextLevel).getSize() + " Members");
                    }
                    if (((LandownerLevel) nextLevel).getMissions() > ((LandownerLevel) level).getMissions()) {
                        benefits.add(((LandownerLevel) nextLevel).getMissions() + " Missions");
                    }
                    if (((LandownerLevel) nextLevel).getChunks() > ((LandownerLevel) level).getChunks()) {
                        benefits.add(((LandownerLevel) nextLevel).getChunks() + " Claims");
                    }
                    if (((LandownerLevel) nextLevel).getClaimUpkeep(who.getWorld().getName()) > ((LandownerLevel) level).getClaimUpkeep(who.getWorld().getName())) {
                        benefits.add(((LandownerLevel) nextLevel).getClaimUpkeep(who.getWorld().getName()) + " Claim Upkeep");
                    }
                    if (nextLevel instanceof SettlementLevel && level instanceof SettlementLevel) {
                        if (((SettlementLevel) nextLevel).getExtraMissions() > ((SettlementLevel) level).getExtraMissions()) {
                            benefits.add(((SettlementLevel) nextLevel).getExtraMissions() + " Extra Missions");
                        }
                        if (((SettlementLevel) nextLevel).getOutposts() > ((SettlementLevel) level).getOutposts()) {
                            benefits.add(((SettlementLevel) nextLevel).getOutposts() + " Outposts");
                        }
                    }
                    if (nextLevel instanceof EmpireLevel && level instanceof EmpireLevel) {
                        if (((EmpireLevel) nextLevel).getWorldRadius() > ((EmpireLevel) level).getWorldRadius()) {
                            benefits.add(((EmpireLevel) nextLevel).getWorldRadius() + " World Radius");
                        }
                        if (((EmpireLevel) nextLevel).getUpkeep() > ((EmpireLevel) level).getUpkeep()) {
                            benefits.add(((EmpireLevel) nextLevel).getUpkeep() + " Upkeep");
                        }
                        if (((EmpireLevel) nextLevel).getUpkeepReduction() > ((EmpireLevel) level).getUpkeepReduction()) {
                            benefits.add(((EmpireLevel) nextLevel).getUpkeepReduction() + " Upkeep Reduction");
                        }
                    }
                    if (!((LandownerLevel) nextLevel).getNPCs().isEmpty()) {
                        for (NpcType npcType : ((LandownerLevel) nextLevel).getNPCs()) {
                            benefits.add(npcType.getTag() + " NPC");
                        }
                    }
                }
                List<String> benefitTexts = new ArrayList<>();
                for (LevelPerk perk : perks) {
                    if (perk.getUnlockables().isEmpty() || getMessage("description." + perk.getKey()).exists()) {
                        benefitTexts.add(getText("benefit",
                                "name", perk.getTag(),
                                "description", getText("description." + perk.getKey())));
                    } else {
                        for (String unlockable : perk.getUnlockables()) {
                            benefitTexts.add(getText("benefit",
                                    "name", HumanReadable.humanizedName(unlockable),
                                    "description", HumanReadable.humanizedName(unlockable)));
                        }
                    }
                }
                for (String benefit : benefits) {
                    benefitTexts.add(getText("benefit",
                            "name", benefit,
                            "description", benefit
                    ));
                }
                return new StaticGuiElement('i', getIcon(selected), getText("info",
                        "name", selected.getTag(),
                        "benefits", String.join("\n", benefitTexts)
                ));
            }
            return null;
        }));

        gui.addElement(new DynamicGuiElement('p', who -> {
            LevelInfo nextLevel = selected.getNextLevel().orElse(null);
            if (nextLevel != null) {
                double progress = selected.getNextLevelProgress();
                return new StaticGuiElement('p', getIcon("progress"), getText("progress",
                        "progress", String.valueOf(((int) progress * 1000) / 10d),
                        "progressbar", getText("progress-bar.finished",
                                "bars", String.join("", Collections.nCopies((int) Math.floor(progress * 50), "|"))) +
                                getText("progress-bar.unfinished",
                                        "bars", String.join("", Collections.nCopies((int) Math.ceil((1 - progress) * 50), "|")))
                        )
                );
            }
            return null;
        }));

        gui.addElement(new DynamicGuiElement('d', who -> new StaticGuiElement('d', getIcon("deposit"), click -> {
            if (click.getCursor() == null || click.getCursor().getType().isEmpty()) {
                new DepositGUI<>(landOwner.getKey(), resident, selected).open();
            } else if (click.getType() == ClickType.LEFT && click.getCursor() != null) {
                Map<String, Long> requirements = selected.getNextLevelRequirements();
                ItemStack cursor = click.getCursor();
                String type = cursor.getType().name();
                if (requirements.containsKey(type)) {
                    long required = requirements.get(type);
                    long amount = selected.getAmount(type);
                    if (amount < required) {
                        int cursorAmount = cursor.getAmount();
                        int added;
                        if (cursorAmount <= required - amount) {
                            added = cursorAmount;
                        } else {
                            added = (int) (required - amount);
                        }
                        cursor.setAmount(cursor.getAmount() - added);
                        if (cursor.getAmount() <= 0) {
                            click.setCursor(null);
                        }
                        selected.add(type, added);
                        if (!selected.checkLevelUp(resident)) {
                            selected.save();
                        }
                        click.getGui().draw();
                    }
                }
            }
            return true;
        }, getText("deposit", "name", selected.getTag()))));

        gui.addElement(new GuiBackElement('b', getIcon("back"), true, getText("back")));
    }
}
