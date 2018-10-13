package me.crz.minecraft.kingdoms.command.village.money;

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

import me.crz.minecraft.kingdoms.command.VillageSubCommand;
import me.crz.minecraft.kingdoms.village.Resident;
import me.crz.minecraft.kingdoms.rank.VillageRank;

import java.text.DecimalFormat;

import me.crz.minecraft.kingdoms.command.SubCommand;

public class VillageMoneyCommand extends VillageSubCommand {
    private final static DecimalFormat DF = new DecimalFormat("#,###");

    public VillageMoneyCommand(SubCommand parent) {
        super(parent, "bank <deposit|withdraw|balance> <amount>", "money");
        setPermission("kingdoms.user");
        registerCommand(new VillageSubCommand(this, "balance", VillageRank.RESIDENT) {
            @Override
            public boolean run(Resident resident, String base, String alias, String... args) {
                resident.tell("&2&lVillage Balance: &r&7" + DF.format((long) resident.getVillage().getBank()));
                return true;
            }
        });
        registerCommand(new MoneyWithdrawCommand(this));
        registerCommand(new MoneyDepositCommand(this));
    }

    @Override
    public boolean run(Resident resident, String base, String alias, String... args) {
        return false;
    }
}
