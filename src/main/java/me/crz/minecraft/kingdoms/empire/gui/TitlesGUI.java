package me.crz.minecraft.kingdoms.empire.gui;

import de.themoep.inventorygui.DynamicGuiElement;
import de.themoep.inventorygui.GuiElementGroup;
import de.themoep.inventorygui.InventoryGui;
import de.themoep.inventorygui.StaticGuiElement;
import me.crz.minecraft.kingdoms.KingdomsGUI;
import me.crz.minecraft.kingdoms.KingdomsPlugin;
import me.crz.minecraft.kingdoms.chat.ChatManager;
import me.crz.minecraft.kingdoms.command.empire.EmpireTitlesCommand;
import me.crz.minecraft.kingdoms.rank.EmpireRank;
import me.crz.minecraft.kingdoms.utils.ItemUtils;
import me.crz.minecraft.kingdoms.utils.Message;
import me.crz.minecraft.kingdoms.utils.Utils;
import me.crz.minecraft.kingdoms.village.Resident;
import net.wesjd.anvilgui.AnvilGUI;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class TitlesGUI extends KingdomsGUI {
    private static final String[] SETUP = {
            "         ",
            "         ",
            "ggggggggg",
            "ggggggggg",
            "         ",
            "    b   a"
    };
    private final Resident resident;
    private final InventoryGui gui;

    public TitlesGUI(Resident resident, EmpireRank rank) {
        super("empire.titles");
        this.resident = resident;
        gui = new InventoryGui(KingdomsPlugin.getInstance(), getText("title",
                "state", resident.hasEmpire() ? resident.getVillage().getEmpire().getStateTag() : "",
                "rank", rank.getTag(resident.getVillage().getEmpire())
        ), SETUP);
        gui.setCloseAction(close -> false);
        gui.setFiller(BORDER_ITEM);

        if (resident.hasEmpire()) {
            GuiElementGroup group = new GuiElementGroup('g');

            for (String title : resident.getVillage().getEmpire().getTitles(rank)) {
                group.addElement(new DynamicGuiElement('g', h -> {
                    ItemStack item = new ItemStack(Material.PAPER);
                    List<String> description = new ArrayList<>();
                    description.add(getText("entry.title",
                            "title", title,
                            "rank", rank.getTag(resident.getVillage().getEmpire())
                    ));
                    if (title.equals(resident.getVillage().getEmpire().getDefaultTitle(rank))) {
                        description.add(getText("entry.default",
                                "title", title,
                                "rank", rank.getTag(resident.getVillage().getEmpire())
                        ));
                        ItemUtils.setGlowing(item, true);
                    } else if (resident.hasPermission("empire.titles.set")) {
                        description.add(getText("entry.set",
                                "title", title,
                                "rank", rank.getTag(resident.getVillage().getEmpire())
                        ));
                    }
                    if (resident.hasPermission("empire.titles.remove") && !rank.above(resident.getVillageRank())) {
                        description.add(getText("entry.remove",
                                "title", title,
                                "rank", rank.getTag(resident.getVillage().getEmpire())
                        ));
                    }
                    return new StaticGuiElement('g', item, click -> {
                        if (resident.hasEmpire()) {
                            switch (click.getType()) {
                                case LEFT:
                                case SHIFT_LEFT:
                                    if (resident.hasPermission("empire.titles.set")) {
                                        String oldRankName = rank.getTag(resident.getVillage().getEmpire());
                                        resident.getVillage().getEmpire().setDefaultTitle(rank, title);
                                        Message.get("command.empire.titles.set.success").tell(resident, "title", title, "rank", oldRankName);
                                        gui.draw();
                                    }
                                    return true;
                                case RIGHT:
                                case SHIFT_RIGHT:
                                    if (resident.hasPermission("empire.titles.remove")) {
                                        resident.getVillage().getEmpire().removeTitle(rank, title);
                                        Message.get("command.empire.titles.remove.success").tell(resident,
                                                "title", title,
                                                "rank", rank.getTag(resident.getVillage().getEmpire())
                                        );
                                        new TitlesGUI(resident, rank).open();
                                    }
                                    return true;
                            }
                        }
                        return true;
                    }, description.toArray(new String[0]));
                }));
            }


            gui.addElement(group);

            if (resident.hasPermission("empire.titles.add")
                    && !rank.above(resident.getEmpireRank())
                    && resident.getVillage().getEmpire().getTitles(rank).size() < Utils.getTitlesPerRank()) {
                gui.addElement(new StaticGuiElement('a', new ItemStack(Material.WRITABLE_BOOK), click -> {
                    if (resident.hasPermission("empire.titles.add")) {
                        new AnvilGUI.Builder()
                                .item(new ItemStack(Material.WRITABLE_BOOK))
                                .title(getMessage("add.anvil.title").getReplacedText(
                                        "rank", rank.getTag(resident.getVillage().getEmpire()),
                                        "state", resident.getVillage().getStateTag()
                                ))
                                .text(getMessage("add.anvil.placeholder").getReplacedText(
                                        "rank", rank.getTag(resident.getVillage().getEmpire()),
                                        "state", resident.getVillage().getEmpire().getStateTag()
                                ))
                                .onClose(player -> Utils.runLater(() -> new TitlesGUI(resident, rank).open(), 1))
                                .onComplete((player, text) -> {
                                    text = ChatColor.stripColor(text);
                                    if (rank.above(resident.getVillageRank())) {
                                        return AnvilGUI.Response.text(getError("cant-edit-higher")
                                                .getReplacedText("rank", resident.getEmpireRank().getTag(resident.getVillage().getEmpire())));
                                    } else if (resident.getVillage().getEmpire().getTitles(rank).size() >= Utils.getTitlesPerRank()) {
                                        return AnvilGUI.Response.text(getError("too-many").getReplacedText(
                                                "rank", resident.getEmpireRank().getTag(),
                                                "max", String.valueOf(Utils.getTitlesPerRank())
                                        ));
                                    } else if (text.length() > 16) {
                                        return AnvilGUI.Response.text(getError("too-long").getReplacedText("input", text, "amount", "16"));
                                    } else if (!EmpireTitlesCommand.TITLE_PATTERN.matcher(text).matches()) {
                                        return AnvilGUI.Response.text(getError("invalid-characters").getReplacedText("input", text));
                                    } else if (ChatManager.getInstance().containsAnyCurses(text)) {
                                        return AnvilGUI.Response.text(getError("prohibited-words").getReplacedText("input", text));
                                    } else if (resident.getVillage().getEmpire().addTitle(rank, text)) {
                                        Message.get("command.empire.titles.add.success")
                                                .tell(resident, "title", text, "rank", rank.getTag(resident.getVillage().getEmpire()));
                                        return AnvilGUI.Response.close();
                                    } else {
                                        return AnvilGUI.Response.text(getError("already-exists")
                                                .getReplacedText("input", text, "rank", rank.getTag(resident.getVillage().getEmpire())));
                                    }
                                })
                                .plugin(KingdomsPlugin.getInstance())
                                .open((Player) click.getEvent().getWhoClicked());
                    }
                    return true;
                }, getText("add.title",
                        "rank", rank.getTag(resident.getVillage().getEmpire()),
                        "state", resident.getVillage().getEmpire().getStateTag()
                )));
            }
        }

        gui.addElement(new StaticGuiElement('b', new ItemStack(Material.BARRIER), click -> {
            new TitleGUI(resident).open();
            return true;
        }, getText("back")));
    }

    public void open() {
        gui.show(resident.getPlayer());
    }
}
