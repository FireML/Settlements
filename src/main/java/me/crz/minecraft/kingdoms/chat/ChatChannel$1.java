package me.crz.minecraft.kingdoms.chat;

import java.util.Arrays;
import java.util.List;

enum ChatChannel$1
{
    ChatChannel$1(final String s, final int n) {
    }
    
    @Override
    public List<String> getAliases() {
        return Arrays.<String>asList("local", "l");
    }
}