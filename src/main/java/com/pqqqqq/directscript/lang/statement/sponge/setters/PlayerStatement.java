package com.pqqqqq.directscript.lang.statement.sponge.setters;

import com.google.common.base.Optional;
import com.pqqqqq.directscript.lang.reader.Context;
import com.pqqqqq.directscript.lang.statement.Statement;
import org.spongepowered.api.entity.player.Player;
import org.spongepowered.api.text.Texts;

/**
 * Created by Kevin on 2015-06-02.
 * A statement that sends a message to a player
 */
public class PlayerStatement extends Statement {

    public PlayerStatement() {
        super(Syntax.builder()
                .identifiers("player")
                .prefix("@")
                .arguments(Arguments.of(Argument.from("Message")), Arguments.of(Argument.from("Player"), ",", Argument.from("Message")))
                .build());
    }

    @Override
    public Result run(Context ctx) {
        Optional<Player> player = ctx.getLiteral("Player").getPlayer();
        String message = ctx.getLiteral("Message").getString();

        if (!player.isPresent()) {
            return Result.failure();
        }

        player.get().sendMessage(Texts.of(message));
        return Result.success();
    }
}
