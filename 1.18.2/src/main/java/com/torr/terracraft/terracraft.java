package com.torr.terracraft;

import com.mojang.logging.LogUtils;
import net.minecraftforge.fml.common.Mod;
import org.slf4j.Logger;

@Mod(terracraft.MOD_ID)
public class terracraft
{
    public static final String MOD_ID = "terracraft";
    public static final Logger LOGGER = LogUtils.getLogger();

    public terracraft()
    {
        LOGGER.info("Terracraft {} bootstrap loaded (1.18.2 — gameplay not yet ported)", MOD_ID);
    }
}
