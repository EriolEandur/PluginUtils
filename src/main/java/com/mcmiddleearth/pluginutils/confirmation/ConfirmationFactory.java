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
package com.mcmiddleearth.pluginutils.confirmation;

import com.mcmiddleearth.pluginutils.message.MessageUtil;
import org.bukkit.conversations.Conversation;
import org.bukkit.conversations.ConversationAbandonedEvent;
import org.bukkit.conversations.ConversationAbandonedListener;
import org.bukkit.conversations.ConversationContext;
import org.bukkit.conversations.ConversationFactory;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

/**
 *
 * @author Eriol_Eandur
 */
public class ConfirmationFactory implements ConversationAbandonedListener{
    
    private final ConversationFactory factory;
    
    private final MessageUtil messageUtil;
    
    public ConfirmationFactory(Plugin plugin, MessageUtil messageUtil){
        this. messageUtil = messageUtil;
        factory = new ConversationFactory(plugin)
                .withModality(false)
                .withPrefix(new ConfirmationPrefix())
                .withFirstPrompt(new ConfirmationPrompt())
                .withTimeout(60)
                .addConversationAbandonedListener(this);
    }
    
    public void start(Player player, String query, Confirmationable task, Object[] data) {
        Conversation conversation = factory.buildConversation(player);
        ConversationContext context = conversation.getContext();
        context.setSessionData("player", player);
        context.setSessionData("task", task);
        context.setSessionData("query", query);
        context.setSessionData("data", data);
        context.setSessionData("messageUtil",messageUtil);
        conversation.begin();
    }
   
    @Override
    public void conversationAbandoned(ConversationAbandonedEvent abandonedEvent) {
        ConversationContext cc = abandonedEvent.getContext();
        Player player = (Player) cc.getSessionData("player");
        Object[] data = (Object[]) cc.getSessionData("data");
        if (abandonedEvent.gracefulExit() && (Boolean) cc.getSessionData("answer")) {
            ((Confirmationable) cc.getSessionData("task")).confirmed(player, data);
        }
        else {
            ((Confirmationable) cc.getSessionData("task")).cancelled(player, data);
        }
    }

}
