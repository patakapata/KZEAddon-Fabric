package com.theboss.kzeaddonfabric.utils;

import com.mojang.authlib.GameProfile;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.theboss.kzeaddonfabric.KZEAddon;
import com.theboss.kzeaddonfabric.mixin.accessor.GameRendererAccessor;
import com.theboss.kzeaddonfabric.mixin.accessor.Matrix4fAccessor;
import com.theboss.kzeaddonfabric.render.Constants;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.client.render.*;
import net.minecraft.client.render.block.BlockModelRenderer;
import net.minecraft.client.render.block.BlockRenderManager;
import net.minecraft.client.render.block.entity.BlockEntityRenderDispatcher;
import net.minecraft.client.render.entity.PlayerModelPart;
import net.minecraft.client.texture.AbstractTexture;
import net.minecraft.client.texture.ResourceTexture;
import net.minecraft.client.util.GlAllocationUtils;
import net.minecraft.client.util.Window;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.*;
import net.minecraft.world.BlockRenderView;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL30;
import org.lwjgl.opengl.GL33;

import java.lang.reflect.Field;
import java.nio.FloatBuffer;
import java.util.*;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL30.*;

public class RenderUtils {
    public static final Queue<Integer> texStack = new ArrayDeque<>();
    public static final Random RANDOM;
    public static final BlockRenderManager renderManager;
    public static final BlockModelRenderer modelRenderer;

    static {
        RANDOM = new Random();
        renderManager = MinecraftClient.getInstance().getBlockRenderManager();
        modelRenderer = RenderUtils.renderManager.getModelRenderer();
    }

    public static void beginFullBuild(BufferBuilder buffer, int drawMode) {
        buffer.begin(drawMode, VertexFormats.POSITION_COLOR_TEXTURE_OVERLAY_LIGHT_NORMAL);
    }

    public static int getDrawFramebufferBound() {
        return GL30.glGetInteger(GL_DRAW_FRAMEBUFFER_BINDING);
    }

    public static int getReadFramebufferBound() {
        return GL30.glGetInteger(GL_READ_FRAMEBUFFER_BINDING);
    }

