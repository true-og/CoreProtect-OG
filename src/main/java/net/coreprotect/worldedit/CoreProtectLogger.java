package net.coreprotect.worldedit;

import com.sk89q.worldedit.WorldEditException;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.bukkit.BukkitWorld;
import com.sk89q.worldedit.extension.platform.Actor;
import com.sk89q.worldedit.extent.AbstractDelegateExtent;
import com.sk89q.worldedit.extent.Extent;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.world.World;
import com.sk89q.worldedit.world.block.BaseBlock;
import com.sk89q.worldedit.world.block.BlockState;
import com.sk89q.worldedit.world.block.BlockStateHolder;
import net.coreprotect.config.Config;
import net.coreprotect.utility.Util;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

public class CoreProtectLogger extends AbstractDelegateExtent {

    private final Actor eventActor;
    private final World eventWorld;
    private final Extent eventExtent;

    protected CoreProtectLogger(Actor actor, World world, Extent extent) {
        super(extent);
        this.eventActor = actor;
        this.eventWorld = world;
        this.eventExtent = extent;
    }

    @Override
    public <T extends BlockStateHolder<T>> boolean setBlock(BlockVector3 position, T block) throws WorldEditException {
        if (!(eventWorld instanceof BukkitWorld)) {
            return eventExtent.setBlock(position, block);
        }
        org.bukkit.World world = ((BukkitWorld) eventWorld).getWorld();
        if (!Config.getConfig(world).WORLDEDIT) {
            return eventExtent.setBlock(position, block);
        }

        BlockState oldBlock = eventExtent.getBlock(position);
        Material oldType = BukkitAdapter.adapt(oldBlock.getBlockType());
        Location location = new Location(world, position.x(), position.y(), position.z());
        BaseBlock baseBlock = WorldEditLogger.getBaseBlock(eventExtent, position, location, oldType, oldBlock);

        // No clear way to get container content data from within the WorldEdit API
        // Data may be available by converting oldBlock.toBaseBlock().getNbtData()
        // e.g. BaseBlock block = eventWorld.getBlock(position);
        ItemStack[] containerData = Util.getContainerContents(oldType, null, location);

        if (eventExtent.setBlock(position, block)) {
            WorldEditLogger.postProcess(
                    eventExtent, eventActor, position, location, block, baseBlock, oldType, oldBlock, containerData);
            return true;
        }

        return false;
    }
}
