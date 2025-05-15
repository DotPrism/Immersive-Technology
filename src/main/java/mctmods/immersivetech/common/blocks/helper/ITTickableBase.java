package mctmods.immersivetech.common.blocks.helper;

public interface ITTickableBase {
    default boolean canTickAny() {
        return true;
    }
}
