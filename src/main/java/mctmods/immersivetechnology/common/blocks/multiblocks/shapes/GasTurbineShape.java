package mctmods.immersivetechnology.common.blocks.multiblocks.shapes;

import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.AABB;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class GasTurbineShape extends GenericShape {
    public static final GasTurbineShape GETTER = new GasTurbineShape();

    @NotNull
    @Override
    protected List<AABB> getShape(BlockPos posInMultiblock) {
        final int bX = posInMultiblock.getX();
        final int bY = posInMultiblock.getY();
        final int bZ = posInMultiblock.getZ();

        List<AABB> main = new ArrayList<>();

        if (bY == 0) {
            if (bZ == 0) {
                if (bX == 1) {
                    main.add(new AABB(0, 0, 0, 1, 1, 1)); // Add full shape for output port hitbox/connection
                }
            }
            if (bX == 0 && bZ == 1) {
                main.add(new AABB(0, 0, 0, 1, 1, 1));
            }
            if (bX == 1 && bZ == 1) {
                main.add(new AABB(0, 0, 0, 1, 1, 1));
            }
            if (bX == 2 && bZ == 1) {
                main.add(new AABB(0, 0, 0, 1, 1, 1));
            }
            if (bX == 0 && bZ == 2) {
                main.add(new AABB(0, 0, 0, 1, 1, 0.5));
                main.add(new AABB(0.25, 0, 0.5, 1, 1, 1));
            }
            if (bX == 1 && bZ == 2) {
                main.add(new AABB(0, 0, 0, 1, 1, 1));
            }
            if (bX == 2 && bZ == 2) {
                main.add(new AABB(0, 0, 0, 1, 1, 0.5));
                main.add(new AABB(0, 0, 0.5, 0.75, 1, 1));
            }
            if (bX == 0 && bZ == 3) {
                main.add(new AABB(0.25, 0, 0, 1, 1, 1));
            }
            if (bX == 1 && bZ == 3) {
                main.add(new AABB(0, 0, 0, 1, 1, 1));
            }
            if (bX == 2 && bZ == 3) {
                main.add(new AABB(0, 0, 0, 0.75, 1, 1));
            }
            if (bX == 0 && bZ == 4) {
                main.add(new AABB(0.25, 0, 0, 1, 1, 0.5));
                main.add(new AABB(0, 0, 0.5, 1, 1, 1));
            }
            if (bX == 1 && bZ == 4) {
                main.add(new AABB(0, 0, 0, 1, 1, 1));
            }
            if (bX == 2 && bZ == 4) {
                main.add(new AABB(0, 0, 0.5, 1, 1, 1));
                main.add(new AABB(0, 0, 0, 0.75, 1, 1));
            }
            if (bX == 0 && bZ == 5) {
                main.add(new AABB(0, 0, 0, 1, 1, 1));
            }
            if (bX == 1 && bZ == 5) {
                main.add(new AABB(0, 0, 0, 1, 1, 1));
            }
            if (bX == 2 && bZ == 5) {
                main.add(new AABB(0, 0, 0, 1, 1, 1));
            }
            if (bX == 0 && bZ == 6) {
                main.add(new AABB(0.5, 0, 0, 1, 1, 1));
            }
            if (bX == 1 && bZ == 6) {
                main.add(new AABB(0, 0, 0, 1, 1, 1));
            }
            if (bX == 2 && bZ == 6) {
                main.add(new AABB(0, 0, 0, 0.5, 1, 1));
            }
            if (bX == 0 && bZ == 7) {
                main.add(new AABB(0, 0, 0, 1, 1, 1));
            }
            if (bX == 1 && bZ == 7) {
                main.add(new AABB(0, 0, 0, 1, 1, 1));
            }
            if (bX == 2 && bZ == 7) {
                main.add(new AABB(0, 0, 0, 1, 1, 1));
            }
        }

        return main;
    }
}
