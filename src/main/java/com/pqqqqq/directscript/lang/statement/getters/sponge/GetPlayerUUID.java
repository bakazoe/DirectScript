package com.pqqqqq.directscript.lang.statement.getters.sponge;

import com.google.common.base.Optional;
import com.pqqqqq.directscript.lang.annotation.Statement;
import com.pqqqqq.directscript.lang.reader.Line;
import com.pqqqqq.directscript.lang.statement.IStatement;
import com.pqqqqq.directscript.lang.statement.StatementResult;
import org.spongepowered.api.entity.player.Player;

/**
 * Created by Kevin on 2015-06-04.
 */
@Statement(prefix = "@", identifiers = {"uuid"})
public class GetPlayerUUID implements IStatement<String> {

    public StatementResult<String> run(Line.LineContainer line) {
        Optional<Player> player = line.getPlayerOrCauser(0);
        if (!player.isPresent()) {
            return StatementResult.failure();
        }

        return StatementResult.<String>builder().success().result(player.get().getIdentifier()).literal(player.get().getIdentifier()).build();
    }
}
