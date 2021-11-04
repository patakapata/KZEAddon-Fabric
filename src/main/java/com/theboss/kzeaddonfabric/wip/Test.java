package com.theboss.kzeaddonfabric.wip;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.client.world.DummyClientTickScheduler;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.Fluid;
import net.minecraft.item.map.MapState;
import net.minecraft.recipe.RecipeManager;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.tag.TagGroup;
import net.minecraft.tag.TagManager;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.profiler.Profiler;
import net.minecraft.util.registry.DynamicRegistryManager;
import net.minecraft.util.registry.RegistryKey;
import net.minecraft.world.MutableWorldProperties;
import net.minecraft.world.TickScheduler;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.chunk.ChunkManager;
import net.minecraft.world.dimension.DimensionType;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.List;
import java.util.Scanner;
import java.util.function.Supplier;

public class Test {
    public static void main(String[] args) {
        System.out.println("RESULT: " + MathHelper.floor(-13F / 16));
    }

    public static int safeParse(String src) {
        try {
            return Integer.parseInt(src);
        } catch (NumberFormatException ex) {
            return -1;
        }
    }

    public static String safeRead(Scanner scanner) {
        try {
            return scanner.nextLine();
        } catch (Exception ex) {
            return "";
        }
    }

    public static class Structure {
        private String name;
        private int age;
        private String aliases;
        private int deathOn;

        public Structure() {
            System.out.println("Construct with no args");
            this.name = "DEFAULT NAME";
            this.age = -1;
            this.aliases = null;
            this.deathOn = -1;
        }

        public Structure(String name, int age) {
            System.out.println("Construct with name and age");
            this.name = name;
            this.age = age;
            this.aliases = null;
            this.deathOn = -1;
        }

        public Structure(String name, int age, String aliases, int deathOn) {
            System.out.println("Construct with all args");
            this.name = name;
            this.age = age;
            this.aliases = aliases;
            this.deathOn = deathOn;
        }

        @Override
        public String toString() {
            return String.format("%s: %d(%s), %d", this.name, this.age, this.aliases, this.deathOn);
        }
    }

    public static class RenderWorld extends World {
        private final Scoreboard scoreboard;
        private final RecipeManager recipeManager;
        private final TagManager tagManager;
        private final DummyClientTickScheduler<Block> blockScheduler;
        private final DummyClientTickScheduler<Fluid> fluidScheduler;

        public RenderWorld(MutableWorldProperties properties, RegistryKey<World> registryRef, DimensionType dimensionType, Supplier<Profiler> profiler, boolean isClient, boolean debugWorld, long seed) {
            super(properties, registryRef, dimensionType, profiler, isClient, debugWorld, seed);

            this.scoreboard = new Scoreboard();
            this.recipeManager = new RecipeManager();
            this.tagManager = TagManager.create(
                    TagGroup.create(new HashMap<>()),
                    TagGroup.create(new HashMap<>()),
                    TagGroup.create(new HashMap<>()),
                    TagGroup.create(new HashMap<>())
            );
            this.blockScheduler = new DummyClientTickScheduler<>();
            this.fluidScheduler = new DummyClientTickScheduler<>();
        }

        protected void assertExpect() {
            throw new AssertionError("Not expected");
        }

        @Override
        public void updateListeners(BlockPos pos, BlockState oldState, BlockState newState, int flags) {}

        @Override
        public void playSound(@Nullable PlayerEntity player, double x, double y, double z, SoundEvent sound, SoundCategory category, float volume, float pitch) {}

        @Override
        public void playSoundFromEntity(@Nullable PlayerEntity player, Entity entity, SoundEvent sound, SoundCategory category, float volume, float pitch) {}

        @Override
        public @Nullable Entity getEntityById(int id) {
            return null;
        }

        @Override
        public @Nullable MapState getMapState(String id) {
            return null;
        }

        @Override
        public void putMapState(MapState mapState) {
            this.assertExpect();
        }

        @Override
        public int getNextMapId() {
            this.assertExpect();
            return -1;
        }

        @Override
        public void setBlockBreakingInfo(int entityId, BlockPos pos, int progress) {
            this.assertExpect();
        }

        @Override
        public Scoreboard getScoreboard() {
            return this.scoreboard;
        }

        @Override
        public RecipeManager getRecipeManager() {
            return this.recipeManager;
        }

        @Override
        public TagManager getTagManager() {
            return this.tagManager;
        }

        @Override
        public TickScheduler<Block> getBlockTickScheduler() {
            return this.blockScheduler;
        }

        @Override
        public TickScheduler<Fluid> getFluidTickScheduler() {
            return this.fluidScheduler;
        }

        @Override
        public ChunkManager getChunkManager() {
            return null;
        }

        @Override
        public void syncWorldEvent(@Nullable PlayerEntity player, int eventId, BlockPos pos, int data) {}

        @Override
        public DynamicRegistryManager getRegistryManager() {
            return null;
        }

        @Override
        public float getBrightness(Direction direction, boolean shaded) {
            return 0;
        }

        @Override
        public List<? extends PlayerEntity> getPlayers() {
            return null;
        }

        @Override
        public Biome getGeneratorStoredBiome(int biomeX, int biomeY, int biomeZ) {
            return null;
        }
    }
}
