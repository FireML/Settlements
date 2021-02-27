package me.crz.minecraft.kingdoms.command;

import me.crz.minecraft.kingdoms.resident.Resident;

public abstract class ResidentPermsSubCommand extends ResidentSubCommand {

    public ResidentPermsSubCommand(SubCommand parent, String usage, String... aliases) {
        super(parent, usage, aliases);
    }

    public int ordinal() {
        return -1;
    }

    @Override
    public boolean hasAccess(Resident resident) {
        return resident.hasPermission(getPermission());
    }
}
