package com.theboss.kzeaddonfabric.events;

import com.mojang.blaze3d.systems.RenderSystem;
import com.theboss.kzeaddonfabric.KZEAddon;
import com.theboss.kzeaddonfabric.render.VBOWrapperRegistry;
import com.theboss.kzeaddonfabric.render.shader.BarrierShader;
import com.theboss.kzeaddonfabric.render.shader.InvertShader;
import com.theboss.kzeaddonfabric.render.shader.ParticleShader;
import com.theboss.kzeaddonfabric.render.shader.ScanShader;
import com.theboss.kzeaddonfabric.wip.*;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.*;
import net.minecraft.client.render.block.BlockRenderManager;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.*;
import net.minecraft.util.profiler.Profiler;
import org.lwjgl.opengl.GL11;

import java.util.*;

import static org.lwjgl.opengl.GL43.*;

public class RenderingEventsListener {
    public static final Queue<Integer> texStack = new ArrayDeque<>();
    public static int lightLevel;

    /**
     * Render world event listener method
     * Not expect call by you
     *
     * @param matrices {@link net.minecraft.client.util.math.MatrixStack}
     * @param delta    A render delay
     */
    @SuppressWarnings("unused")
    public static void afterRenderWorld(MatrixStack matrices, float delta) {
        KZEAddon.glError("afterRenderWorld Head");
        MinecraftClient mc = MinecraftClient.getInstance();
        Vec3d cam = mc.gameRenderer.getCamera().getPos();
        Profiler profiler = KZEAddon.getProfiler();
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.getBuffer();

        profiler.swap("KZEAddon$onRenderWorld");
        //        profiler.push("Marked Area");
        //        MARKED_AREA.render(matrices, delta);
        //        profiler.pop();
        // Entity entity = mc.cameraEntity;
        /* TEST_LAYERS.render(matrices,
                (float) MathHelper.lerp(delta, entity.lastRenderX, pos.getX()),
                (float) MathHelper.lerp(delta, entity.lastRenderY, pos.getY()),
                (float) MathHelper.lerp(delta, entity.lastRenderZ, pos.getZ()),
                delta);
         */

        matrices.push();
        matrices.translate(-cam.getX(), -cam.getY(), -cam.getZ());
        PerlinParticle[] particles = KZEAddon.PARTICLES.toArray(new PerlinParticle[0]);
        List<PerlinParticle> list = Arrays.asList(particles);
        list.sort((p1, p2) -> PerlinParticle.whichNear(cam, p1, p2));
        RenderSystem.enableBlend();
        mc.getTextureManager().bindTexture(KZEAddon.PARTICLE_SPRITE);
        for (PerlinParticle pp : list) {
            pp.render(matrices, delta);
        }

        // Render the instanced particles
        InstancedPerlinParticleManager.INSTANCE.render(delta);
        // Render the depth circle
        FrameBufferLearn.INSTANCE.doRender(delta);

        RenderSystem.disableBlend();
        matrices.pop();

        // -------------------------------------------------- //
        // TODO コマンド範囲描画
        if (DebugCommand.traceCmdBlockStart != null && DebugCommand.traceCmdBlockEnd != null) {
            BlockPos s = DebugCommand.traceCmdBlockStart.add(1, 0, 1);
            BlockPos e = DebugCommand.traceCmdBlockEnd.add(0, 1, 0);

            // -------------------------------------------------- //
            // Setup
            matrices.push();
            matrices.translate(-cam.getX(), -cam.getY(), -cam.getZ());
            Matrix4f matrix = matrices.peek().getModel();
            RenderSystem.defaultBlendFunc();
            RenderSystem.disableTexture();
            RenderSystem.enablePolygonOffset();
            RenderSystem.enableLineOffset();
            RenderSystem.disableCull();
            RenderSystem.enableBlend();
            RenderSystem.enableDepthTest();
            // -------------------------------------------------- //
            // Rendering
            RenderSystem.polygonMode(GL_FRONT_AND_BACK, GL_LINE);
            RenderSystem.polygonOffset(-2, -2);
            glLineWidth(2.0F);
            drawBoxOutline(tessellator, buffer, s, e, matrix, 0f, 0f, 1f, 1f, false);
            glLineWidth(1.0F);
            RenderSystem.polygonMode(GL_FRONT_AND_BACK, GL_FILL);
            RenderSystem.polygonOffset(-1, -1);
            drawBoxOutline(tessellator, buffer, s, e, matrix, 0.5f, 0.5f, 1.0f, 0.2f, true);
            // -------------------------------------------------- //
            // Restore
            RenderSystem.disableBlend();
            RenderSystem.enableCull();
            RenderSystem.disableLineOffset();
            RenderSystem.disablePolygonOffset();
            RenderSystem.enableTexture();
            matrices.pop();
        }
        KZEAddon.glError("afterRenderWorld Tail");
    }

