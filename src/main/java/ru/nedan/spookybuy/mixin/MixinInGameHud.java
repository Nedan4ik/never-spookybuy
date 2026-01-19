package ru.nedan.spookybuy.mixin;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.mojang.datafixers.util.Pair;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.scoreboard.ScoreboardObjective;
import net.minecraft.scoreboard.ScoreboardPlayerScore;
import net.minecraft.scoreboard.Team;
import net.minecraft.text.LiteralText;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.ChatUtil;
import net.minecraft.util.Formatting;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import ru.nedan.spookybuy.SpookyBuy;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

@Mixin(InGameHud.class)
public abstract class MixinInGameHud {

    @Shadow public abstract TextRenderer getFontRenderer();

    @Shadow private int scaledHeight;
    @Shadow private int scaledWidth;
    @Shadow @Final private MinecraftClient client;

    /**
     * @author nedan4ik
     * @reason для обновления баланса
     */
    @Overwrite
    private void renderScoreboardSidebar(MatrixStack matrices, ScoreboardObjective objective) {
        Scoreboard scoreboard = objective.getScoreboard();
        Collection<ScoreboardPlayerScore> collection = scoreboard.getAllPlayerScores(objective);
        List<ScoreboardPlayerScore> list = collection.stream().filter((scoreboardPlayerScorex) -> scoreboardPlayerScorex.getPlayerName() != null && !scoreboardPlayerScorex.getPlayerName().startsWith("#")).collect(Collectors.toList());
        if (list.size() > 15) {
            collection = Lists.newArrayList(Iterables.skip(list, collection.size() - 15));
        } else {
            collection = list;
        }

        List<Pair<ScoreboardPlayerScore, Text>> list2 = Lists.newArrayListWithCapacity(collection.size());
        Text text = objective.getDisplayName();
        int i = this.getFontRenderer().getWidth(text);
        int j = i;
        int k = this.getFontRenderer().getWidth(": ");

        ScoreboardPlayerScore scoreboardPlayerScore;
        MutableText text2;
        for (Iterator<ScoreboardPlayerScore> var11 = ( collection).iterator(); var11.hasNext(); j = Math.max(j, this.getFontRenderer().getWidth(text2) + k + this.getFontRenderer().getWidth(Integer.toString(scoreboardPlayerScore.getScore())))) {
            scoreboardPlayerScore = var11.next();
            Team team = scoreboard.getPlayerTeam(scoreboardPlayerScore.getPlayerName());
            text2 = Team.decorateName(team, new LiteralText(scoreboardPlayerScore.getPlayerName()));
            list2.add(Pair.of(scoreboardPlayerScore, text2));
        }

        int l = collection.size() * 9;
        int m = this.scaledHeight / 2 + l / 3;
        int o = this.scaledWidth - j - 3;
        int p = 0;
        int q = this.client.options.getTextBackgroundColor(0.3F);
        int r = this.client.options.getTextBackgroundColor(0.4F);

        for (Pair<ScoreboardPlayerScore, Text> pair : list2) {
            ++p;
            ScoreboardPlayerScore scoreboardPlayerScore2 = pair.getFirst();
            Text text3 = pair.getSecond();
            String string = Formatting.RED + "" + scoreboardPlayerScore2.getScore();
            int t = m - p * 9;
            int u = this.scaledWidth - 3 + 2;
            int var10001 = o - 2;
            Screen.fill(matrices, var10001, t, u, t + 9, q);
            this.getFontRenderer().draw(matrices, text3, (float) o, (float) t, -1);
            this.getFontRenderer().draw(matrices, string, (float) (u - this.getFontRenderer().getWidth(string)), (float) t, -1);
            SpookyBuy.getInstance().getAutoBuy().onSBLine(ChatUtil.stripTextFormat(text3.getString()));
            if (p == collection.size()) {
                var10001 = o - 2;
                Screen.fill(matrices, var10001, t - 9 - 1, u, t - 1, r);
                Screen.fill(matrices, o - 2, t - 1, u, t, q);
                float var10003 = (float) (o + j / 2 - i / 2);
                this.getFontRenderer().draw(matrices, text, var10003, (float) (t - 9), -1);
            }
        }

    }
}
