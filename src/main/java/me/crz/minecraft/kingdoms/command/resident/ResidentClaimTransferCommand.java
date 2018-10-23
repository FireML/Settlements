package me.crz.minecraft.kingdoms.command.resident;

/*
 * Kingdoms - kingdoms
 * Copyright (c) 2018 Max Lee aka Phoenix616 (mail@moep.tv)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

import me.crz.minecraft.kingdoms.command.SubCommand;
import me.crz.minecraft.kingdoms.command.VillageSubCommand;
import me.crz.minecraft.kingdoms.db.DatabaseManager;
import me.crz.minecraft.kingdoms.rank.VillageRank;
import me.crz.minecraft.kingdoms.utils.MessageUtils;
import me.crz.minecraft.kingdoms.utils.Utils;
import me.crz.minecraft.kingdoms.village.Resident;

public class ResidentClaimTransferCommand extends VillageSubCommand {
    public ResidentClaimTransferCommand(SubCommand parent) {
        super(parent, "claimtransfer <amount>", VillageRank.RESIDENT, "transferclaims", "bonustransfer", "transferbonus");
        setPermission("kingdoms.user");
    }

    @Override
    public boolean run(Resident resident, String base, String alias, String... args) {
        if (args.length < 1) {
            return false;
        }
        try {
            int transfer = Integer.parseInt(args[0]);
            if (resident.removeBonusClaims(transfer)) {
                resident.getVillage().addBonusClaims(transfer);
                Utils.runAsync(() -> {
                    DatabaseManager.getInstance().saveResident(resident);
                    DatabaseManager.getInstance().saveVillage(resident.getVillage());
                    DatabaseManager.getInstance().log(resident.getVillage(), resident, "deposit", "claim", transfer);
                });
                MessageUtils.tellResidents(resident.getVillage().getOnlineResidents(), getMessage("success"),
                        "rank", resident.getVillageRank().getTag(resident.getVillage()),
                        "title", resident.getTitle(),
                        "player", resident.getName(),
                        "amount", String.valueOf(transfer),
                        "state", resident.getVillage().getState().getTag());
            } else {
                resident.tell(getError("not-enough-bonus"), "amount", String.valueOf(transfer));
            }
        } catch (NumberFormatException e) {
            return false;
        }
        return true;
    }
}
