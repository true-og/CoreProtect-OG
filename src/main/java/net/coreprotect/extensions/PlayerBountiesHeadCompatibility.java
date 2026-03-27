package net.coreprotect.extensions;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Skull;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

public final class PlayerBountiesHeadCompatibility {

    private static final String TARGET_KEY = "bounty_head_target";
    private static final String UUID_KEY = "bounty_head_target_uuid";
    private static final String AMOUNT_KEY = "bounty_head_diamonds";
    private static final String ITEM_MARKER = "playerbounties_head";
    private static final String SKULL_MARKER = "pbh1";

    private PlayerBountiesHeadCompatibility() {

        throw new IllegalStateException("Utility class");

    }

    public static String serializeSkullMetadata(Skull skull) {

        if (skull == null) {

            return null;

        }

        SkullData skullData = readSkullData(skull.getPersistentDataContainer());
        if (skullData == null) {

            return null;

        }

        return String.join("|", SKULL_MARKER, encode(skullData.namespace), encode(skullData.targetName),
                encode(skullData.targetUuid), Double.toString(skullData.bountyAmount));

    }

    public static void applySkullMetadata(Skull skull, String metadata) {

        if (skull == null || metadata == null || metadata.isBlank()) {

            return;

        }

        SkullData skullData = parseMetadata(metadata);
        if (skullData == null) {

            return;

        }

        applyPersistentData(skull.getPersistentDataContainer(), skullData);

    }

    public static void appendItemMetadata(ItemStack item, ItemMeta itemMeta, List<List<Map<String, Object>>> metadata) {

        if (item == null || itemMeta == null || metadata == null || item.getType() != Material.PLAYER_HEAD) {

            return;

        }

        SkullData skullData = readSkullData(itemMeta.getPersistentDataContainer());
        if (skullData == null) {

            return;

        }

        Map<String, Object> data = new HashMap<>();
        data.put("plugin", ITEM_MARKER);
        data.put("namespace", skullData.namespace);
        data.put("target", skullData.targetName);
        data.put("uuid", skullData.targetUuid);
        data.put("amount", skullData.bountyAmount);

        List<Map<String, Object>> list = new ArrayList<>();
        list.add(data);
        metadata.add(list);

    }

    public static boolean applyItemMetadata(ItemStack itemstack, Map<String, Object> mapData) {

        if (itemstack == null || mapData == null || itemstack.getType() != Material.PLAYER_HEAD) {

            return false;

        }

        Object marker = mapData.get("plugin");
        if (!ITEM_MARKER.equals(marker)) {

            return false;

        }

        String namespace = asString(mapData.get("namespace"));
        String target = asString(mapData.get("target"));
        String uuid = asString(mapData.get("uuid"));
        Double amount = asDouble(mapData.get("amount"));
        if (namespace == null || target == null || uuid == null || amount == null) {

            return true;

        }

        ItemMeta itemMeta = itemstack.getItemMeta();
        if (itemMeta == null) {

            return true;

        }

        applyPersistentData(itemMeta.getPersistentDataContainer(), new SkullData(namespace, target, uuid, amount));
        itemstack.setItemMeta(itemMeta);
        return true;

    }

    private static SkullData readSkullData(PersistentDataContainer container) {

        if (container == null) {

            return null;

        }

        NamespacedKey targetKey = null;
        NamespacedKey uuidKey = null;
        NamespacedKey amountKey = null;

        for (NamespacedKey key : container.getKeys()) {

            if (TARGET_KEY.equals(key.getKey())) {

                targetKey = key;

            } else if (UUID_KEY.equals(key.getKey())) {

                uuidKey = key;

            } else if (AMOUNT_KEY.equals(key.getKey())) {

                amountKey = key;

            }

        }

        if (targetKey == null || uuidKey == null || amountKey == null) {

            return null;

        }

        String target = container.get(targetKey, PersistentDataType.STRING);
        String uuid = container.get(uuidKey, PersistentDataType.STRING);
        Double amount = container.get(amountKey, PersistentDataType.DOUBLE);
        if (target == null || uuid == null || amount == null) {

            return null;

        }

        return new SkullData(targetKey.getNamespace(), target, uuid, amount);

    }

    private static void applyPersistentData(PersistentDataContainer container, SkullData skullData) {

        NamespacedKey targetKey = new NamespacedKey(skullData.namespace, TARGET_KEY);
        NamespacedKey uuidKey = new NamespacedKey(skullData.namespace, UUID_KEY);
        NamespacedKey amountKey = new NamespacedKey(skullData.namespace, AMOUNT_KEY);

        container.set(targetKey, PersistentDataType.STRING, skullData.targetName);
        container.set(uuidKey, PersistentDataType.STRING, skullData.targetUuid);
        container.set(amountKey, PersistentDataType.DOUBLE, skullData.bountyAmount);

    }

    private static SkullData parseMetadata(String metadata) {

        String[] parts = metadata.split("\\|", -1);
        if (parts.length != 5 || !SKULL_MARKER.equals(parts[0])) {

            return null;

        }

        try {

            return new SkullData(decode(parts[1]), decode(parts[2]), decode(parts[3]), Double.parseDouble(parts[4]));

        } catch (Exception e) {

            return null;

        }

    }

    private static String encode(String value) {

        return Base64.getEncoder().encodeToString(value.getBytes(StandardCharsets.UTF_8));

    }

    private static String decode(String value) {

        return new String(Base64.getDecoder().decode(value), StandardCharsets.UTF_8);

    }

    private static String asString(Object value) {

        if (value == null) {

            return null;

        }

        return String.valueOf(value);

    }

    private static Double asDouble(Object value) {

        if (value instanceof Number) {

            return ((Number) value).doubleValue();

        }

        if (value == null) {

            return null;

        }

        try {

            return Double.parseDouble(String.valueOf(value));

        } catch (NumberFormatException e) {

            return null;

        }

    }

    private record SkullData(String namespace, String targetName, String targetUuid, double bountyAmount) {
    }

}
