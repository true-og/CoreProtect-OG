package net.coreprotect.command;

import com.sk89q.worldedit.LocalSession;
import com.sk89q.worldedit.bukkit.WorldEditPlugin;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.regions.Region;
import com.sk89q.worldedit.world.World;
import net.coreprotect.worldedit.WorldEditLogger;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class WorldEditHandler {

    protected static Integer[] runWorldEditCommand(CommandSender user) {
        Integer[] result = null;
        try {
            WorldEditPlugin worldEdit = WorldEditLogger.getWorldEdit(user.getServer());
            if (worldEdit != null && user instanceof Player) {
                LocalSession session = worldEdit.getSession((Player) user);
                World world = session.getSelectionWorld();
                if (world != null) {
                    Region region = session.getSelection(world);
                    if (region != null
                            && world.getName().equals(((Player) user).getWorld().getName())) {
                        BlockVector3 block = region.getMinimumPoint();
                        int x = block.x();
                        int y = block.y();
                        int z = block.z();
                        int width = region.getWidth();
                        int height = region.getHeight();
                        int length = region.getLength();
                        int max = width;
                        if (height > max) {
                            max = height;
                        }
                        if (length > max) {
                            max = length;
                        }
                        int xMin = x;
                        int xMax = x + (width - 1);
                        int yMin = y;
                        int yMax = y + (height - 1);
                        int zMin = z;
                        int zMax = z + (length - 1);
                        result = new Integer[] {max, xMin, xMax, yMin, yMax, zMin, zMax, 1};
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }
}
