package com.mrailouis.kosovoclient.features.impl.visuals;

import com.mrailouis.kosovoclient.features.BooleanSetting;
import com.mrailouis.kosovoclient.features.Category;
import com.mrailouis.kosovoclient.features.ColorSetting;
import com.mrailouis.kosovoclient.features.Module;
import com.mrailouis.kosovoclient.features.NumberSetting;
import lombok.Getter;
import net.minecraft.block.Block;
import net.minecraft.block.BlockBed;
import net.minecraft.block.BlockChest;
import net.minecraft.block.BlockDoor;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderGlobal;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityChest;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.world.World;
import net.minecraftforge.client.event.DrawBlockHighlightEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.lwjgl.opengl.GL11;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.TreeSet;

@Getter
public class BlockOverlay extends Module {
    private static final BlockOverlay INSTANCE = new BlockOverlay();

    private final BooleanSetting fill = new BooleanSetting("Fill", "Render filled block highlight.", true);
    private final ColorSetting fillColor = new ColorSetting("Fill Color", "Color and opacity of the fill.", 0x40FF3333);
    private final BooleanSetting fillDepth = new BooleanSetting("Fill Depth", "Enable depth test for fill.", true);

    private final BooleanSetting outline = new BooleanSetting("Outline", "Render block outline.", true);
    private final ColorSetting outlineColor = new ColorSetting("Outline Color", "Color and opacity of the outline.", 0xFFFF3333);
    private final NumberSetting outlineWidth = new NumberSetting("Outline Width", "Thickness of the outline.", 2.0, 0.5, 5.0, 0.5);
    private final BooleanSetting outlineDepth = new BooleanSetting("Outline Depth", "Enable depth test for outline.", true);

    public static BlockOverlay getInstance() {
        return INSTANCE;
    }

    private BlockOverlay() {
        super("Block Overlay", "Customizable block selection overlay and outline.", Category.VISUALS, true);
        registerSetting(fill);
        registerSetting(fillColor);
        registerSetting(fillDepth);
        registerSetting(outline);
        registerSetting(outlineColor);
        registerSetting(outlineWidth);
        registerSetting(outlineDepth);
    }

    @SubscribeEvent
    public void onDrawBlockHighlight(DrawBlockHighlightEvent event) {
        if (!isEnabled()) {
            return;
        }

        MovingObjectPosition target = event.target;
        if (target == null || target.typeOfHit != MovingObjectPosition.MovingObjectType.BLOCK) {
            return;
        }

        event.setCanceled(true);

        Minecraft mc = Minecraft.getMinecraft();
        World world = mc.theWorld;
        EntityPlayerSP player = mc.thePlayer;
        if (world == null || player == null) {
            return;
        }

        BlockPos pos = target.getBlockPos();
        IBlockState state = world.getBlockState(pos);
        Block block = state.getBlock();
        if (block.getMaterial() == Material.air || !world.getWorldBorder().contains(pos)) {
            return;
        }

        float partialTicks = event.partialTicks;
        double interpX = player.lastTickPosX + (player.posX - player.lastTickPosX) * partialTicks;
        double interpY = player.lastTickPosY + (player.posY - player.lastTickPosY) * partialTicks;
        double interpZ = player.lastTickPosZ + (player.posZ - player.lastTickPosZ) * partialTicks;

        List<AxisAlignedBB> boxes = getBlockBoundingBoxes(world, pos, state, block, player);
        if (boxes.isEmpty()) {
            return;
        }

        GlStateManager.pushMatrix();
        GlStateManager.enableBlend();
        GlStateManager.tryBlendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, GL11.GL_ONE, GL11.GL_ZERO);
        GlStateManager.disableTexture2D();
        GlStateManager.disableAlpha();
        GlStateManager.disableLighting();

        if (this.fill.isEnabled()) {
            if (this.fillDepth.isEnabled()) {
                GlStateManager.enableDepth();
                GlStateManager.depthMask(true);
            } else {
                GlStateManager.disableDepth();
                GlStateManager.depthMask(false);
            }

            GlStateManager.color(this.fillColor.getRed(), this.fillColor.getGreen(), this.fillColor.getBlue(), this.fillColor.getAlpha());

            for (AxisAlignedBB box : boxes) {
                AxisAlignedBB renderedBox = box.expand(0.002, 0.002, 0.002).offset(-interpX, -interpY, -interpZ);
                drawFilledBox(renderedBox);
            }
        }

