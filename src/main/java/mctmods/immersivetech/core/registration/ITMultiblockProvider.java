package mctmods.immersivetech.core.registration;

import blusunrize.immersiveengineering.api.multiblocks.blocks.MultiblockRegistration;
import mctmods.immersivetech.common.blocks.multiblocks.logic.*;

public class ITMultiblockProvider
{
    // Example Multiblock registration
    //public static final MultiblockRegistration<?> EXAMPLE_MULTIBLOCK = ITRegistrationHolder.registerMultiblock("example", new ExampleMBLogic(), () -> ITRegistrationHolder.getMBTemplate.apply("example"));

    public static final MultiblockRegistration<ITBoilerLogic.State> BOILER =
            ITRegistrationHolder.metal(new ITBoilerLogic(), "boiler")
            .structure(() -> ITRegistrationHolder.getMBTemplate.apply("boiler"))
            .gui(ITMenuTypes.BOILER_MENU)
            .redstone(s -> s.rsState, ITBoilerLogic.REDSTONE_POS)
            .build();
    public static final MultiblockRegistration<ITAlternatorLogic.State> ALTERNATOR =
            ITRegistrationHolder.metal(new ITAlternatorLogic(), "alternator")
                    .structure(() -> ITRegistrationHolder.getMBTemplate.apply("alternator"))
                    .build();
   public static final MultiblockRegistration<ITSteamTurbineLogic.State> STEAM_TURBINE =
            ITRegistrationHolder.metal(new ITSteamTurbineLogic(), "steam_turbine")
                    .structure(() -> ITRegistrationHolder.getMBTemplate.apply("steam_turbine"))
                    .redstone(s -> s.rsState, ITSteamTurbineLogic.REDSTONE_POS)
                    .build();
    public static final MultiblockRegistration<ITGasTurbineLogic.State> GAS_TURBINE =
            ITRegistrationHolder.metal(new ITGasTurbineLogic(), "gas_turbine")
                    .structure(() -> ITRegistrationHolder.getMBTemplate.apply("gas_turbine"))
                    .redstone(s -> s.rsState, ITGasTurbineLogic.REDSTONE_POS)
                    .build();
    public static final MultiblockRegistration<ITAdvancedCokeOvenLogic.State> ADV_COKE_OVEN =
            ITRegistrationHolder.stone(new ITAdvancedCokeOvenLogic(), "coke_oven_advanced", false)
                .structure(() -> ITRegistrationHolder.getMBTemplate.apply("coke_oven_advanced"))
                    .gui(ITMenuTypes.ADVANCED_COKE_OVEN_MENU)
                        .build();

    public static void forceClassLoad()
    {}
}
