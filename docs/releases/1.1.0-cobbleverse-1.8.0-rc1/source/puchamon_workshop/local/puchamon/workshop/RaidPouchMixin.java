package local.puchamon.workshop.mixin;
import com.necro.raid.dens.common.items.item.RaidPouchItem;import com.necro.raid.dens.common.data.raid.BossLootTable;import org.spongepowered.asm.mixin.Mixin;import org.spongepowered.asm.mixin.injection.*;
@Mixin(value=RaidPouchItem.class,remap=false)
public abstract class RaidPouchMixin {
 @Redirect(method="getRewardItems",at=@At(value="INVOKE",target="Lcom/necro/raid/dens/common/data/raid/BossLootTable;replace()Z"))
 private boolean retainTierRewards(BossLootTable custom){return false;}
}
