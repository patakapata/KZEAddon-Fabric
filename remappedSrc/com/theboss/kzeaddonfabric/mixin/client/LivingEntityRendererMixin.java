package com.theboss.kzeaddonfabric.mixin.client;

import com.theboss.kzeaddonfabric.KZEAddon;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRenderDispatcher;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.render.entity.LivingEntityRenderer;
import net.minecraft.client.render.entity.feature.FeatureRenderer;
import net.minecraft.client.render.entity.feature.FeatureRendererContext;
import net.minecraft.client.render.entity.model.EntityModel;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.EntityPose;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

import java.util.Iterator;
import java.util.List;

@SuppressWarnings({"unchecked", "WhileLoopReplaceableByForEach", "RawUseOfParameterized", "RedundantCast"})
@Mixin(LivingEntityRenderer.class)
public abstract class LivingEntityRendererMixin<T extends LivingEntity, M extends EntityModel<T>> extends EntityRenderer<T> implements FeatureRendererContext<T, M> {
    @Shadow
    protected M model;

    @Shadow
    protected abstract float getHandSwingProgress(T entity, float tickDelta);

    @Shadow
    protected abstract float getAnimationProgress(T entity, float tickDelta);

    @Shadow
    protected abstract void setupTransforms(T entity, MatrixStack matrices, float animationProgress, float bodyYaw, float tickDelta);

    @Shadow
    protected abstract void scale(T entity, MatrixStack matrices, float amount);

    @Shadow
    protected abstract boolean isVisible(T entity);

    @Shadow
    protected abstract RenderLayer getRenderLayer(T entity, boolean showBody, boolean translucent, boolean showOutline);

    @Shadow
    public static int getOverlay(LivingEntity entity, float whiteOverlayProgress) {
        return 0;
    }

    @Shadow
    protected abstract float getAnimationCounter(T entity, float tickDelta);

    @Shadow
    @Final
    protected List<FeatureRenderer<T, M>> features;

    public LivingEntityRendererMixin(EntityRenderDispatcher dispatcher) {
        super(dispatcher);
    }

    /**
     * @author theBooooSS
     * @reason Change the entity color
     */
    @Overwrite
    public void render(T livingEntity, float f, float g, MatrixStack matrixStack, VertexConsumerProvider vertexConsumerProvider, int i) {
        matrixStack.push();
        this.model.handSwingProgress = this.getHandSwingProgress(livingEntity, g);
        this.model.riding = livingEntity.hasVehicle();
        this.model.child = livingEntity.isBaby();
        float h = MathHelper.lerpAngleDegrees(g, livingEntity.prevBodyYaw, livingEntity.bodyYaw);
        float j = MathHelper.lerpAngleDegrees(g, livingEntity.prevHeadYaw, livingEntity.headYaw);
        float k = j - h;
        float o;
        if (livingEntity.hasVehicle() && livingEntity.getVehicle() instanceof LivingEntity) {
            LivingEntity livingEntity2 = (LivingEntity) livingEntity.getVehicle();
            h = MathHelper.lerpAngleDegrees(g, livingEntity2.prevBodyYaw, livingEntity2.bodyYaw);
            k = j - h;
            o = MathHelper.wrapDegrees(k);
            if (o < -85.0F) {
                o = -85.0F;
            }

            if (o >= 85.0F) {
                o = 85.0F;
            }

            h = j - o;
            if (o * o > 2500.0F) {
                h += o * 0.2F;
            }

            k = j - h;
        }

        float m = MathHelper.lerp(g, livingEntity.prevPitch, livingEntity.pitch);
        float p;
        if (livingEntity.getPose() == EntityPose.SLEEPING) {
            Direction direction = livingEntity.getSleepingDirection();
            if (direction != null) {
                p = livingEntity.getEyeHeight(EntityPose.STANDING) - 0.1F;
                matrixStack.translate((double) ((float) (-direction.getOffsetX()) * p), 0.0D, (double) ((float) (-direction.getOffsetZ()) * p));
            }
        }

        o = this.getAnimationProgress(livingEntity, g);
        this.setupTransforms(livingEntity, matrixStack, o, h, g);
        matrixStack.scale(-1.0F, -1.0F, 1.0F);
        this.scale(livingEntity, matrixStack, g);
        matrixStack.translate(0.0D, -1.5010000467300415D, 0.0D);
        p = 0.0F;
        float q = 0.0F;
        if (!livingEntity.hasVehicle() && livingEntity.isAlive()) {
            p = MathHelper.lerp(g, livingEntity.lastLimbDistance, livingEntity.limbDistance);
            q = livingEntity.limbAngle - livingEntity.limbDistance * (1.0F - g);
            if (livingEntity.isBaby()) {
                q *= 3.0F;
            }

            if (p > 1.0F) {
                p = 1.0F;
            }
        }

        this.model.animateModel(livingEntity, q, p, g);
        this.model.setAngles(livingEntity, q, p, o, k, m);
        MinecraftClient minecraftClient = MinecraftClient.getInstance();
        boolean bl = this.isVisible(livingEntity);
        boolean bl2 = !bl && !livingEntity.isInvisibleTo(minecraftClient.player);
        boolean bl3 = minecraftClient.hasOutline(livingEntity);
        RenderLayer renderLayer = this.getRenderLayer(livingEntity, bl, bl2, bl3);
        if (renderLayer != null) {
            VertexConsumer vertexConsumer = vertexConsumerProvider.getBuffer(renderLayer);
            int r = getOverlay(livingEntity, this.getAnimationCounter(livingEntity, g));
            float[] color = KZEAddon.getEntityRenderColor(livingEntity, bl2 ? 0.15F : 1.0F);
            this.model.render(matrixStack, vertexConsumer, i, r, color[0], color[1], color[2], color[3]);
        }

        if (!livingEntity.isSpectator()) {
            Iterator var23 = this.features.iterator();

            while (var23.hasNext()) {
                FeatureRenderer<T, M> featureRenderer = (FeatureRenderer) var23.next();
                featureRenderer.render(matrixStack, vertexConsumerProvider, i, livingEntity, q, p, g, o, k, m);
            }
        }

        matrixStack.pop();
        super.render(livingEntity, f, g, matrixStack, vertexConsumerProvider, i);
    }
}
