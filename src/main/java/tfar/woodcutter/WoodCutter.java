package tfar.woodcutter;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap;
import net.fabricmc.fabric.api.client.screenhandler.v1.ScreenRegistry;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.block.Material;
import net.minecraft.client.recipebook.ClientRecipeBook;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.RenderLayers;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.stat.StatFormatter;
import net.minecraft.stat.Stats;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import org.apache.logging.log4j.core.Filter;
import org.apache.logging.log4j.core.Logger;
import org.apache.logging.log4j.core.filter.CompositeFilter;
import org.apache.logging.log4j.core.filter.ThresholdFilter;

public class WoodCutter implements ModInitializer, ClientModInitializer {

	public static final String MODID = "woodcutter";

	public static final Identifier ID = new Identifier(MODID,MODID);

	public static final Identifier INTERACT_WITH_WOODCUTTER = register("interact_with_"+MODID, StatFormatter.DEFAULT);

	public static Block woodcutter;
	public static ScreenHandlerType<WoodCutterContainer> woodCutterContainer;
	public static RecipeSerializer<WoodcuttingRecipe> WOODCUTTING;

	@Override
	public void onInitialize() {
		woodcutter = Registry.register(Registry.BLOCK,ID,new WoodCutterBlock(AbstractBlock.Settings.of(Material.WOOD).strength(2.5F).sounds(BlockSoundGroup.WOOD)));
		Registry.register(Registry.ITEM,ID,new BlockItem(woodcutter,new Item.Settings().group(ItemGroup.DECORATIONS)));
		woodCutterContainer = Registry.register(Registry.SCREEN_HANDLER,ID, new ScreenHandlerType<>(WoodCutterContainer::new));
		WOODCUTTING = RecipeSerializer.register(WoodCutter.MODID +":woodcutting", new WoodcuttingRecipe.Serializer2<>(WoodcuttingRecipe::new));
	}

	private static Identifier register(String string, StatFormatter statFormatter) {
		Identifier identifier = new Identifier(string);
		Registry.register(Registry.CUSTOM_STAT, string, identifier);
		Stats.CUSTOM.getOrCreateStat(identifier, statFormatter);
		return identifier;
	}

	@Override
	public void onInitializeClient() {
		BlockRenderLayerMap.INSTANCE.putBlock(woodcutter, RenderLayer.getCutout());
		ScreenRegistry.register(woodCutterContainer,WoodCutterScreen::new);

		Filter logfilter = ((Logger) ClientRecipeBook.field_25622).getContext().getConfiguration().getFilter();
		Filter toRemove = null;
		if (logfilter instanceof CompositeFilter) {
			CompositeFilter compositeFilter = (CompositeFilter)logfilter;
			Filter[] filters  = compositeFilter.getFiltersArray();
			for (Filter filter : filters) {
				if (filter instanceof ThresholdFilter)
					toRemove = filter;
			}
		}
		((Logger) ClientRecipeBook.field_25622).getContext().removeFilter(toRemove);
		((Logger) ClientRecipeBook.field_25622).getContext().addFilter(new ShutUpRecipeBookFilter());
		System.out.println("Log spam from Recipe Book successfully neutralized");
	}
}
