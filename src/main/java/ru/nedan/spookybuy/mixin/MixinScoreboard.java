package ru.nedan.spookybuy.mixin;

import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.scoreboard.Team;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

import java.util.Map;

@Mixin(Scoreboard.class)
public abstract class MixinScoreboard {

    @Shadow public abstract @Nullable Team getPlayerTeam(String playerName);

    @Shadow @Final private Map<String, Team> teamsByPlayer;

    @Shadow @Final private Map<String, Team> teams;

    @Shadow public abstract void updateRemovedTeam(Team team);

    /**
     * @author nedan4ik
     * @reason чтобы не было спама в консоль
     */
    @Overwrite
    public void removePlayerFromTeam(String playerName, Team team) {
        if (this.getPlayerTeam(playerName) == team) {
            this.teamsByPlayer.remove(playerName);
            team.getPlayerList().remove(playerName);
        }
    }

    /**
     * @author nedan4ik
     * @reason чтобы не было спама в консоль
     */
    @Overwrite
    public void removeTeam(Team team) {
        if (team == null) return;

        this.teams.remove(team.getName());

        for (String string : team.getPlayerList()) {
            this.teamsByPlayer.remove(string);
        }

        this.updateRemovedTeam(team);
    }
}
