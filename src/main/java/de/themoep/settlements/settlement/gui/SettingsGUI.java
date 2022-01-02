package de.themoep.settlements.settlement.gui;

import de.themoep.inventorygui.GuiBackElement;
import de.themoep.inventorygui.StaticGuiElement;
import de.themoep.settlements.SettlementsPlugin;
import de.themoep.settlements.integration.MailProvider;
import de.themoep.settlements.land.LandOwner;
import de.themoep.settlements.land.gui.AccessGUI;
import de.themoep.settlements.land.gui.RecruitmentGUI;
import de.themoep.settlements.resident.Resident;
import de.themoep.settlements.resident.ResidentManager;
import de.themoep.settlements.settlement.Settlement;
import de.themoep.settlements.settlement.SettlementManager;
import de.themoep.settlements.utils.ConfigurableGui;
import de.themoep.settlements.utils.Message;
import de.themoep.settlements.utils.SignGui;
import de.themoep.settlements.utils.Utils;
import de.themoep.settlements.utils.config.Config;
import net.wesjd.anvilgui.AnvilGUI;
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

        LandOwner<?, ?> landOwner = resident.getSettlement();

        gui.addElement(new StaticGuiElement('n', getIcon("rename"), click -> {
            if (landOwner.hasPermission(resident, "rename")) {
                new AnvilGUI.Builder()
                        .itemLeft(getIcon("rename"))
                        .title(getText("rename.anvil.title", "name", landOwner.getName()))
                        .text(getText("rename.anvil.placeholder", "name", landOwner.getName()))
                        .onClose(player -> Utils.runLater(() -> new SettingsGUI(resident).open(), 1))
                        .onComplete((player, text) -> {
                            if (landOwner.hasPermission(resident, "rename")) {
                                String name = Utils.filterAllCurses(text.trim());
                                if (name.toLowerCase().contains("claims")) {
                                    getError("invalid-characters").tell(player, "input", name);
                                    return AnvilGUI.Response.text(getError("invalid-characters").getReplacedText("input", name));
                                } else if (landOwner instanceof Settlement && SettlementManager.getInstance().getFromName(name) != null) {
                                    getError("settlement-exists").tell(player, "input", name);
                                    return AnvilGUI.Response.text(getError("settlement-exists").getReplacedText("name", name));
                                } else if (landOwner instanceof Settlement && name.length() > Config.Settlement.NAME_LENGTH) {
                                    getError("too-long").tell(player, "input", name, "amount", String.valueOf(Config.Settlement.NAME_LENGTH));
                                    return AnvilGUI.Response.text(getError("too-long").getReplacedText("input", name, "amount", String.valueOf(Config.Settlement.NAME_LENGTH)));
                                } else if (!namePattern.matcher(name).matches()) {
                                    getError("invalid-characters").tell(player, "input", name);
                                    return AnvilGUI.Response.text(getError("invalid-characters").getReplacedText("input", name));
                                } else {
                                    if (landOwner instanceof Settlement) {
                                        SettlementManager.getInstance().changeName((Settlement) landOwner, name);
                                    }
                                    Message.get("command." + landOwner.getKey() + ".rename.success").tell(landOwner, "name", landOwner.getName(), "state", landOwner.getStateTag());
                                    Utils.runLater(() -> new SettingsGUI(player).open(), 1);
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
                "state", landOwner.getStateTag(),
                "name", landOwner.getName(),
                "action", landOwner.hasPermission(resident, "rename") ? getText("unlockables.unlocked.change") : getText("no-permission")
        )));

        gui.addElement(new StaticGuiElement('p', getIcon("perks"), click -> {
            if (landOwner.hasPermission(resident, "perks")) {
                new PerksGUI(resident).open();
            }
            return true;
        }, getText("perks.entry",
                "state", landOwner.getStateTag(),
                "perks", landOwner.getPerks().stream().map(p -> getText("perks.list", "name", p.getTag())).collect(Collectors.joining("\n")),
                "action", landOwner.hasPermission(resident, "perks") ? getText("unlockables.unlocked.options") : getText("no-permission")
        )));

        gui.addElement(new StaticGuiElement('r', getIcon("rules"), click -> {
            if (!landOwner.hasPermission(resident, "rules.set")) {
                return true;
            }
            if (click.getType() == ClickType.LEFT) {
                ItemStack cursor = click.getEvent().getCursor();
                if (cursor != null) {
                    ItemMeta meta = cursor.getItemMeta();
                    if (meta instanceof BookMeta) {
                        String rules = String.join("\n", ((BookMeta) meta).getPages());
                        landOwner.setRules(rules);
                        landOwner.save();
                        getMessage("rules.updated").tell(landOwner.getOnlineResidents(), "state", landOwner.getStateTag());
                        for (MailProvider provider : SettlementsPlugin.getInstance().getMailProviders()) {
                            for (Resident r : landOwner.getResidents()) {
                                provider.sendMail(resident.getName(), r, getText("rules.mail",
                                        "state", landOwner.getStateTag(),
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
                if (landOwner.hasPermission(resident, "rules.set")) {
                    String rules = String.join("\n", text).trim();
                    if (!rules.isEmpty() && !rules.equals(landOwner.getRules())) {
                        landOwner.setRules(rules);
                        landOwner.save();
                        getMessage("rules.updated").tell(landOwner.getOnlineResidents(), "state", landOwner.getStateTag());
                        for (MailProvider provider : SettlementsPlugin.getInstance().getMailProviders()) {
                            for (Resident r : landOwner.getResidents()) {
                                provider.sendMail(resident.getName(), r, getText("rules.mail",
                                        "state", landOwner.getStateTag(),
                                        "date", Utils.DATE_FORMAT.format(System.currentTimeMillis())
                                ));
                            }
                        }
                    }
                }
                Utils.runLater(() -> new SettingsGUI(resident).open(), 1);
            }).open((Player) click.getWhoClicked());
            return true;
        }, getText("rules.entry",
                "state", landOwner.getStateTag(),
                "action", landOwner.hasPermission(resident, "rules.set") ? getText("unlockables.unlocked.change") : getText("no-permission"))
        ));

        gui.addElement(new StaticGuiElement('d', getIcon("discord"), click -> {
            if (landOwner.hasPermission(resident, "discord.set")) {
                new AnvilGUI.Builder()
                        .itemLeft(getIcon("discord"))
                        .title(getText("discord.anvil.title", "name", landOwner.getName(), "discord", landOwner.getDiscord()))
                        .text(getText("discord.anvil.placeholder", "discord", landOwner.getDiscord()))
                        .onClose(player -> Utils.runLater(() -> new SettingsGUI(resident).open(), 1))
                        .onComplete((player, text) -> {
                            if (landOwner.hasPermission(resident, "discord.set")) {
                                String link = text.trim();
                                String oldLink = landOwner.getDiscord();
                                landOwner.setDiscord(link);
                                if (text.isEmpty()) {
                                    Message.get("command." + landOwner.getKey() + ".discord.set.removed").tell(resident,
                                            "state", landOwner.getStateTag(),
                                            "discordlink", oldLink
                                    );
                                } else {
                                    Message.get("command." + landOwner.getKey() + ".discord.set.success").tell(resident,
                                            "state", landOwner.getStateTag(),
                                            "discordlink", landOwner.getDiscord()
                                    );
                                }
                                landOwner.save();
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
        }, getText("discord.entry", "state", landOwner.getStateTag(),
                "name", landOwner.getName(),
                "discord", landOwner.getDiscord(),
                "action", landOwner.hasPermission(resident, "discord.set") ? getText("unlockables.unlocked.change") : getText("no-permission")
        )));


        gui.addElement(new StaticGuiElement('j', getIcon("joinfee"), click -> {
            if (landOwner.hasPermission(resident, "joinfee.set")) {
                new AnvilGUI.Builder()
                        .itemLeft(getIcon("joinfee"))
                        .title(getText("joinfee.anvil.title", "name", landOwner.getName(), "joinfee", String.valueOf(landOwner.getJoinFee())))
                        .text(getText("joinfee.anvil.placeholder", "joinfee", String.valueOf(landOwner.getJoinFee())))
                        .onClose(player -> Utils.runLater(() -> new SettingsGUI(resident).open(), 1))
                        .onComplete((player, text) -> {
                            if (landOwner.hasPermission(resident, "joinfee.set")) {
                                try {
                                    if (!text.isEmpty()) {
                                        long amount = Long.parseLong(text);
                                        landOwner.setJoinFee(amount);
                                        Message.get("command." + landOwner.getKey() + ".joinfee.set.success").tell(landOwner.getOnlineResidents(),
                                                "state", landOwner.getStateTag(),
                                                "amount", String.valueOf(landOwner.getJoinFee())
                                        );
                                        landOwner.save();
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
                "state", landOwner.getStateTag(),
                "name", landOwner.getName(),
                "joinfee", String.valueOf(landOwner.getJoinFee()),
                "action", landOwner.hasPermission(resident, "joinfee.set") ? getText("unlockables.unlocked.edit") : getText("no-permission")
        )));

        gui.addElement(new StaticGuiElement('e', getIcon("description"), click -> {
            if (landOwner.hasPermission(resident, "setdescription")) {
                new AnvilGUI.Builder()
                        .itemLeft(getIcon("description"))
                        .title(getText("description.anvil.title", "name", landOwner.getName(), "description", landOwner.getDescription()))
                        .text(getText("description.anvil.placeholder", "description", landOwner.getDescription()))
                        .onClose(player -> Utils.runLater(() -> new SettingsGUI(resident).open(), 1))
                        .onComplete((player, text) -> {
                            if (landOwner.hasPermission(resident, "setdescription")) {
                                String description = text.trim();
                                landOwner.setDescription(Utils.filterAllCurses(description));
                                if (!text.isEmpty()) {
                                    Message.get("command." + landOwner.getKey() + ".setdescription.success").tell(player,
                                            "state", landOwner.getStateTag(),
                                            "description", landOwner.getDescription()
                                    );
                                    landOwner.save();
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
                "state", landOwner.getStateTag(),
                "name", landOwner.getName(),
                "description", landOwner.getDescription(),
                "action", landOwner.hasPermission(resident, "setdescription") ? getText("unlockables.unlocked.edit") : getText("no-permission")
        )));

        gui.addElement(new StaticGuiElement('a', getIcon("access"), click -> {
            if (landOwner.hasPermission(resident, "setaccess")) {
                new AccessGUI(resident, landOwner.getKey(), landOwner).open();
            }
            return true;
        }, getText("access",
                "access", landOwner.getAccess().getTag(),
                "state", landOwner.getStateTag(),
                "action", landOwner.hasPermission(resident, "setaccess") ? getText("unlockables.unlocked.change") : getText("no-permission")
        )));

        gui.addElement(new StaticGuiElement('c', getIcon("recruitment"), click -> {
            if (landOwner.hasPermission(resident, "recruitment")) {
                new RecruitmentGUI(resident, landOwner).open();
            }
            return true;
        }, getText("recruitment",
                "jointype", landOwner.getJoinType().getTag(),
                "state", landOwner.getStateTag(),
                "action", landOwner.hasUnlocked("recruitment") ? landOwner.hasPermission(resident, "recruitment") ? getText("unlockables.unlocked.change") : getText("no-permission") : getUnlockText(landOwner, "recruitment")
        )));

        gui.addElement(new GuiBackElement('b', getIcon("back"), true, getText("back")));
    }
}
