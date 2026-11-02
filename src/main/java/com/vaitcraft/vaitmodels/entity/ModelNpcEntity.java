package com.vaitcraft.vaitmodels.entity;

import com.vaitcraft.vaitmodels.VaitModels;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.PanicGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animation.AnimatableManager;
import software.bernie.geckolib.animation.AnimationController;
import software.bernie.geckolib.animation.PlayState;
import software.bernie.geckolib.constant.DefaultAnimations;
import software.bernie.geckolib.util.GeckoLibUtil;

/**
 * Entidad generica animada con GeckoLib.
 * El modelo se elige con el dato sincronizado "ModelId", p. ej.:
 *   /summon vaitmodels:npc ~ ~ ~ {ModelId:"robot"}
 *   /summon vaitmodels:npc ~ ~ ~ {ModelId:"otromod:dragon"}   (otro namespace / resourcepack)
 *
 * Nombres de animacion que busca en el .animation.json (convencion de GeckoLib):
 *   misc.idle | move.walk | move.run | misc.interact
 */
public class ModelNpcEntity extends PathfinderMob implements GeoEntity {
    public static final String DEFAULT_MODEL = "example";
    public static final String CONTROLLER_MOVE = "move";
    public static final String CONTROLLER_ACTION = "action";
    public static final String TRIGGER_INTERACT = "interact";

    private static final EntityDataAccessor<String> DATA_MODEL_ID =
            SynchedEntityData.defineId(ModelNpcEntity.class, EntityDataSerializers.STRING);

    private final AnimatableInstanceCache geoCache = GeckoLibUtil.createInstanceCache(this);

    // cache del ModelId ya convertido a ResourceLocation (se usa cada frame en el cliente)
    private String cachedRaw = null;
    private ResourceLocation cachedLocation = null;

    public ModelNpcEntity(EntityType<? extends ModelNpcEntity> type, Level level) {
        super(type, level);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 20.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.25D);
    }

    // ---------------------------------------------------------------- datos / NBT

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_MODEL_ID, DEFAULT_MODEL);
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putString("ModelId", getModelId());
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        if (tag.contains("ModelId")) {
            setModelId(tag.getString("ModelId"));
        }
    }

    public String getModelId() {
        return this.entityData.get(DATA_MODEL_ID);
    }

    public void setModelId(String id) {
        this.entityData.set(DATA_MODEL_ID, id);
    }

    /** "robot" -> vaitmodels:robot ; "otromod:dragon" -> otromod:dragon ; invalido -> modelo por defecto. */
    public ResourceLocation getModelLocation() {
        String raw = getModelId();
        if (!raw.equals(this.cachedRaw) || this.cachedLocation == null) {
            this.cachedRaw = raw;
            this.cachedLocation = parseModelId(raw);
        }
        return this.cachedLocation;
    }

    private static ResourceLocation parseModelId(String raw) {
        ResourceLocation parsed = null;
        if (!raw.isEmpty()) {
            if (raw.indexOf(':') >= 0) {
                parsed = ResourceLocation.tryParse(raw);
            } else if (ResourceLocation.isValidPath(raw)) {
                parsed = ResourceLocation.fromNamespaceAndPath(VaitModels.MODID, raw);
            }
        }
        return parsed != null ? parsed : ResourceLocation.fromNamespaceAndPath(VaitModels.MODID, DEFAULT_MODEL);
    }

    // ---------------------------------------------------------------- IA

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new RunWhenHurtGoal(this, 1.8D));          // huye CORRIENDO si le pegan
        this.goalSelector.addGoal(2, new WaterAvoidingRandomStrollGoal(this, 0.8D)); // pasea CAMINANDO
        this.goalSelector.addGoal(3, new LookAtPlayerGoal(this, Player.class, 8.0F)); // te MIRA
        this.goalSelector.addGoal(4, new RandomLookAroundGoal(this));
    }

    @Override
    public boolean removeWhenFarAway(double distanceToClosestPlayer) {
        return false; // es un NPC: no desaparece
    }

    /** Panico de vanilla, pero marca la entidad como "sprinting" para que el cliente reproduzca move.run. */
    private static final class RunWhenHurtGoal extends PanicGoal {
        private final PathfinderMob npc;

        RunWhenHurtGoal(PathfinderMob mob, double speedModifier) {
            super(mob, speedModifier);
            this.npc = mob;
        }

        @Override
        public void start() {
            super.start();
            this.npc.setSprinting(true);
        }

        @Override
        public void stop() {
            super.stop();
            this.npc.setSprinting(false);
        }
    }

    // ---------------------------------------------------------------- interaccion

    @Override
    protected InteractionResult mobInteract(Player player, InteractionHand hand) {
        if (hand != InteractionHand.MAIN_HAND) {
            return InteractionResult.PASS;
        }
        if (!this.level().isClientSide) {
            this.getNavigation().stop();
            this.getLookControl().setLookAt(player, 30.0F, 30.0F);
            this.triggerAnim(CONTROLLER_ACTION, TRIGGER_INTERACT);
        }
        return InteractionResult.sidedSuccess(this.level().isClientSide);
    }

    // ---------------------------------------------------------------- GeckoLib

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        // Controlador de movimiento: idle / caminar / correr
        controllers.add(new AnimationController<>(this, CONTROLLER_MOVE, 5, state -> {
            if (state.isMoving()) {
                return state.setAndContinue(this.isSprinting() ? DefaultAnimations.RUN : DefaultAnimations.WALK);
            }
            return state.setAndContinue(DefaultAnimations.IDLE);
        }));

        // Controlador de acciones: solo se activa cuando el servidor dispara "interact"
        controllers.add(new AnimationController<>(this, CONTROLLER_ACTION, 0, state -> PlayState.STOP)
                .triggerableAnim(TRIGGER_INTERACT, DefaultAnimations.INTERACT));
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.geoCache;
    }
}
