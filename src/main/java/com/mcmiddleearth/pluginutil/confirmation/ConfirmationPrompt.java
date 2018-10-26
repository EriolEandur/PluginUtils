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

import org.bukkit.conversations.BooleanPrompt;
import org.bukkit.conversations.ConversationContext;
import org.bukkit.conversations.Prompt;

/**
 * Boolean prompt to display a query defined in convesation context.
 * @author Eriol_Eandur
 */
public class ConfirmationPrompt extends BooleanPrompt{

    @Override
    protected Prompt acceptValidatedInput(ConversationContext cc, boolean answer) {
        cc.setSessionData("answer", answer);
        return Prompt.END_OF_CONVERSATION;
    }

    @Override
    protected Prompt acceptValidatedInput(ConversationContext cc, String answer) {
        if(answer.equalsIgnoreCase("true") || answer.equalsIgnoreCase("yes"))
            return acceptValidatedInput(cc, true);
        else
            return acceptValidatedInput(cc, false);
    }

    @Override
    public String getPromptText(ConversationContext cc) {
        return (String) cc.getSessionData("query");
    }
    
    @Override
    protected boolean isInputValid(ConversationContext context, String answer){
        return answer.equalsIgnoreCase("no") 
            || answer.equalsIgnoreCase("yes")
            || answer.equalsIgnoreCase("false")
            || answer.equalsIgnoreCase("true");
    }
    
    @Override
    protected String getFailedValidationText(ConversationContext context, String invalidInput){
        return "Type 'yes' or 'no' in chat.";
    }
    
}
