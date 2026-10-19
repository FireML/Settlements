package de.themoep.settlements.command.exceptions;

import com.mojang.brigadier.LiteralMessage;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;

public class NotAResidentException extends SimpleCommandExceptionType {

    public NotAResidentException() {
        super(new LiteralMessage("You are not a resident?"));
    }

}
