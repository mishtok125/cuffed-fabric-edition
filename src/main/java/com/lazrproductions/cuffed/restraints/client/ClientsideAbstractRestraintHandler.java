package com.lazrproductions.cuffed.restraints.client;

import com.lazrproductions.cuffed.restraints.base.AbstractRestraint;
import com.lazrproductions.cuffed.restraints.base.IBreakableRestraint;
import com.mojang.blaze3d.platform.Window;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;

public abstract class ClientsideAbstractRestraintHandler {
    public final AbstractRestraint parent;
    
    public ClientsideAbstractRestraintHandler(AbstractRestraint parent) {
        this.parent = parent;
    }

    /** Called each frame only on the client to render overlays and such. */
    public abstract void renderOverlay(Player player, GuiGraphicsExtractor graphics, float partialTick, Window window);
    
    
    public abstract Class<? extends HumanoidModel<?>> getRenderedModel();
    public abstract ModelLayerLocation getRenderedModelLayer();
    public abstract Identifier getRenderedModelTexture();


    protected int clientSidedDurability = 100;
    /** Called on the client any time the client presses any key. */
    public void onKeyInput(Player player, int keyCode, int action) {
        Minecraft instance = Minecraft.getInstance();
        
        if(action == 1) {
            if(parent instanceof IBreakableRestraint breakable) {
                breakable.attemptToBreak(player, keyCode, action, instance.options);
            }
        }
    }
    /** Called on the client any time the client presses any mouse button. */
    public void onMouseInput(Player player, int keyCode, int action) {
        Minecraft instance = Minecraft.getInstance();
        
        if(action == 1)
            if(parent instanceof IBreakableRestraint breakable)
                breakable.attemptToBreak(player, keyCode, action, instance.options);
    }
}
