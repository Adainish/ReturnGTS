package io.github.adainish.returngts.obj.player;

import com.pixelmonmod.pixelmon.api.pokemon.Pokemon;
import net.minecraft.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class RetrievalStorage
{
    public List<Pokemon> pokemon = new ArrayList<>();
    public List<ItemStack> itemStacks = new ArrayList<>();

    public RetrievalStorage()
    {

    }

    public boolean hasPokemonToClaim()
    {
        return !pokemon.isEmpty();
    }

    public boolean hasItemsToClaim()
    {
        return !itemStacks.isEmpty();
    }


}
