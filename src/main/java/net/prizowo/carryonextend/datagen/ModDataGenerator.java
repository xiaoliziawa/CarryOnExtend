package net.prizowo.carryonextend.datagen;

import net.minecraft.core.HolderLookup;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.PackOutput;
import net.minecraftforge.data.event.GatherDataEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.prizowo.carryonextend.CarryOnExtend;

import java.util.concurrent.CompletableFuture;

@Mod.EventBusSubscriber(modid = CarryOnExtend.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ModDataGenerator {
    
    @SubscribeEvent
    public static void gatherData(GatherDataEvent event) {
        DataGenerator generator = event.getGenerator();
        PackOutput packOutput = generator.getPackOutput();
        CompletableFuture<HolderLookup.Provider> lookupProvider = event.getLookupProvider();

        generator.addProvider(event.includeServer(), 
            new ModAdvancementProvider(packOutput, lookupProvider, event.getExistingFileHelper())
        );

        generator.addProvider(event.includeServer(),
            new ModBlockTagsProvider(packOutput, lookupProvider, event.getExistingFileHelper())
        );
    }
}