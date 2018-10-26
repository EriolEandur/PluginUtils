/*
 * Copyright (C) 2015 MCME
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
package com.mcmiddleearth.pluginutil.confirmation;

import org.bukkit.entity.Player;

/**
 * Interface for tasks that will be executed depending on the answer of a 
 * player in a confirmation conversation.
 * @author Eriol_Eandur
 */
public interface Confirmationable {
    
    /**
     * Executed if the player answers true or yes in the conersation.
     * @param player to whom the conversation is displayed
     * @param data data objects for free use of the executed task
     */
    public void confirmed(Player player, Object[] data);
    
    /**
     * Executed if the player answers false or no in the conersation.
     * @param player to whom the conversation is displayed
     * @param data data objects for free use of the executed task
     */
    public void cancelled(Player player, Object[] data);
    
}
