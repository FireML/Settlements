package me.crz.minecraft.kingdoms.chat;

import java.util.Arrays;
import java.util.List;

enum ChatChannel$4
{
    ChatChannel$4(final String s, final int n) {
    }
    
    @Override
    public List<String> getAliases() {
        return Arrays.<String>asList("settlement", "s", "village");
    }
}