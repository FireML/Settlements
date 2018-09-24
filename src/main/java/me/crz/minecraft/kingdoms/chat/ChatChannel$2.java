package me.crz.minecraft.kingdoms.chat;

import java.util.Arrays;
import java.util.List;

enum ChatChannel$2
{
    ChatChannel$2(final String s, final int n) {
    }
    
    @Override
    public List<String> getAliases() {
        return Arrays.<String>asList("trade", "t");
    }
}