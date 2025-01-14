package com.black_dog20.servertabinfo.client.overlays;

import com.black_dog20.bml.client.DrawingContext;
import com.black_dog20.bml.client.overlay.GameOverlay;
import com.black_dog20.bml.client.rows.Row;
import com.black_dog20.bml.client.rows.RowHelper;
import com.black_dog20.bml.client.rows.columns.BlankColumn;
import com.black_dog20.bml.client.rows.columns.Column;
import com.black_dog20.bml.client.rows.columns.HeadColumn;
import com.black_dog20.bml.client.rows.columns.ITextComponentColumn;
import com.black_dog20.bml.utils.dimension.DimensionUtil;
import com.black_dog20.bml.utils.text.TextComponentBuilder;
import com.black_dog20.servertabinfo.Config;
import com.black_dog20.servertabinfo.ServerTabInfo;
import com.black_dog20.servertabinfo.client.ClientDataManager;
import com.black_dog20.servertabinfo.client.keybinds.Keybinds;
import com.black_dog20.servertabinfo.common.utils.Dimension;
import com.google.common.collect.ComparisonChain;
import com.google.common.collect.Ordering;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.ChatFormatting;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.level.GameType;
import net.minecraft.world.scores.Objective;
import net.minecraft.world.scores.PlayerTeam;
import net.minecraft.world.scores.Score;
import net.minecraft.world.scores.Scoreboard;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.gui.overlay.NamedGuiOverlay;
import net.minecraftforge.client.gui.overlay.VanillaGuiOverlay;

import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import static com.black_dog20.servertabinfo.common.utils.Translations.*;

@OnlyIn(Dist.CLIENT)
public class PlayerListOverlay extends GameOverlay.Pre {

    private static final Ordering<PlayerInfo> ENTRY_ORDERING = Ordering.from(new PlayerComparator());
    private Minecraft minecraft;
    private Font fontRenderer;
    private ItemRenderer itemRenderer;
    private long lastRenderTime = Util.getMillis();
    private int ticks = 0;
    private int page = 1;

    public PlayerListOverlay() {
        this.minecraft = Minecraft.getInstance();
        this.fontRenderer = minecraft.font;
        this.itemRenderer = minecraft.getItemRenderer();
    }

    @Override
    public void onRender(PoseStack matrixStack, int width, int height) {
        if(Keybinds.SHOW.isDown())
            return;

        int y = 10;
        int z = 0;
        if (Util.getMillis() - 2000 > lastRenderTime) {
            page = 1;
            ticks = 1;
        }
        int itemsPerPage = (int) Math.floor((height - 7 * y) / fontRenderer.lineHeight);
        List<Row> rows = getRows();

        int maxPages = (int) Math.ceil(rows.size() / (double) itemsPerPage);

        if (ticks % 300 == 0) {
            if (page >= maxPages)
                page = 1;
            else
                page++;
            ticks = 1;
        }

        rows = getPagedRows(rows, itemsPerPage);
        int maxWidth = RowHelper.getMaxWidth(rows);
        int x = width / 2 - maxWidth / 2;


        int i3 = rows.size();
        int j3 = i3;

        int k3;
        for(k3 = 1; j3 > 20; j3 = (i3 + k3 - 1) / k3) {
            ++k3;
        }

        int i1 = Math.min(k3 * ((true ? 9 : 0) + 200 + 0 + 13), width - 50) / k3;
        int j1 = width / 2 - (i1 * k3 + (k3 - 1) * 5) / 2;
        int l1 = i1 * k3 + (k3 - 1) * 5;



        Component header = Minecraft.getInstance().gui.getTabList().header;
        Component footer = Minecraft.getInstance().gui.getTabList().footer;
        List<FormattedCharSequence> list1 = null;
        if (header != null) {
            list1 = this.minecraft.font.split(header, width - 50);

            for(FormattedCharSequence formattedcharsequence : list1) {
                l1 = Math.max(l1, this.minecraft.font.width(formattedcharsequence));
            }
        }

        List<FormattedCharSequence> list2 = null;
        if (footer != null) {
            list2 = this.minecraft.font.split(footer, width - 50);

            for(FormattedCharSequence formattedcharsequence1 : list2) {
                l1 = Math.max(l1, this.minecraft.font.width(formattedcharsequence1));
            }
        }

        if (list1 != null) {
            Gui.fill(matrixStack, width / 2 - l1 / 2 - 1, y - 1, width / 2 + l1 / 2 + 1, y + list1.size() * 9, Integer.MIN_VALUE);

            for(FormattedCharSequence formattedcharsequence2 : list1) {
                int i2 = this.minecraft.font.width(formattedcharsequence2);
                this.minecraft.font.drawShadow(matrixStack, formattedcharsequence2, (float)(width / 2 - i2 / 2), (float)y, -1);
                y += 9;
            }

            ++y;
        }

        DrawingContext drawingContext = new DrawingContext(matrixStack, width, height, x, y, z, fontRenderer, itemRenderer);
        Gui.fill(matrixStack, width / 2 - l1 / 2 - 1, y - 1, width / 2 + l1 / 2 + 1, y + rows.size() * 9, Integer.MIN_VALUE);
        y = RowHelper.drawRowsWithBackground(drawingContext, rows);
        //fontRenderer.drawShadow(matrixStack, PAGE.get(page, maxPages), width / 2 + 2, y + 2, -1);

        if (list2 != null) {
            y += j3;
            Gui.fill(matrixStack, width / 2 - l1 / 2 - 1, y - 1, width / 2 + l1 / 2 + 1, y + list2.size() * 9, Integer.MIN_VALUE);

            for(FormattedCharSequence formattedcharsequence3 : list2) {
                int k4 = this.minecraft.font.width(formattedcharsequence3);
                this.minecraft.font.drawShadow(matrixStack, formattedcharsequence3, (float)(width / 2 - k4 / 2), (float)y, -1);
                y += 9;
            }
        }

        ticks++;
        lastRenderTime = Util.getMillis();
    }

