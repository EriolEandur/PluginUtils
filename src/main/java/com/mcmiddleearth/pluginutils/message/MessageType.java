/*
 * Copyright (C) 2016 MCME
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
package com.mcmiddleearth.pluginutils.message;

import lombok.Getter;
import org.bukkit.ChatColor;

/**
 *
 * @author Eriol_Eandur
 */
public enum MessageType {
    
    INFO                (ChatColor.AQUA, MessageUtil.getPREFIX()),
    ERROR               (ChatColor.RED, MessageUtil.getPREFIX()),
    HIGHLIGHT           (ChatColor.GOLD, MessageUtil.getPREFIX()),
    INFO_INDENTED       (ChatColor.AQUA, MessageUtil.getNOPREFIX()),
    ERROR_INDENTED      (ChatColor.RED, MessageUtil.getNOPREFIX()),
    HIGHLIGHT_INDENTED  (ChatColor.GOLD, MessageUtil.getNOPREFIX()),
    INFO_NO_PREFIX      (ChatColor.AQUA, ""),
    ERROR_NO_PREFIX     (ChatColor.RED, ""),
    HIGHLIGHT_NO_PREFIX (ChatColor.GOLD, ""),
    WHITE               (ChatColor.WHITE,"");
    
    @Getter
    private final ChatColor baseColor;
    
    @Getter
    private final String prefix;
    
    private MessageType(ChatColor color, String prefix) {
        baseColor = color;
        this.prefix = prefix;
    }
}
