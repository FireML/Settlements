package de.themoep.settlements.settlement.gui;

import de.themoep.inventorygui.GuiBackElement;
import de.themoep.inventorygui.InventoryGui;
import de.themoep.inventorygui.StaticGuiElement;
import de.themoep.settlements.SettlementsGUI;
import de.themoep.settlements.SettlementsPlugin;
import de.themoep.settlements.integration.MailProvider;
import de.themoep.settlements.land.gui.AccessGUI;
import de.themoep.settlements.land.gui.RecruitmentGUI;
import de.themoep.settlements.resident.Resident;
import de.themoep.settlements.resident.ResidentManager;
import de.themoep.settlements.settlement.Settlement;
import de.themoep.settlements.settlement.SettlementManager;
import de.themoep.settlements.utils.ConfigurableGui;
import de.themoep.settlements.utils.Message;
import de.themoep.settlements.utils.MessageUtils;
import de.themoep.settlements.utils.SignGui;
import de.themoep.settlements.utils.Utils;
import de.themoep.settlements.utils.config.Config;
import net.wesjd.anvilgui.AnvilGUI;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class SettingsGUI extends ConfigurableGui {
    private final Pattern namePattern;

    public SettingsGUI(Player player) {
        this(ResidentManager.getInstance().getResident(player));
    }

    public SettingsGUI(Resident resident) {
        super(resident, "settlement.settings");
        namePattern = Pattern.compile(Config.Settlement.NAME_VALIDATION);

        gui.addElement(new StaticGuiElement('n', getIcon("rename"), click -> {
            if (resident.hasPermission("settlement.rename")) {
                Settlement settlement = resident.getSettlement();
                new AnvilGUI.Builder()
                        .itemLeft(getIcon("rename"))
                        .title(getText("rename.anvil.title", "name", settlement.getName()))
                        .text(getText("rename.anvil.placeholder", "name", settlement.getName()))
                        .onClose(player -> Utils.runLater(() -> new SettingsGUI(resident).open(), 1))
                        .onComplete((player, text) -> {
                            if (settlement.hasPermission(resident, "rename")) {
                                String name = Utils.filterAllCurses(text.trim());
                                if (name.toLowerCase().contains("claims")) {
                                    getError("invalid-characters").tell(player, "input", name);
                                    return AnvilGUI.Response.text(getError("invalid-characters").getReplacedText("input", name));
                                } else if (SettlementManager.getInstance().getFromName(name) != null) {
                                    getError("settlement-exists").tell(player, "input", name);
                                    return AnvilGUI.Response.text(getError("settlement-exists").getReplacedText("name", name));
                                } else if (name.length() > Config.Settlement.NAME_LENGTH) {
                                    getError("too-long").tell(player, "input", name, "amount", String.valueOf(Config.Settlement.NAME_LENGTH));
                                    return AnvilGUI.Response.text(getError("too-long").getReplacedText("input", name, "amount", String.valueOf(Config.Settlement.NAME_LENGTH)));
                                } else if (!namePattern.matcher(name).matches()) {
                                    getError("invalid-characters").tell(player, "input", name);
                                    return AnvilGUI.Response.text(getError("invalid-characters").getReplacedText("input", name));
                                } else {
                                    SettlementManager.getInstance().changeName(settlement, name);
                                    Message.get("command.settlement.rename.success").tell(settlement.getOnlineResidents(), "name", settlement.getName(), "state", settlement.getState().getTag());
                                    Utils.runLater(() -> new SettingsGUI(player), 1);
                                }
                            } else {
                                getError("no-permission").tell(player);
                            }
                            return AnvilGUI.Response.close();
                        })
                        .plugin(SettlementsPlugin.getInstance())
                        .open(resident.getPlayer());
            }
            return true;
        }, getText("rename.entry",
                "state", resident.getSettlement().getStateTag(),
                "name", resident.getSettlement().getName(),
                "action", resident.hasPermission("settlement.rename") ? getText("unlockables.unlocked.change") : getText("no-permission")
        )));

        gui.addElement(new StaticGuiElement('p', getIcon("perks"), click -> {
            if (resident.hasPermission("settlement.perks")) {
                new PerksGUI(resident).open();
            }
            return true;
        }, getText("perks.entry",
                "state", resident.getSettlement().getStateTag(),
                "perks", resident.getSettlement().getPerks().stream().map(p -> getText("perks.list", "name", p.getTag())).collect(Collectors.joining("\n")),
                "action", resident.hasPermission("settlement.perks") ? getText("unlockables.unlocked.options") : getText("no-permission")
        )));

        gui.addElement(new StaticGuiElement('r', getIcon("rules"), click -> {
            if (!resident.hasPermission("settlement.setrules")) {
                return true;
            }
            if (click.getType() == ClickType.LEFT) {
                ItemStack cursor = click.getEvent().getCursor();
                if (cursor != null) {
                    ItemMeta meta = cursor.getItemMeta();
                    if (meta instanceof BookMeta) {
                        String rules = String.join("\n", ((BookMeta) meta).getPages());
                        resident.getSettlement().setRules(rules);
                        resident.getSettlement().save();
                        getMessage("rules.updated").tell(resident.getSettlement().getOnlineResidents(), "state", resident.getSettlement().getStateTag());
                        for (MailProvider provider : SettlementsPlugin.getInstance().getMailProviders()) {
                            for (Resident r : resident.getSettlement().getResidents()) {
                                provider.sendMail(resident.getName(), r, getText("mail",
                                        "state", resident.getSettlement().getStateTag(),
                                        "date", Utils.DATE_FORMAT.format(System.currentTimeMillis())
                                ));
                            }
                        }
                        return true;
                    }
                }
            }
            getMessage("rules.info,actionbar").actionbar(resident);
            new SignGui(text -> {
                if (resident.hasPermission("settlement.setrules")) {
                    String rules = String.join("\n", text).trim();
                    if (!rules.isEmpty() && !rules.equals(resident.getSettlement().getRules())) {
                        resident.getSettlement().setRules(rules);
                        resident.getSettlement().save();
                        getMessage("rules.updated").tell(resident.getSettlement().getOnlineResidents(), "state", resident.getSettlement().getStateTag());
                        for (MailProvider provider : SettlementsPlugin.getInstance().getMailProviders()) {
                            for (Resident r : resident.getSettlement().getResidents()) {
                                provider.sendMail(resident.getName(), r, getText("mail",
                                        "state", resident.getSettlement().getStateTag(),
                                        "date", Utils.DATE_FORMAT.format(System.currentTimeMillis())
                                ));
                            }
                        }
                    }
                }
                open();
            }).open((Player) click.getWhoClicked());
            return true;
        }, getText("rules.entry", "state", resident.getSettlement().getStateTag(),
                "action", resident.hasPermission("settlement.setrules") ? getText("unlockables.unlocked.change") : getText("no-permission"))
        ));

        gui.addElement(new StaticGuiElement('d', getIcon("discord"), click -> {
            if (resident.hasPermission("settlement.discord.set")) {
                Settlement settlement = resident.getSettlement();
                new AnvilGUI.Builder()
                        .itemLeft(getIcon("discord"))
                        .title(getText("discord.anvil.title", "name", settlement.getName(), "discord", settlement.getDiscord()))
                        .text(getText("discord.anvil.placeholder", "discord", settlement.getDiscord()))
                        .onClose(player -> Utils.runLater(() -> new SettingsGUI(resident).open(), 1))
                        .onComplete((player, text) -> {
                            if (settlement.hasPermission(resident, "discord.set")) {
                                String link = text.trim();
                                String oldLink = settlement.getDiscord();
                                settlement.setDiscord(link);
                                if (text.isEmpty()) {
                                    Message.get("command.settlement.discord.set.removed").tell(resident,
                                            "state", settlement.getStateTag(),
                                            "discordlink", oldLink
                                    );
                                } else {
                                    Message.get("command.settlement.discord.set.success").tell(resident,
                                            "state", settlement.getStateTag(),
                                            "discordlink", settlement.getDiscord()
                                    );
                                }
                                settlement.save();
                                Utils.runLater(() -> new SettingsGUI(resident).open(), 1);
                            } else {
                                getError("no-permission").tell(player);
                            }
                            return AnvilGUI.Response.close();
                        })
                        .plugin(SettlementsPlugin.getInstance())
                        .open(resident.getPlayer());
            }
            return true;
        }, getText("discord.entry", "state", resident.getSettlement().getStateTag(),
                "name", resident.getSettlement().getName(),
                "discord", resident.getSettlement().getDiscord(),
                "action", resident.hasPermission("settlement.discord.set") ? getText("unlockables.unlocked.change") : getText("no-permission")
        )));


        gui.addElement(new StaticGuiElement('j', getIcon("joinfee"), click -> {
            if (resident.hasPermission("settlement.joinfee.set")) {
                Settlement settlement = resident.getSettlement();
                new AnvilGUI.Builder()
                        .itemLeft(getIcon("joinfee"))
                        .title(getText("joinfee.anvil.title", "name", settlement.getName(), "joinfee", String.valueOf(settlement.getJoinFee())))
                        .text(getText("joinfee.anvil.placeholder", "joinfee", String.valueOf(settlement.getJoinFee())))
                        .onClose(player -> Utils.runLater(() -> new SettingsGUI(resident).open(), 1))
                        .onComplete((player, text) -> {
                            if (settlement.hasPermission(resident, "joinfee.set")) {
                                try {
                                    if (!text.isEmpty()) {
                                        long amount = Long.parseLong(text);
                                        settlement.setJoinFee(amount);
                                        Message.get("command.settlement.joinfee.set.success").tell(resident.getSettlement().getOnlineResidents(),
                                                "state", resident.getSettlement().getStateTag(),
                                                "amount", String.valueOf(resident.getSettlement().getJoinFee())
                                        );
                                        settlement.save();
                                    }
                                    Utils.runLater(() -> new SettingsGUI(resident).open(), 1);
                                } catch (NumberFormatException e) {
                                    return AnvilGUI.Response.text(getError("not-a-number").getReplacedText("input", text));
                                }
                            } else {
                                getError("no-permission").tell(player);
                            }
                            return AnvilGUI.Response.close();
                        })
                        .plugin(SettlementsPlugin.getInstance())
                        .open(resident.getPlayer());
            }
            return true;
        }, getText("joinfee.entry",
                "state", resident.getSettlement().getStateTag(),
                "name", resident.getSettlement().getName(),
                "joinfee", String.valueOf(resident.getSettlement().getJoinFee()),
                "action", resident.hasPermission("settlement.joinfee.set") ? getText("unlockables.unlocked.edit") : getText("no-permission")
        )));

        gui.addElement(new StaticGuiElement('e', getIcon("description"), click -> {
            if (resident.hasPermission("settlement.setdescription")) {
                Settlement settlement = resident.getSettlement();
                new AnvilGUI.Builder()
                        .itemLeft(getIcon("description"))
                        .title(getText("description.anvil.title", "name", settlement.getName(), "description", settlement.getDescription()))
                        .text(getText("description.anvil.placeholder", "description", settlement.getDescription()))
                        .onClose(player -> Utils.runLater(() -> new SettingsGUI(resident).open(), 1))
                        .onComplete((player, text) -> {
                            if (settlement.hasPermission(resident, "setdescription")) {
                                String description = text.trim();
                                settlement.setDescription(Utils.filterAllCurses(description));
                                if (!text.isEmpty()) {
                                    Message.get("command.settlement.setdescription.success").tell(player,
                                            "state", settlement.getStateTag(),
                                            "description", settlement.getDescription()
                                    );
                                    settlement.save();
                                }
                                Utils.runLater(() -> new SettingsGUI(resident).open(), 1);
                            } else {
                                getError("no-permission").tell(player);
                            }
                            return AnvilGUI.Response.close();
                        })
                        .plugin(SettlementsPlugin.getInstance())
                        .open(resident.getPlayer());
            }
            return true;
        }, getText("description.entry",
                "state", resident.getSettlement().getStateTag(),
                "name", resident.getSettlement().getName(),
                "description", resident.getSettlement().getDescription(),
                "action", resident.hasPermission("settlement.setdescription") ? getText("unlockables.unlocked.edit") : getText("no-permission")
        )));

        gui.addElement(new StaticGuiElement('a', getIcon("access"), click -> {
            if (resident.hasPermission("settlement.setaccess")) {
                new AccessGUI(resident, "settlement", resident.getSettlement()).open();
            }
            return true;
        }, getText("access",
                "access", resident.getSettlement().getAccess().getTag(),
                "state", resident.getSettlement().getStateTag(),
                "action", resident.hasPermission("settlement.setaccess") ? getText("unlockables.unlocked.change") : getText("no-permission")
        )));

        gui.addElement(new StaticGuiElement('c', getIcon("recruitment"), click -> {
            if (resident.hasPermission("settlement.recruitment")) {
                new RecruitmentGUI("settlement", resident.getSettlement()).open((Player) click.getEvent().getWhoClicked());
            }
            return true;
        }, getText("recruitment",
                "jointype", resident.getSettlement().getJoinType().getTag(),
                "state", resident.getSettlement().getStateTag(),
                "action", resident.getSettlement().hasUnlocked("recruitment") ? resident.hasPermission("settlement.recruitment") ? getText("unlockables.unlocked.change") : getText("no-permission") : getText("unlockables.locked.npc")
        )));

        gui.addElement(new GuiBackElement('b', getIcon("back"), true, getText("back")));
    }
}