    @Override
    public boolean doRender(NamedGuiOverlay overlay) {
        if (overlay.id().equals(VanillaGuiOverlay.PLAYER_LIST.id())) {
            Objective scoreboard = minecraft.level.getScoreboard().getDisplayObjective(0);
            ClientPacketListener handler = minecraft.player.connection;
            boolean shouldShowTabList = (minecraft.options.keyPlayerList.isDown() && (!minecraft.isLocalServer() || handler.getOnlinePlayers().size() > 1 || scoreboard != null));
            return shouldShowTabList && Config.REPLACE_PLAYER_LIST.get();
        }

        return false;
    }

    @Override
    public boolean doesCancelEvent() {
        return true;
    }

    private List<Row> getRows() {
        ClientPacketListener nethandlerplayclient = this.minecraft.player.connection;
        List<PlayerInfo> list = ENTRY_ORDERING.<PlayerInfo>sortedCopy(nethandlerplayclient.getOnlinePlayers());

        return list.stream()
                .map(this::buildRow)
                .collect(Collectors.toCollection(LinkedList::new));
    }

    private Row buildRow(PlayerInfo playerInfo) {
        Row.RowBuilder builder = new Row.RowBuilder();
        Scoreboard scoreboard = Optional.ofNullable(minecraft.level)
                .map(ClientLevel::getScoreboard)
                .orElse(null);

        return builder
                .withColumn(HeadColumn.of("head", playerInfo))
                .withColumn(ITextComponentColumn.of("name", getPlayerName(playerInfo)))
                .withColumn(BlankColumn.of("nameSpace", 6))
                .withColumn(ITextComponentColumn.of("dim", getPlayerDim(playerInfo)))
                .withColumn(BlankColumn.of("dimSpace", 3))
                .withColumn(ITextComponentColumn.of("score", getPlayerScore(playerInfo, scoreboard)), showScoreObjective(scoreboard))
                .withColumn(BlankColumn.of("scoreSpace", 3), showScoreObjective(scoreboard))
                .withColumn(ITextComponentColumn.of("ping", getPlayerPing(playerInfo), Column.Alignment.RIGHT))
                .withColumn(BlankColumn.of("pingSpace", 1))
                .build();
    }

    private Component getPlayerScore(PlayerInfo playerInfo, Scoreboard scoreboard) {
        Objective scoreObjective = Optional.ofNullable(scoreboard)
                .map(s -> s.getDisplayObjective(0))
                .orElse(null);

        return TextComponentBuilder.of("")
                .with(getScore(playerInfo, scoreboard, scoreObjective), showPlayerScore(playerInfo, scoreObjective))
                .format(ChatFormatting.YELLOW)
                .build();
    }