    @SuppressWarnings("RedundantCast")
    public static void bobView(MatrixStack matrices, float f) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.getCameraEntity() instanceof PlayerEntity) {
            PlayerEntity playerEntity = (PlayerEntity) client.getCameraEntity();
            float g = playerEntity.horizontalSpeed - playerEntity.prevHorizontalSpeed;
            float h = -(playerEntity.horizontalSpeed + g * f);
            float i = MathHelper.lerp(f, playerEntity.prevStrideDistance, playerEntity.strideDistance);
            matrices.translate((double) (MathHelper.sin(h * 3.1415927F) * i * 0.5F), (double) (-Math.abs(MathHelper.cos(h * 3.1415927F) * i)), 0.0D);
            matrices.multiply(Vec3f.POSITIVE_Z.getDegreesQuaternion(MathHelper.sin(h * 3.1415927F) * i * 3.0F));
            matrices.multiply(Vec3f.POSITIVE_X.getDegreesQuaternion(Math.abs(MathHelper.cos(h * 3.1415927F - 0.2F) * i) * 5.0F));
        }

    }

    public static void bobViewWhenHurt(MatrixStack matrices, float f) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.getCameraEntity() instanceof LivingEntity) {
            LivingEntity livingEntity = (LivingEntity) client.getCameraEntity();
            float g = (float) livingEntity.hurtTime - f;
            float i;
            if (livingEntity.isDead()) {
                i = Math.min((float) livingEntity.deathTime + f, 20.0F);
                matrices.multiply(Vec3f.POSITIVE_Z.getDegreesQuaternion(40.0F - 8000.0F / (i + 200.0F)));
            }

            if (g < 0.0F) {
                return;
            }

            g /= (float) livingEntity.maxHurtTime;
            g = MathHelper.sin(g * g * g * g * 3.1415927F);
            i = livingEntity.knockbackVelocity;
            matrices.multiply(Vec3f.POSITIVE_Y.getDegreesQuaternion(-i));
            matrices.multiply(Vec3f.POSITIVE_Z.getDegreesQuaternion(-g * 14.0F));
            matrices.multiply(Vec3f.POSITIVE_Y.getDegreesQuaternion(i));
        }

    }

    /**
     * Use {@link WorldRenderer#drawBox(MatrixStack, VertexConsumer, double, double, double, double, double, double, float, float, float, float)  WorldRenderer.drawBox}
     */
    @Deprecated
    private static void drawBoxOutline(VertexConsumer vertexConsumer, BlockPos start, BlockPos end, Matrix4f matrix, float red, float green, float blue, float alpha, boolean shouldSort) {
        vertexConsumer.vertex(matrix, start.getX(), start.getY(), start.getZ()).color(red, green, blue, alpha).next();
        vertexConsumer.vertex(matrix, end.getX(), start.getY(), start.getZ()).color(red, green, blue, alpha).next();
        vertexConsumer.vertex(matrix, end.getX(), start.getY(), end.getZ()).color(red, green, blue, alpha).next();
        vertexConsumer.vertex(matrix, start.getX(), start.getY(), end.getZ()).color(red, green, blue, alpha).next();
        vertexConsumer.vertex(matrix, start.getX(), end.getY(), start.getZ()).color(red, green, blue, alpha).next();
        vertexConsumer.vertex(matrix, start.getX(), end.getY(), end.getZ()).color(red, green, blue, alpha).next();
        vertexConsumer.vertex(matrix, end.getX(), end.getY(), end.getZ()).color(red, green, blue, alpha).next();
        vertexConsumer.vertex(matrix, end.getX(), end.getY(), start.getZ()).color(red, green, blue, alpha).next();
        vertexConsumer.vertex(matrix, start.getX(), start.getY(), start.getZ()).color(red, green, blue, alpha).next();
        vertexConsumer.vertex(matrix, start.getX(), end.getY(), start.getZ()).color(red, green, blue, alpha).next();
        vertexConsumer.vertex(matrix, end.getX(), end.getY(), start.getZ()).color(red, green, blue, alpha).next();
        vertexConsumer.vertex(matrix, end.getX(), start.getY(), start.getZ()).color(red, green, blue, alpha).next();
        vertexConsumer.vertex(matrix, start.getX(), start.getY(), end.getZ()).color(red, green, blue, alpha).next();
        vertexConsumer.vertex(matrix, end.getX(), start.getY(), end.getZ()).color(red, green, blue, alpha).next();
        vertexConsumer.vertex(matrix, end.getX(), end.getY(), end.getZ()).color(red, green, blue, alpha).next();
        vertexConsumer.vertex(matrix, start.getX(), end.getY(), end.getZ()).color(red, green, blue, alpha).next();
        vertexConsumer.vertex(matrix, start.getX(), start.getY(), start.getZ()).color(red, green, blue, alpha).next();
        vertexConsumer.vertex(matrix, start.getX(), start.getY(), end.getZ()).color(red, green, blue, alpha).next();
        vertexConsumer.vertex(matrix, start.getX(), end.getY(), end.getZ()).color(red, green, blue, alpha).next();
        vertexConsumer.vertex(matrix, start.getX(), end.getY(), start.getZ()).color(red, green, blue, alpha).next();
        vertexConsumer.vertex(matrix, end.getX(), start.getY(), start.getZ()).color(red, green, blue, alpha).next();
        vertexConsumer.vertex(matrix, end.getX(), end.getY(), start.getZ()).color(red, green, blue, alpha).next();
        vertexConsumer.vertex(matrix, end.getX(), end.getY(), end.getZ()).color(red, green, blue, alpha).next();
        vertexConsumer.vertex(matrix, end.getX(), start.getY(), end.getZ()).color(red, green, blue, alpha).next();
    }

    /**
     * Overload a {@link #drawPlayerHead(MatrixStack, GameProfile, int, int)}
     *
     * @param matrices MatrixStack
     * @param player   Skin get destination
     * @param x        Rendering offset in screen space
     * @param y        Rendering offset in screen space
     */
    @SuppressWarnings("unused")
    public static void drawPlayerHead(MatrixStack matrices, PlayerEntity player, int x, int y) {
        RenderUtils.drawPlayerHead(matrices, player.getGameProfile(), x, y);
    }

    @SuppressWarnings({ "SpellCheckingInspection", "ConstantConditions" })
    public static void drawPlayerHead(MatrixStack matrices, GameProfile profile, int x, int y) {
        MinecraftClient client = MinecraftClient.getInstance();
        PlayerListEntry entry = client.player.networkHandler.getPlayerListEntry(profile.getId());
        PlayerEntity playerEntity = client.world.getPlayerByUuid(profile.getId());
        boolean bl2 = playerEntity != null && playerEntity.isPartVisible(PlayerModelPart.CAPE) && ("Dinnerbone".equals(profile.getName()) || "Grumm".equals(profile.getName()));
        client.getTextureManager().bindTexture(entry.getSkinTexture());
        int ad = 8 + (bl2 ? 8 : 0);
        int ae = 8 * (bl2 ? -1 : 1);
        DrawableHelper.drawTexture(matrices, x, y, 8, 8, 8.0F, (float) ad, 8, ae, 64, 64);
        if (playerEntity != null && playerEntity.isPartVisible(PlayerModelPart.HAT)) {
            int af = 8 + (bl2 ? 8 : 0);
            int ag = 8 * (bl2 ? -1 : 1);
            DrawableHelper.drawTexture(matrices, x, y, 8, 8, 40.0F, (float) af, 8, ag, 64, 64);
        }
    }

    /**
     * Note: Y axis is down to plus
     */
    public static void drawRadialTexture(MatrixStack matrices, double progress, float size, float u1, float v1, float u2, float v2) {
        double radian = progress * Math.PI * 2 - Math.PI / 2;
        float cos = (float) Math.cos(radian);
        float sin = (float) Math.sin(radian);
        float texWidth = u2 - u1;
        float texHeight = v2 - v1;
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.getBuffer();
        Matrix4f matrix = matrices.peek().getModel();

        float xa = Math.abs(cos);
        float ya = Math.abs(sin);
        float x, y;
        if (ya > xa) { // 上下
            x = cos * (1 / ya);
            y = sin / ya;
        } else { // 左右
            x = cos / xa;
            y = sin * (1 / xa);
        }

        float xL = 0;
        float yL = -1;
        float xC, yC;
        int partition = (int) (progress / 0.125F);

        buffer.begin(GL11.GL_TRIANGLES, VertexFormats.POSITION_TEXTURE);
        for (int i = 1; i <= partition; i++) {
            switch (i) {
                case 1:
                default:
                    xC = 1;
                    yC = -1;
                    break;
                case 2:
                    xC = 1;
                    yC = 0;
                    break;
                case 3:
                    xC = 1;
                    yC = 1;
                    break;
                case 4:
                    xC = 0;
                    yC = 1;
                    break;
                case 5:
                    xC = -1;
                    yC = 1;
                    break;
                case 6:
                    xC = -1;
                    yC = 0;
                    break;
                case 7:
                    xC = -1;
                    yC = -1;
                    break;
                case 8:
                    xC = 0;
                    yC = -1;
                    break;
            }

            buffer.vertex(matrix, xL * size, yL * size, 0).texture(u1 + texWidth * (xL / 2 + 0.5F), v1 + texHeight * (yL / 2 + 0.5F)).next();
            buffer.vertex(matrix, 0, 0, 0).texture(0.5F, 0.5F).next();
            buffer.vertex(matrix, xC * size, yC * size, 0).texture(u1 + texWidth * (xC / 2 + 0.5F), v1 + texHeight * (yC / 2 + 0.5F)).next();

            xL = xC;
            yL = yC;
        }

        buffer.vertex(matrix, xL * size, yL * size, 0).texture(u1 + texWidth * (xL / 2 + 0.5F), v1 + texHeight * (yL / 2 + 0.5F)).next();
        buffer.vertex(matrix, 0, 0, 0).texture(0.5F, 0.5F).next();
        buffer.vertex(matrix, x * size, y * size, 0).texture(u1 + texWidth * (x / 2 + 0.5F), v1 + texHeight * (y / 2 + 0.5F)).next();

        tessellator.draw();
    }

    @SuppressWarnings("unused")
    public static double[] fromMatrix(Matrix4f matrix) {
        double[] result = new double[16];
        int i = 0;
        for (int y = 0; y < 4; y++) {
            for (int x = 0; x < 4; x++) {
                try {
                    Field field = Matrix4f.class.getDeclaredField("a" + y + "" + x);
                    field.setAccessible(true);
                    result[i++] = field.getFloat(matrix);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        }
        return result;
    }

    @SuppressWarnings("ConstantConditions")
    public static Vec3f getCameraRightWorldSpace(Matrix4f viewMatrix) {
        return new Vec3f(
                ((Matrix4fAccessor) (Object) viewMatrix).a00(),
                ((Matrix4fAccessor) (Object) viewMatrix).a10(),
                ((Matrix4fAccessor) (Object) viewMatrix).a20()
        );
    }

    @SuppressWarnings("ConstantConditions")
    public static Vec3f getCameraUpWorldSpace(Matrix4f viewMatrix) {
        return new Vec3f(
                ((Matrix4fAccessor) (Object) viewMatrix).a01(),
                ((Matrix4fAccessor) (Object) viewMatrix).a11(),
                ((Matrix4fAccessor) (Object) viewMatrix).a21()
        );
    }

    public static Optional<Integer> getGlId(Identifier identifier) {
        AbstractTexture texture = MinecraftClient.getInstance().getTextureManager().getTexture(identifier);
        return Optional.ofNullable(texture == null ? null : texture.getGlId());
    }

    public static List<Text> getMatrixContent(boolean isProjectionMatrix) {
        float[] matContent = new float[16];
        GL11.glGetFloatv(isProjectionMatrix ? GL11.GL_PROJECTION_MATRIX : GL11.GL_MODELVIEW_MATRIX, matContent);
        List<Text> list = new ArrayList<>();
        StringBuilder builder = new StringBuilder();
        list.add(Text.of((isProjectionMatrix ? "PROJECTION" : "MODELVIEW") + "  \\/"));
        for (int i = 0; i < 16; i++) {
            if (i % 4 == 0 && i != 0) {
                list.add(Text.of(builder.toString()));
                builder.setLength(0);
            }
            builder.append(" ").append(matContent[i / 4 + (i % 4 * 4)]).append(" ");
        }
        list.add(Text.of(builder.toString()));

        return list;
    }

    /**
     * Get a matrix contents
     *
     * @param matrices target matrix
     * @return String array split by rows
     */
    @SuppressWarnings("unused")
    public static String[] getMatrixContents(MatrixStack matrices) {
        FloatBuffer buffer = GlAllocationUtils.allocateFloatBuffer(4 * 4 * 4);
        Matrix4f matrix = matrices.peek().getModel();
        matrix.writeRowFirst(buffer);
        String[] result = new String[4];
        StringBuilder builder = new StringBuilder();

        int i = 0;
        for (int y = 0; y < 4; y++) {
            for (int x = 0; x < 4; x++) {
                builder.append(String.format("%3.2f", buffer.get(i++))).append(" ");
            }
            result[y] = builder.toString();
            builder.setLength(0);
        }

        return result;
    }

    public static Matrix4f getProjectionMatrix(float delta) {
        MinecraftClient mc = MinecraftClient.getInstance();
        return mc.gameRenderer.getBasicProjectionMatrix(mc.gameRenderer.getCamera(), delta, true);
    }

    public static Matrix4f getVPMatrix(float delta) {
        Matrix4f projection = getProjectionMatrix(delta);
        MatrixStack view = getViewMatrix(delta);
        projection.multiply(view.peek().getModel());

        return projection;
    }

    public static int getVertexArrayBinding() {
        return GL33.glGetInteger(GL33.GL_VERTEX_ARRAY_BINDING);
    }

    public static MatrixStack getViewMatrix(float delta) {
        MinecraftClient mc = MinecraftClient.getInstance();
        Camera camera = mc.gameRenderer.getCamera();
        MatrixStack matrix = new MatrixStack();

        bobViewWhenHurt(matrix, delta);
        if (mc.options.bobView) {
            bobView(matrix, delta);
        }

        if (mc.player != null) {
            float f = MathHelper.lerp(delta, mc.player.lastNauseaStrength, mc.player.nextNauseaStrength) * mc.options.distortionEffectScale * mc.options.distortionEffectScale;
            if (f > 0.0F) {
                int ticks = ((GameRendererAccessor) mc.gameRenderer).getTicks();
                int i = mc.player.hasStatusEffect(StatusEffects.NAUSEA) ? 7 : 20;
                float g = 5.0F / (f * f + 5.0F) - f * 0.04F;
                g *= g;
                Vec3f vec3f = new Vec3f(0.0F, MathHelper.SQUARE_ROOT_OF_TWO / 2.0F, MathHelper.SQUARE_ROOT_OF_TWO / 2.0F);
                matrix.multiply(vec3f.getDegreesQuaternion(((float) ticks + delta) * (float) i));
                matrix.scale(1.0F / g, 1.0F, 1.0F);
                float h = -((float) ticks + delta) * (float) i;
                matrix.multiply(vec3f.getDegreesQuaternion(h));
            }
        }

        matrix.multiply(Vec3f.POSITIVE_X.getDegreesQuaternion(camera.getPitch()));
        matrix.multiply(Vec3f.POSITIVE_Y.getDegreesQuaternion(camera.getYaw() + 180.0F));

        return matrix;
    }

    public static void polygonLineMode() {
        GL33.glPolygonMode(GL_FRONT_AND_BACK, GL_LINE);
    }

    public static void polygonFillMode() {
        GL33.glPolygonMode(GL_FRONT_AND_BACK, GL_FILL);
    }

    public static void glError(String location) {
        int error = GL33.glGetError();
        if (error != 0) {
            String msg = "";
            if (KZEAddon.GL_ERRORS.containsKey(error)) msg = KZEAddon.GL_ERRORS.get(error);
            KZEAddon.info(Text.Serializer.fromJson("{\"translate\":\"%s%s%s %s %s (%s)\",\"with\":[" +
                                                           "{\"text\":\"[\",\"color\":\"gold\"}," +
                                                           "{\"text\":\"ERROR\",\"color\":\"red\"}," +
                                                           "{\"text\":\"]\",\"color\":\"gold\"}," +
                                                           "{\"text\":\"" + location + "\",\"color\":\"white\"}," +
                                                           "{\"text\":\"" + error + "\",\"color\":\"white\"}," +
                                                           "{\"text\":\"" + msg + "\",\"color\":\"white\"}" +
                                                           "]}"));
        }
    }

    public static void drawTexturedBox(MatrixStack matrices, VertexConsumer consumer, Box box, float u1, float v1, float u2, float v2) {
        Matrix4f matrix = matrices.peek().getModel();
        // X+
        consumer.vertex(matrix, (float) box.maxX, (float) box.maxY, (float) box.maxZ).texture(u1, v1).next();
        consumer.vertex(matrix, (float) box.maxX, (float) box.minY, (float) box.maxZ).texture(u1, v2).next();
        consumer.vertex(matrix, (float) box.maxX, (float) box.minY, (float) box.minZ).texture(u2, v2).next();
        consumer.vertex(matrix, (float) box.maxX, (float) box.maxY, (float) box.minZ).texture(u2, v1).next();
        // X-
        consumer.vertex(matrix, (float) box.minX, (float) box.maxY, (float) box.minZ).texture(u1, v1).next();
        consumer.vertex(matrix, (float) box.minX, (float) box.minY, (float) box.minZ).texture(u1, v2).next();
        consumer.vertex(matrix, (float) box.minX, (float) box.minY, (float) box.maxZ).texture(u2, v2).next();
        consumer.vertex(matrix, (float) box.minX, (float) box.maxY, (float) box.maxZ).texture(u2, v1).next();
        // Y+
        consumer.vertex(matrix, (float) box.minX, (float) box.maxY, (float) box.minZ).texture(u1, v1).next();
        consumer.vertex(matrix, (float) box.minX, (float) box.maxY, (float) box.maxZ).texture(u1, v2).next();
        consumer.vertex(matrix, (float) box.maxX, (float) box.maxY, (float) box.maxZ).texture(u2, v2).next();
        consumer.vertex(matrix, (float) box.maxX, (float) box.maxY, (float) box.minZ).texture(u2, v1).next();
        // Y-
        consumer.vertex(matrix, (float) box.minX, (float) box.minY, (float) box.minZ).texture(u2, v1).next();
        consumer.vertex(matrix, (float) box.maxX, (float) box.minY, (float) box.minZ).texture(u1, v1).next();
        consumer.vertex(matrix, (float) box.maxX, (float) box.minY, (float) box.maxZ).texture(u1, v2).next();
        consumer.vertex(matrix, (float) box.minX, (float) box.minY, (float) box.maxZ).texture(u2, v2).next();
        // Z+
        consumer.vertex(matrix, (float) box.minX, (float) box.maxY, (float) box.maxZ).texture(u1, v1).next();
        consumer.vertex(matrix, (float) box.minX, (float) box.minY, (float) box.maxZ).texture(u1, v2).next();
        consumer.vertex(matrix, (float) box.maxX, (float) box.minY, (float) box.maxZ).texture(u2, v2).next();
        consumer.vertex(matrix, (float) box.maxX, (float) box.maxY, (float) box.maxZ).texture(u2, v1).next();
        // Z-
        consumer.vertex(matrix, (float) box.maxX, (float) box.maxY, (float) box.minZ).texture(u1, v1).next();
        consumer.vertex(matrix, (float) box.maxX, (float) box.minY, (float) box.minZ).texture(u1, v2).next();
        consumer.vertex(matrix, (float) box.minX, (float) box.minY, (float) box.minZ).texture(u2, v2).next();
        consumer.vertex(matrix, (float) box.minX, (float) box.maxY, (float) box.minZ).texture(u2, v1).next();
    }

    public static void glFlatShadeModel() {
        GlStateManager.shadeModel(Constants.GL_FLAT);
    }

    public static void glSmoothShadeModel() {
        GlStateManager.shadeModel(Constants.GL_SMOOTH);
    }

    public static void perspectiveToOrtho(MatrixStack mvp, int width, int height, Vector4f vector) {
        vector.transform(mvp.peek().getModel());
        vector.set(((vector.getX() / vector.getW()) + 1) / 2 * width, (-(vector.getY() / vector.getW()) + 1) / 2 * height, vector.getZ() / vector.getW(), vector.getW());
    }

    public static void popProjectionMatrix() {
        RenderSystem.matrixMode(GL_PROJECTION);
        RenderSystem.popMatrix();
        RenderSystem.matrixMode(GL_MODELVIEW);
    }

    public static void popTexture() {
        if (texStack.isEmpty()) throw new IllegalStateException("Texture stack is empty!");
        glBindTexture(GL_TEXTURE_2D, texStack.poll());
    }

    public static void pushTexture() {
        texStack.add(glGetInteger(GL_TEXTURE_BINDING_2D));
    }

    public static void registerTexture(String namespace, String path) {
        registerTexture(new Identifier(namespace, path));
    }

    public static void registerTexture(Identifier id) {
        MinecraftClient.getInstance().getTextureManager().registerTexture(id, new ResourceTexture(id));
    }

    public static void renderBlock(MatrixStack matrices, BlockRenderView world, BlockState state, BlockPos pos, VertexConsumer vertexConsumer, boolean cull) {
        modelRenderer.render(world, renderManager.getModel(state), state, pos, matrices, vertexConsumer, cull, RANDOM, state.getRenderingSeed(pos), OverlayTexture.DEFAULT_UV);
    }

    public static void renderBlock(MatrixStack matrices, float delta, BlockRenderView world, BlockPos pos, boolean cull, VertexConsumer consumer) {
        BlockState state = world.getBlockState(pos);
        if (world.getBlockState(pos).getBlock().hasBlockEntity()) {
            renderBlock(matrices, delta, world, pos, cull, consumer, state, world.getBlockEntity(pos));
        } else {
            MinecraftClient.getInstance().getBlockRenderManager().renderBlock(state, pos, world, matrices, consumer, cull, RANDOM);
        }
    }

    public static void renderBlock(MatrixStack matrices, float delta, BlockRenderView world, BlockPos pos, boolean cull, VertexConsumer consumer, BlockState state, BlockEntity blockEntity) {
        MinecraftClient mc = MinecraftClient.getInstance();

        mc.getBlockRenderManager().renderBlock(state, pos, world, matrices, consumer, cull, RANDOM);
        BlockEntityRenderDispatcher.INSTANCE.render(blockEntity, delta, matrices, mc.getBufferBuilders().getEntityVertexConsumers());
    }

    @SuppressWarnings({ "unused", "ConstantConditions" })
    public static void renderPlayerHead(MatrixStack matrices, float tickDelta) {
        float texSize = 64;
        MinecraftClient client = MinecraftClient.getInstance();
        List<AbstractClientPlayerEntity> players = client.world.getPlayers();
        ClientPlayerEntity mainPlayer = client.player;
        Camera camera = client.gameRenderer.getCamera();
        Vec3d cam = camera.getPos();
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.getBuffer();
        for (AbstractClientPlayerEntity player : players) {
            if (player.equals(mainPlayer)) continue;
            client.getTextureManager().bindTexture(client.player.networkHandler.getPlayerListEntry(player.getGameProfile().getId()).getSkinTexture());
            matrices.push();
            Matrix4f matrix = matrices.peek().getModel();
            matrices.translate(MathHelper.lerp(tickDelta, player.lastRenderX, player.getX()) - cam.x, MathHelper.lerp(tickDelta, player.lastRenderY, player.getY()) + 2.5 - cam.y, MathHelper.lerp(tickDelta, player.lastRenderZ, player.getZ()) - cam.z);
            matrices.multiply(Vec3f.POSITIVE_Y.getDegreesQuaternion(-camera.getYaw()));
            matrices.multiply(Vec3f.POSITIVE_X.getDegreesQuaternion(camera.getPitch()));
            buffer.begin(GL11.GL_QUADS, VertexFormats.POSITION_TEXTURE);
            buffer.vertex(matrix, 0.5F, 0.5F, 0).texture(8 / texSize, 8 / texSize).next();
            buffer.vertex(matrix, 0.5F, -0.5F, 0).texture(8 / texSize, 16 / texSize).next();
            buffer.vertex(matrix, -0.5F, -0.5F, 0).texture(16 / texSize, 16 / texSize).next();
            buffer.vertex(matrix, -0.5F, 0.5F, 0).texture(16 / texSize, 8 / texSize).next();
            tessellator.draw();
            buffer.begin(GL11.GL_QUADS, VertexFormats.POSITION_TEXTURE);
            buffer.vertex(matrix, 0.6F, 0.6F, 0).texture(40 / texSize, 8 / texSize).next();
            buffer.vertex(matrix, 0.6F, -0.6F, 0).texture(40 / texSize, 16 / texSize).next();
            buffer.vertex(matrix, -0.6F, -0.6F, 0).texture(48 / texSize, 16 / texSize).next();
            buffer.vertex(matrix, -0.6F, 0.6F, 0).texture(48 / texSize, 8 / texSize).next();
            RenderSystem.enableBlend();
            RenderSystem.enablePolygonOffset();
            RenderSystem.polygonOffset(-1, -1);
            tessellator.draw();
            RenderSystem.disablePolygonOffset();
            RenderSystem.enableBlend();
            matrices.pop();
        }
    }

    public static void setupOrthogonalProjectionMatrix() {
        Window window = MinecraftClient.getInstance().getWindow();
        RenderSystem.matrixMode(GL_PROJECTION);
        RenderSystem.pushMatrix();
        RenderSystem.loadIdentity();
        RenderSystem.ortho(0.0D, window.getScaledWidth(), window.getScaledHeight(), 0.0D, 1000.0D, 3000.0D);
        RenderSystem.matrixMode(GL_MODELVIEW);
    }

    public static void setupPerspectiveProjectionMatrix(float delta) {
        RenderSystem.matrixMode(GL_PROJECTION);
        RenderSystem.pushMatrix();
        RenderSystem.loadIdentity();
        RenderSystem.multMatrix(getProjectionMatrix(delta));
        RenderSystem.matrixMode(GL_MODELVIEW);
    }

    @SuppressWarnings("unused")
    public static void toMatrix(Matrix4f matrix, double[] contents) {
        for (int x = 0; x < 4; x++) {
            for (int y = 0; y < 4; y++) {
                try {
                    Field field = Matrix4f.class.getDeclaredField("a" + x + "" + y);
                    field.setAccessible(true);
                    field.set(matrix, (float) contents[y * 4 + x]);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        }
    }

    private RenderUtils() {}
}