        if (this.outline.isEnabled()) {
            if (this.outlineDepth.isEnabled()) {
                GlStateManager.enableDepth();
                GlStateManager.depthMask(true);
            } else {
                GlStateManager.disableDepth();
                GlStateManager.depthMask(false);
            }

            GL11.glLineWidth(this.outlineWidth.getValue().floatValue());
            GlStateManager.color(this.outlineColor.getRed(), this.outlineColor.getGreen(), this.outlineColor.getBlue(), this.outlineColor.getAlpha());

            if (boxes.size() == 1) {
                AxisAlignedBB renderedBox = boxes.get(0).expand(0.002, 0.002, 0.002).offset(-interpX, -interpY, -interpZ);
                RenderGlobal.drawSelectionBoundingBox(renderedBox);
            } else {
                List<AxisAlignedBB> offsetBoxes = new ArrayList<AxisAlignedBB>();
                for (AxisAlignedBB box : boxes) {
                    offsetBoxes.add(box.expand(0.002, 0.002, 0.002).offset(-interpX, -interpY, -interpZ));
                }
                drawCombinedOutline(offsetBoxes);
            }
        }

        GlStateManager.enableDepth();
        GlStateManager.depthMask(true);
        GlStateManager.enableAlpha();
        GlStateManager.enableTexture2D();
        GlStateManager.disableBlend();
        GlStateManager.popMatrix();
    }

    private List<AxisAlignedBB> getBlockBoundingBoxes(World world, BlockPos pos, IBlockState state, Block block, EntityPlayerSP player) {
        List<AxisAlignedBB> result = new ArrayList<AxisAlignedBB>();

        if (block instanceof BlockBed) {
            BlockBed.EnumPartType part = state.getValue(BlockBed.PART);
            EnumFacing facing = state.getValue(BlockBed.FACING);

            BlockPos otherPos = (part == BlockBed.EnumPartType.HEAD) ? pos.offset(facing.getOpposite()) : pos.offset(facing);
            IBlockState otherState = world.getBlockState(otherPos);

            BlockPos footPos = (part == BlockBed.EnumPartType.HEAD) ? otherPos : pos;
            BlockPos headPos = (part == BlockBed.EnumPartType.HEAD) ? pos : otherPos;

            if (otherState.getBlock() == block) {
                double minX = Math.min(footPos.getX(), headPos.getX());
                double minY = Math.min(footPos.getY(), headPos.getY());
                double minZ = Math.min(footPos.getZ(), headPos.getZ());
                double maxX = Math.max(footPos.getX() + 1, headPos.getX() + 1);
                double maxY = Math.max(footPos.getY() + 0.5625, headPos.getY() + 0.5625);
                double maxZ = Math.max(footPos.getZ() + 1, headPos.getZ() + 1);

                result.add(new AxisAlignedBB(minX, minY, minZ, maxX, maxY, maxZ));
                return result;
            }
        }

        if (block instanceof BlockDoor) {
            BlockDoor.EnumDoorHalf half = state.getValue(BlockDoor.HALF);
            BlockPos bottomPos = (half == BlockDoor.EnumDoorHalf.UPPER) ? pos.down() : pos;
            BlockPos topPos = (half == BlockDoor.EnumDoorHalf.UPPER) ? pos : pos.up();

            IBlockState bottomState = world.getBlockState(bottomPos);
            IBlockState topState = world.getBlockState(topPos);

            if (bottomState.getBlock() == block && topState.getBlock() == block) {
                block.setBlockBoundsBasedOnState(world, bottomPos);
                AxisAlignedBB bottomBox = block.getSelectedBoundingBox(world, bottomPos);
                block.setBlockBoundsBasedOnState(world, topPos);
                AxisAlignedBB topBox = block.getSelectedBoundingBox(world, topPos);

                if (bottomBox != null && topBox != null) {
                    result.add(new AxisAlignedBB(
                            Math.min(bottomBox.minX, topBox.minX),
                            Math.min(bottomBox.minY, topBox.minY),
                            Math.min(bottomBox.minZ, topBox.minZ),
                            Math.max(bottomBox.maxX, topBox.maxX),
                            Math.max(bottomBox.maxY, topBox.maxY),
                            Math.max(bottomBox.maxZ, topBox.maxZ)
                    ));
                    return result;
                }
            }
        }

        if (block instanceof BlockChest) {
            TileEntity te = world.getTileEntity(pos);
            if (te instanceof TileEntityChest) {
                TileEntityChest chest = (TileEntityChest) te;
                double minX = pos.getX() + 0.0625;
                double minY = pos.getY();
                double minZ = pos.getZ() + 0.0625;
                double maxX = pos.getX() + 0.9375;
                double maxY = pos.getY() + 0.875;
                double maxZ = pos.getZ() + 0.9375;

                if (chest.adjacentChestXNeg != null) {
                    minX = pos.getX() - 1 + 0.0625;
                } else if (chest.adjacentChestXPos != null) {
                    maxX = pos.getX() + 2 - 0.0625;
                } else if (chest.adjacentChestZNeg != null) {
                    minZ = pos.getZ() - 1 + 0.0625;
                } else if (chest.adjacentChestZPos != null) {
                    maxZ = pos.getZ() + 2 - 0.0625;
                }

                result.add(new AxisAlignedBB(minX, minY, minZ, maxX, maxY, maxZ));
                return result;
            }
        }

        block.setBlockBoundsBasedOnState(world, pos);
        AxisAlignedBB mask = new AxisAlignedBB(pos.getX(), pos.getY(), pos.getZ(), pos.getX() + 1, pos.getY() + 1, pos.getZ() + 1);
        block.addCollisionBoxesToList(world, pos, state, mask, result, player);

        if (result.isEmpty()) {
            AxisAlignedBB selected = block.getSelectedBoundingBox(world, pos);
            if (selected != null) {
                result.add(selected);
            }
        }

        return result;
    }

    private void drawCombinedOutline(List<AxisAlignedBB> boxes) {
        TreeSet<Double> xs = new TreeSet<Double>();
        TreeSet<Double> ys = new TreeSet<Double>();
        TreeSet<Double> zs = new TreeSet<Double>();

        for (AxisAlignedBB box : boxes) {
            xs.add(box.minX);
            xs.add(box.maxX);
            ys.add(box.minY);
            ys.add(box.maxY);
            zs.add(box.minZ);
            zs.add(box.maxZ);
        }

        List<Double> xList = new ArrayList<Double>(xs);
        List<Double> yList = new ArrayList<Double>(ys);
        List<Double> zList = new ArrayList<Double>(zs);

        double eps = 0.0005;
        List<double[]> lines = new ArrayList<double[]>();

        for (int yIdx = 0; yIdx < yList.size(); yIdx++) {
            double y = yList.get(yIdx);
            for (int zIdx = 0; zIdx < zList.size(); zIdx++) {
                double z = zList.get(zIdx);
                for (int xIdx = 0; xIdx < xList.size() - 1; xIdx++) {
                    double x1 = xList.get(xIdx);
                    double x2 = xList.get(xIdx + 1);
                    double xMid = (x1 + x2) * 0.5;

                    boolean q1 = isInside(boxes, xMid, y + eps, z + eps);
                    boolean q2 = isInside(boxes, xMid, y + eps, z - eps);
                    boolean q3 = isInside(boxes, xMid, y - eps, z + eps);
                    boolean q4 = isInside(boxes, xMid, y - eps, z - eps);

                    if (isBoundaryEdge(q1, q2, q3, q4)) {
                        lines.add(new double[]{x1, y, z, x2, y, z});
                    }
                }
            }
        }

        for (int xIdx = 0; xIdx < xList.size(); xIdx++) {
            double x = xList.get(xIdx);
            for (int zIdx = 0; zIdx < zList.size(); zIdx++) {
                double z = zList.get(zIdx);
                for (int yIdx = 0; yIdx < yList.size() - 1; yIdx++) {
                    double y1 = yList.get(yIdx);
                    double y2 = yList.get(yIdx + 1);
                    double yMid = (y1 + y2) * 0.5;

                    boolean q1 = isInside(boxes, x + eps, yMid, z + eps);
                    boolean q2 = isInside(boxes, x + eps, yMid, z - eps);
                    boolean q3 = isInside(boxes, x - eps, yMid, z + eps);
                    boolean q4 = isInside(boxes, x - eps, yMid, z - eps);

                    if (isBoundaryEdge(q1, q2, q3, q4)) {
                        lines.add(new double[]{x, y1, z, x, y2, z});
                    }
                }
            }
        }

        for (int xIdx = 0; xIdx < xList.size(); xIdx++) {
            double x = xList.get(xIdx);
            for (int yIdx = 0; yIdx < yList.size(); yIdx++) {
                double y = yList.get(yIdx);
                for (int zIdx = 0; zIdx < zList.size() - 1; zIdx++) {
                    double z1 = zList.get(zIdx);
                    double z2 = zList.get(zIdx + 1);
                    double zMid = (z1 + z2) * 0.5;

                    boolean q1 = isInside(boxes, x + eps, y + eps, zMid);
                    boolean q2 = isInside(boxes, x + eps, y - eps, zMid);
                    boolean q3 = isInside(boxes, x - eps, y + eps, zMid);
                    boolean q4 = isInside(boxes, x - eps, y - eps, zMid);

                    if (isBoundaryEdge(q1, q2, q3, q4)) {
                        lines.add(new double[]{x, y, z1, x, y, z2});
                    }
                }
            }
        }

        Tessellator tessellator = Tessellator.getInstance();
        WorldRenderer worldrenderer = tessellator.getWorldRenderer();
        worldrenderer.begin(GL11.GL_LINES, DefaultVertexFormats.POSITION);

        for (double[] line : lines) {
            worldrenderer.pos(line[0], line[1], line[2]).endVertex();
            worldrenderer.pos(line[3], line[4], line[5]).endVertex();
        }

        tessellator.draw();
    }

    private static boolean isInside(List<AxisAlignedBB> boxes, double x, double y, double z) {
        for (AxisAlignedBB b : boxes) {
            if (x >= b.minX && x <= b.maxX && y >= b.minY && y <= b.maxY && z >= b.minZ && z <= b.maxZ) {
                return true;
            }
        }
        return false;
    }

    private static boolean isBoundaryEdge(boolean q1, boolean q2, boolean q3, boolean q4) {
        int count = (q1 ? 1 : 0) + (q2 ? 1 : 0) + (q3 ? 1 : 0) + (q4 ? 1 : 0);
        if (count == 1 || count == 3) {
            return true;
        }
        if (count == 2) {
            return (q1 && q4 && !q2 && !q3) || (q2 && q3 && !q1 && !q4);
        }
        return false;
    }

    private void drawFilledBox(AxisAlignedBB bb) {
        Tessellator tessellator = Tessellator.getInstance();
        WorldRenderer worldrenderer = tessellator.getWorldRenderer();
        worldrenderer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION);

        worldrenderer.pos(bb.minX, bb.minY, bb.minZ).endVertex();
        worldrenderer.pos(bb.maxX, bb.minY, bb.minZ).endVertex();
        worldrenderer.pos(bb.maxX, bb.minY, bb.maxZ).endVertex();
        worldrenderer.pos(bb.minX, bb.minY, bb.maxZ).endVertex();

        worldrenderer.pos(bb.minX, bb.maxY, bb.minZ).endVertex();
        worldrenderer.pos(bb.minX, bb.maxY, bb.maxZ).endVertex();
        worldrenderer.pos(bb.maxX, bb.maxY, bb.maxZ).endVertex();
        worldrenderer.pos(bb.maxX, bb.maxY, bb.minZ).endVertex();

        worldrenderer.pos(bb.minX, bb.minY, bb.minZ).endVertex();
        worldrenderer.pos(bb.minX, bb.maxY, bb.minZ).endVertex();
        worldrenderer.pos(bb.maxX, bb.maxY, bb.minZ).endVertex();
        worldrenderer.pos(bb.maxX, bb.minY, bb.minZ).endVertex();

        worldrenderer.pos(bb.maxX, bb.minY, bb.minZ).endVertex();
        worldrenderer.pos(bb.maxX, bb.maxY, bb.minZ).endVertex();
        worldrenderer.pos(bb.maxX, bb.maxY, bb.maxZ).endVertex();
        worldrenderer.pos(bb.maxX, bb.minY, bb.maxZ).endVertex();

        worldrenderer.pos(bb.minX, bb.minY, bb.maxZ).endVertex();
        worldrenderer.pos(bb.maxX, bb.minY, bb.maxZ).endVertex();
        worldrenderer.pos(bb.maxX, bb.maxY, bb.maxZ).endVertex();
        worldrenderer.pos(bb.minX, bb.maxY, bb.maxZ).endVertex();

        worldrenderer.pos(bb.minX, bb.minY, bb.minZ).endVertex();
        worldrenderer.pos(bb.minX, bb.minY, bb.maxZ).endVertex();
        worldrenderer.pos(bb.minX, bb.maxY, bb.maxZ).endVertex();
        worldrenderer.pos(bb.minX, bb.maxY, bb.minZ).endVertex();

        tessellator.draw();
    }
}