    private Supplier<Boolean> showPlayerScore(PlayerInfo playerInfo, Objective scoreObjective) {
        return () -> scoreObjective != null && GameType.SPECTATOR != playerInfo.getGameMode();
    }

    private int getScore(PlayerInfo playerInfo, Scoreboard scoreboard, Objective scoreObjective) {
        if (scoreObjective != null) {
            Score score = scoreboard.getOrCreatePlayerScore(playerInfo.getProfile().getName(), scoreObjective);
            return score.getScore();
        } else {
            return 0;
        }
    }

    private Supplier<Boolean> showScoreObjective(Scoreboard scoreboard) {
        return () -> scoreboard != null && scoreboard.getDisplayObjective(0) != null;
    }

    private boolean isAllowed() {
        return !Config.OP_ONLY_MODE.get() || minecraft.hasSingleplayerServer() || Optional.ofNullable(minecraft.player)
                .map(player -> player.hasPermissions(1))
                .orElse(false);
    }

    private List<Row> getPagedRows(List<Row> rows, int itemsPerPage) {
        rows = rows.stream()
                .skip((page - 1) * itemsPerPage)
                .limit(itemsPerPage)
                .collect(Collectors.toCollection(LinkedList::new));
        return rows;
    }

    private Component getPlayerName(PlayerInfo playerInfo) {
        return Optional.ofNullable(playerInfo)
                .map(PlayerInfo::getTabListDisplayName)
                .orElseGet(() -> PlayerTeam.formatNameForTeam(playerInfo.getTeam(), Component.literal(playerInfo.getProfile().getName())));
    }

    private Component getPlayerPing(PlayerInfo playerInfo) {
        return TextComponentBuilder.of(playerInfo.getLatency())
                .with(MS)
                .build();
    }

    private Component getPlayerDim(PlayerInfo playerInfo) {
        ResourceLocation dimName = ClientDataManager.PLAYER_DIMENSIONS.getOrDefault(playerInfo.getProfile().getId(), null);
        return TextComponentBuilder.of(getDimensionName(dimName))
                .with(getDimensionTps(dimName), this::isAllowed)
                .build();

    }

    private Component getDimensionName(ResourceLocation dimensionName) {
        if (dimensionName == null)
            return UNKOWN.get();

        if (ClientDataManager.DIMENSION_NAME_CACHE.containsKey(dimensionName))
            return ClientDataManager.DIMENSION_NAME_CACHE.get(dimensionName);

        MutableComponent name = DimensionUtil.getFormattedDimensionName(dimensionName, ServerTabInfo.MOD_ID);
        ClientDataManager.DIMENSION_NAME_CACHE.put(dimensionName, name);
        return name;
    }

    private Component getDimensionTps(ResourceLocation dimensionName) {
        if (dimensionName == null)
            return Component.literal("");
        Dimension dimension = ClientDataManager.DIMENSIONS.stream()
                .filter(d -> d.name.equals(dimensionName))
                .findFirst()
                .orElse(null);
        if (dimension == null)
            return Component.literal("");

        int tps = dimension.tps;
        ChatFormatting color = ChatFormatting.GREEN;

        if (tps < 20)
            color = ChatFormatting.YELLOW;
        if (tps <= 10)
            color = ChatFormatting.RED;

        return TextComponentBuilder.of("(")
                .with(tps)
                .format(color)
                .with(")")
                .build();
    }

    @OnlyIn(Dist.CLIENT)
    static class PlayerComparator implements Comparator<PlayerInfo> {

        public int compare(PlayerInfo player1, PlayerInfo player2) {
            PlayerTeam scoreplayerteam1 = player1.getTeam();
            PlayerTeam scoreplayerteam2 = player2.getTeam();
            return ComparisonChain.start()
                    .compareTrueFirst(player1.getGameMode() != GameType.SPECTATOR, player2.getGameMode() != GameType.SPECTATOR)
                    .compare(getName(scoreplayerteam1), getName(scoreplayerteam2))
                    .compare(player1.getProfile().getName(), player2.getProfile().getName())
                    .result();
        }

        private String getName(PlayerTeam scorePlayerTeam) {
            return Optional.ofNullable(scorePlayerTeam)
                    .map(PlayerTeam::getName)
                    .orElse("");
        }
    }
}