    /**
     * カットアウトレイヤー(ガラスなど透明部分があるが、半透明はない物)の描画前
     *
     * @param matrices               カメラの回転や、視覚効果がのみが適応された行列
     * @param delta                  前回の描画からの経過時間(秒単位)
     * @param limitTime              不明
     * @param renderBlockOutline     アウトラインを描画するか
     * @param camera                 カメラ
     * @param gameRenderer           ゲームレンダラー
     * @param lightmapTextureManager ライトマップテクスチャマネージャー
     * @param matrix4f               不明
     */
    public static void beforeRenderCutout(MatrixStack matrices, float delta, long limitTime, boolean renderBlockOutline, Camera camera, GameRenderer gameRenderer, LightmapTextureManager lightmapTextureManager, Matrix4f matrix4f) {
        MinecraftClient mc = MinecraftClient.getInstance();
        Profiler profiler = KZEAddon.getProfiler();
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.getBuffer();
        Vec3d cam = camera.getPos();

        EventsListener.maxMultiTex = glGetInteger(GL_MAX_TEXTURE_UNITS);
        profiler.push("Chunk Instanced Barrier Visualizer");
        // KZEAddon.BAR_VISUALIZER.draw(matrices, delta);
        // InstancedBarrierVisualizer.INSTANCE.render(delta);
        profiler.push("Barrier Visualize");
        ChunkInstancedBarrierVisualizer.INSTANCE.render(delta);
        profiler.swap("State and Offset");
        if (DebugCommand.showCIBVChunkStates) {
            ChunkInstancedBarrierVisualizer.INSTANCE.renderChunkOffsets(matrices, c -> {
                ChunkInstancedBarrierVisualizer.ChunkUpdateState state = c.getUpdateState();
                char colorCode;
                switch (state) {
                    case NEUTRAL:
                        colorCode = 'a';
                        break;
                    case UPDATE_CPU:
                        colorCode = 'c';
                        break;
                    case WAIT_UPLOAD:
                        colorCode = 'e';
                        break;
                    default:
                        colorCode = 'f';
                }

                return "§" + colorCode + c.getId() + " / " + c.getOffset().toShortString();
            });
        }
        // -------------------------------------------------- //
        // TODO コマンドコネクト描画
        if (!DebugCommand.traceCmdList.isEmpty()) {
            float offset = System.currentTimeMillis() % 1_000 / 1_000.0F;

            matrices.push();
            matrices.translate(-cam.getX(), -cam.getY(), -cam.getZ());
            Matrix4f matrix = matrices.peek().getModel();
            matrices.pop();

            buffer.begin(GL11.GL_LINE_STRIP, VertexFormats.POSITION_COLOR_TEXTURE);
            int i = 0;
            for (BlockPos pos : DebugCommand.traceCmdList) {
                buffer.vertex(matrix, pos.getX() + 0.5F, pos.getY() + 0.5F, pos.getZ() + 0.5F).color(1F, 1F, 1F, 1F).texture(0F, i * 1F - offset).next();
                i++;
            }

            // Setup
            RenderSystem.disableDepthTest();
            pushTexture();
            mc.getTextureManager().bindTexture(new Identifier("kzeaddon-fabric", "textures/wip/cb_connect.png"));
            RenderSystem.defaultBlendFunc();
            RenderSystem.lineWidth(2.0F);
            glEnable(GL_LINE_SMOOTH);
            // Render
            tessellator.draw();
            // Restore
            glDisable(GL_LINE_SMOOTH);
            RenderSystem.lineWidth(1.0F);
            popTexture();
            RenderSystem.enableDepthTest();
        }

        // -------------------------------------------------- //
        // TODO ブロック描画テスト
        if (false && mc.world != null && mc.crosshairTarget != null && mc.crosshairTarget.getType() == HitResult.Type.BLOCK) {
            BlockHitResult hitResult = (BlockHitResult) mc.crosshairTarget;
            Vec3i offset = hitResult.getSide().getVector();
            BlockPos hitPos = hitResult.getBlockPos().add(offset);
            BlockRenderManager brManager = mc.getBlockRenderManager();
            matrices.push();
            matrices.translate(
                    hitPos.getX() - cam.getX() + 0.5F,
                    hitPos.getY() - cam.getY() + 0.5F,
                    hitPos.getZ() - cam.getZ() + 0.5F
            );
            matrices.multiply(Vec3f.POSITIVE_Y.getDegreesQuaternion(System.currentTimeMillis() % 2_000 / 2_000.0F * 360.0F));
            matrices.scale(0.5f, 0.5f, 0.5f);
            matrices.translate(-0.5F, -0.5F, -0.5F);
            // buffer.begin(GL11.GL_QUADS, VertexFormats.POSITION_COLOR_TEXTURE_LIGHT_NORMAL);
            RenderingEventsListener.lightLevel = mc.world.getLightLevel(hitPos);
            // brManager.renderBlockAsEntity(mc.world.getBlockState(hitPos.subtract(offset)), matrices, VertexConsumerProvider.immediate(buffer), WorldRenderer.getLightmapCoordinates(mc.world, hitPos), 0);
            buffer.begin(GL11.GL_QUADS, VertexFormats.POSITION_COLOR_TEXTURE_OVERLAY_LIGHT_NORMAL);
            brManager.renderBlock(mc.world.getBlockState(hitPos.subtract(offset)), hitPos.subtract(offset), mc.world, matrices, buffer, true, new Random());
            RenderSystem.enableAlphaTest();
            RenderSystem.defaultBlendFunc();
            lightmapTextureManager.enable();
            tessellator.draw();
            lightmapTextureManager.disable();
            RenderSystem.disableAlphaTest();
            matrices.pop();
        }
        profiler.pop();
        profiler.pop();
    }

