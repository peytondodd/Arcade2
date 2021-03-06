/*
 * Copyright 2018 Aleksander Jagiełło
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package pl.themolka.arcade.objective.flag;

import org.bukkit.Material;
import org.bukkit.block.Banner;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BannerMeta;
import org.bukkit.material.MaterialData;

public final class BannerUtils {
    private BannerUtils() {
    }

    public static boolean isBanner(Block block) {
        return isBanner(block.getType());
    }

    public static boolean isBanner(BlockState blockState) {
        return isBanner(blockState.getBlock());
    }

    public static boolean isBanner(ItemStack item) {
        return isBanner(item.getType());
    }

    public static boolean isBanner(Material material) {
        return  material.equals(Material.BANNER) ||
                material.equals(Material.STANDING_BANNER) ||
                material.equals(Material.WALL_BANNER);
    }

    public static boolean isBanner(MaterialData data) {
        return isBanner(data.getItemType());
    }

    public static BannerMeta meta(ItemStack item) {
        if (isBanner(item)) {
            return (BannerMeta) item.getItemMeta();
        }

        return null;
    }

    public static Banner toBlock(Banner block, BannerMeta meta) {
        block.setBaseColor(meta.getBaseColor());
        block.setPatterns(meta.getPatterns());
        return block;
    }

    public static ItemStack toItem(ItemStack item, Banner block) {
        BannerMeta meta = meta(item);
        if (meta != null) {
            item.setItemMeta(toMeta(meta, block));
        }

        return item;
    }

    public static BannerMeta toMeta(BannerMeta meta, Banner block) {
        meta.setBaseColor(block.getBaseColor());
        meta.setPatterns(block.getPatterns());
        return meta;
    }
}
