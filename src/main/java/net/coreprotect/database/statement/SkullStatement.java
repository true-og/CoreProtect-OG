package net.coreprotect.database.statement;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import net.coreprotect.database.Database;
import net.coreprotect.extensions.PlayerBountiesHeadCompatibility;
import net.coreprotect.paper.PaperAdapter;
import org.bukkit.block.BlockState;
import org.bukkit.block.Skull;

public class SkullStatement {

    private SkullStatement() {

        throw new IllegalStateException("Database class");

    }

    public static ResultSet insert(PreparedStatement preparedStmt, int time, String owner, String skin,
            String metadata)
    {

        try {

            preparedStmt.setInt(1, time);
            preparedStmt.setString(2, owner);
            int parameterIndex = 3;
            if (Database.hasSkullSkinColumn()) {

                preparedStmt.setString(parameterIndex++, skin);

            }

            if (Database.hasSkullMetadataColumn()) {

                preparedStmt.setString(parameterIndex, metadata);

            }

            if (Database.hasReturningKeys()) {

                return preparedStmt.executeQuery();

            } else {

                preparedStmt.executeUpdate();

            }

        } catch (Exception e) {

            e.printStackTrace();

        }

        return null;

    }

    public static void getData(Statement statement, BlockState block, String query) {

        try {

            if (!(block instanceof Skull)) {

                return;

            }

            Skull skull = (Skull) block;
            ResultSet resultSet = statement.executeQuery(query);

            while (resultSet.next()) {

                String owner = resultSet.getString("owner");
                if (owner != null && owner.length() > 1) {

                    PaperAdapter.ADAPTER.setSkullOwner(skull, owner);

                }

                if (Database.hasSkullSkinColumn()) {

                    String skin = resultSet.getString("skin");
                    if (owner != null && skin != null && skin.length() > 0) {

                        PaperAdapter.ADAPTER.setSkullSkin(skull, skin);

                    }

                }

                if (Database.hasSkullMetadataColumn()) {

                    PlayerBountiesHeadCompatibility.applySkullMetadata(skull, resultSet.getString("metadata"));

                }

            }

            resultSet.close();

        } catch (Exception e) {

            e.printStackTrace();

        }

    }

}