    public static void beforeRenderSolid(MatrixStack matrices, float delta, long limitTime, boolean renderBlockOutline, Camera camera, GameRenderer gameRenderer, LightmapTextureManager lightmapTextureManager, Matrix4f matrix4f) {
    }

    public static int compareBlockPos(Vec3i vec1, Vec3i vec2) {
        int x = Integer.compare(vec1.getX(), vec2.getX());
        int y = Integer.compare(vec1.getY(), vec2.getY());
        int z = Integer.compare(vec1.getZ(), vec2.getZ());

        if (vec1.equals(vec2)) return 0;
        else if (y > 0 && x > 0 && z > 0) return 1;
        else return -1;
    }

    private static void drawBoxOutline(Tessellator tessellator, BufferBuilder buffer, BlockPos s, BlockPos e, Matrix4f matrix, float red, float green, float blue, float alpha, boolean shouldSort) {
        buffer.begin(GL11.GL_QUADS, VertexFormats.POSITION_COLOR);
        buffer.vertex(matrix, s.getX(), s.getY(), s.getZ()).color(red, green, blue, alpha).next();
        buffer.vertex(matrix, e.getX(), s.getY(), s.getZ()).color(red, green, blue, alpha).next();
        buffer.vertex(matrix, e.getX(), s.getY(), e.getZ()).color(red, green, blue, alpha).next();
        buffer.vertex(matrix, s.getX(), s.getY(), e.getZ()).color(red, green, blue, alpha).next();
        buffer.vertex(matrix, s.getX(), e.getY(), s.getZ()).color(red, green, blue, alpha).next();
        buffer.vertex(matrix, s.getX(), e.getY(), e.getZ()).color(red, green, blue, alpha).next();
        buffer.vertex(matrix, e.getX(), e.getY(), e.getZ()).color(red, green, blue, alpha).next();
        buffer.vertex(matrix, e.getX(), e.getY(), s.getZ()).color(red, green, blue, alpha).next();
        buffer.vertex(matrix, s.getX(), s.getY(), s.getZ()).color(red, green, blue, alpha).next();
        buffer.vertex(matrix, s.getX(), e.getY(), s.getZ()).color(red, green, blue, alpha).next();
        buffer.vertex(matrix, e.getX(), e.getY(), s.getZ()).color(red, green, blue, alpha).next();
        buffer.vertex(matrix, e.getX(), s.getY(), s.getZ()).color(red, green, blue, alpha).next();
        buffer.vertex(matrix, s.getX(), s.getY(), e.getZ()).color(red, green, blue, alpha).next();
        buffer.vertex(matrix, e.getX(), s.getY(), e.getZ()).color(red, green, blue, alpha).next();
        buffer.vertex(matrix, e.getX(), e.getY(), e.getZ()).color(red, green, blue, alpha).next();
        buffer.vertex(matrix, s.getX(), e.getY(), e.getZ()).color(red, green, blue, alpha).next();
        buffer.vertex(matrix, s.getX(), s.getY(), s.getZ()).color(red, green, blue, alpha).next();
        buffer.vertex(matrix, s.getX(), s.getY(), e.getZ()).color(red, green, blue, alpha).next();
        buffer.vertex(matrix, s.getX(), e.getY(), e.getZ()).color(red, green, blue, alpha).next();
        buffer.vertex(matrix, s.getX(), e.getY(), s.getZ()).color(red, green, blue, alpha).next();
        buffer.vertex(matrix, e.getX(), s.getY(), s.getZ()).color(red, green, blue, alpha).next();
        buffer.vertex(matrix, e.getX(), e.getY(), s.getZ()).color(red, green, blue, alpha).next();
        buffer.vertex(matrix, e.getX(), e.getY(), e.getZ()).color(red, green, blue, alpha).next();
        buffer.vertex(matrix, e.getX(), s.getY(), e.getZ()).color(red, green, blue, alpha).next();

        if (shouldSort) buffer.sortQuads(0, 0, 0);

        tessellator.draw();
    }

