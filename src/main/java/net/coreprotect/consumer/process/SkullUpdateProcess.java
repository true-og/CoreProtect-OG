package net.coreprotect.consumer.process;

import java.sql.Statement;
import net.coreprotect.config.ConfigHandler;
import net.coreprotect.database.Database;
import net.coreprotect.database.statement.SkullStatement;
import net.coreprotect.utility.Util;
import org.bukkit.block.BlockState;

class SkullUpdateProcess {

    static void process(Statement statement, Object object, int rowId) {

        /*
         * We're switching blocks around quickly. This block could already be removed
         * again by the time the server tries to modify it. Ignore any errors.
         */
        if (object instanceof BlockState) {

            BlockState block = (BlockState) object;
            StringBuilder query = new StringBuilder("SELECT owner");
            if (Database.hasSkullSkinColumn()) {

                query.append(", skin");

            }

            if (Database.hasSkullMetadataColumn()) {

                query.append(", metadata");

            }

            query.append(" FROM ").append(ConfigHandler.prefix).append("skull WHERE rowid='").append(rowId)
                    .append("' LIMIT 0, 1");
            SkullStatement.getData(statement, block, query.toString());
            Util.updateBlock(block);

        }

    }

}
