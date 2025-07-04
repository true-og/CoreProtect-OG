package net.coreprotect.paper;

import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.block.Sign;

public class Paper_v1_17 extends PaperHandler implements PaperInterface {

    @Override
    public String getLine(Sign sign, int line) {
        if (line >= 4) {
            return "";
        }

        // https://docs.adventure.kyori.net/serializer/
        return LegacyComponentSerializer.legacySection().serialize(sign.line(line));
    }
}
