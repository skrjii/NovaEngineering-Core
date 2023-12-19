package github.kasuminova.novaeng.common.hypernet.proc.server.module;

import crafttweaker.annotations.ZenRegister;
import github.kasuminova.novaeng.common.hypernet.proc.server.ModularServer;
import github.kasuminova.novaeng.common.hypernet.proc.server.module.base.ServerModuleBase;
import stanhebben.zenscript.annotations.ZenClass;
import stanhebben.zenscript.annotations.ZenMethod;

@ZenRegister
@ZenClass("novaeng.hypernet.server.module.ModuleCalculateCardHeatRadiator")
public class ModuleCalculateCardHeatRadiator extends ModuleHeatRadiator {

    public ModuleCalculateCardHeatRadiator(final ModularServer server,final ServerModuleBase<?> moduleBase, final int moduleAmount) {
        super(server, moduleBase, moduleAmount);
    }

    @ZenMethod
    public static ModuleCalculateCardHeatRadiator cast(ServerModule module) {
        return module instanceof ModuleCalculateCardHeatRadiator ? (ModuleCalculateCardHeatRadiator) module : null;
    }

}