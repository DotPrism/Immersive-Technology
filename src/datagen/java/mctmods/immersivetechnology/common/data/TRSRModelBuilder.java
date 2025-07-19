package mctmods.immersivetechnology.common.data;

import blusunrize.immersiveengineering.data.models.TransformationMap;
import com.google.common.io.CharStreams;
import com.google.gson.JsonObject;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.resources.Resource;
import net.minecraftforge.client.model.generators.ModelBuilder;
import net.minecraftforge.common.data.ExistingFileHelper;

import javax.annotation.Nonnull;
import java.io.IOException;

public class TRSRModelBuilder extends ModelBuilder<TRSRModelBuilder>
{
    private final TransformationMap transforms = new TransformationMap();

    public TRSRModelBuilder(ResourceLocation outputLocation, ExistingFileHelper existingFileHelper){
        super(outputLocation, existingFileHelper);
    }

    public TRSRModelBuilder transforms(ResourceLocation source){
        Resource transformFile;
        try{
            transformFile = existingFileHelper.getResource(source, PackType.CLIENT_RESOURCES, ".json", "transformations");
            String jsonString = CharStreams.toString(transformFile.openAsReader());
            transforms.addFromJson(jsonString);
            return this;
        }catch(IOException e){
            throw new RuntimeException("While loading transforms from " + source, e);
        }
    }

    @Nonnull
    @Override
    public JsonObject toJson(){
        JsonObject ret = super.toJson();
        JsonObject transformJson = transforms.toJson();
        if(!transformJson.entrySet().isEmpty())
            ret.add("transform", transformJson);
        return ret;
    }
}