    /**
     * Hud render event listener
     *
     * @param matrices {@link MatrixStack}
     * @param delta    A rendering delay
     */
    @SuppressWarnings("unused")
    public static void onRenderHud(MatrixStack matrices, float delta) {
        Profiler profiler = KZEAddon.getProfiler();
        profiler.push("KZEAddon$onRenderHud");

        profiler.push("Widgets");
        KZEAddon.Options.renderWidgets(matrices);
        profiler.swap("KillLog");
        if (MinecraftClient.getInstance().player.isSneaking()) {
            KZEAddon.KZE_INFO.getKillLog().render(matrices, delta);
        }
        if (KZEAddon.isShowModLog()) {
            KZEAddon.MOD_LOG.render(matrices);
        }

        profiler.pop();
        profiler.pop();
    }

    /**
     * Private rendering system initialization
     */
    public static void onRenderInit() {
        KZEAddon.glError("onRenderInit Head");
        KZEAddon.BAR_VISUALIZER.init();
        KZEAddon.BAR_VISUALIZER.setDistance(KZEAddon.Options.getBarrierVisualizeRadius());
        VBOWrapperRegistry.initWrappers(1024 * 1024 * 2);
        // TODO Shader init
        InvertShader.INSTANCE.initialize();
        ScanShader.INSTANCE.initialize();
        ParticleShader.INSTANCE.initialize();
        InstancedPerlinParticleManager.INSTANCE.init();
        BarrierShader.INSTANCE.initialize();
        InstancedBarrierVisualizer.INSTANCE.initialize();
        ChunkInstancedBarrierVisualizer.INSTANCE.initialize();
        KZEAddon.glError("onRenderInit Tail");
    }

    public static void onResolutionChanged() {
        FrameBufferLearn.INSTANCE.updateTextureSize();
    }

    public static void popTexture() {
        if (texStack.isEmpty()) throw new IllegalStateException("Texture stack is empty!");
        glBindTexture(GL_TEXTURE_2D, texStack.poll());
    }

    public static void pushTexture() {
        texStack.add(glGetInteger(GL_TEXTURE_BINDING_2D));
    }
}
