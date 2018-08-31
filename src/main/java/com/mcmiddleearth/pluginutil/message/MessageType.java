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
package com.mcmiddleearth.pluginutil.message;

import lombok.Getter;
import org.bukkit.ChatColor;

/**
 * Konstants for fancy message types which will determine defaut color and prefix of the message.
 * @author Eriol_Eandur
 */
public enum MessageType {
    
    INFO                (ChatColor.AQUA),
    ERROR               (ChatColor.RED),
    HIGHLIGHT           (ChatColor.GOLD),
    INFO_INDENTED       (ChatColor.AQUA),
    ERROR_INDENTED      (ChatColor.RED),
    HIGHLIGHT_INDENTED  (ChatColor.GOLD),
    INFO_NO_PREFIX      (ChatColor.AQUA),
    ERROR_NO_PREFIX     (ChatColor.RED),
    HIGHLIGHT_NO_PREFIX (ChatColor.GOLD),
    WHITE               (ChatColor.WHITE);
    
    @Getter
    private final ChatColor baseColor;
    
    private MessageType(ChatColor color) {
        baseColor = color;
    }
}
