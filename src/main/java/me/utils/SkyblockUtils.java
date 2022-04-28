package me.utils;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import net.minecraft.client.Minecraft;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.scoreboard.Score;
import net.minecraft.scoreboard.ScoreObjective;
import net.minecraft.scoreboard.ScorePlayerTeam;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.util.StringUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

public class SkyblockUtils extends Utils{
    static Minecraft mc = Minecraft.getMinecraft();
    public static List<String> getSidebarLines() {
        List<String> lines = new ArrayList<>();
        if (Minecraft.getMinecraft().theWorld == null) return lines;
        Scoreboard scoreboard = Minecraft.getMinecraft().theWorld.getScoreboard();
        if (scoreboard == null) return lines;

        ScoreObjective objective = scoreboard.getObjectiveInDisplaySlot(1);
        if (objective == null) return lines;

        Collection<Score> scores = scoreboard.getSortedScores(objective);
        List<Score> list = scores.stream()
                .filter(input -> input != null && input.getPlayerName() != null && !input.getPlayerName()
                        .startsWith("#"))
                .collect(Collectors.toList());

        if (list.size() > 15) {
            scores = Lists.newArrayList(Iterables.skip(list, scores.size() - 15));
        } else {
            scores = list;
        }

        for (Score score : scores) {
            ScorePlayerTeam team = scoreboard.getPlayersTeam(score.getPlayerName());
            lines.add(ScorePlayerTeam.formatPlayerName(team, score.getPlayerName()));
        }

        return lines;
    }

    public static String cleanSB(String scoreboard) {
        char[] nvString = StringUtils.stripControlCodes(scoreboard).toCharArray();
        StringBuilder cleaned = new StringBuilder();

        for (char c : nvString) {
            if ((int) c > 20 && (int) c < 127) {
                cleaned.append(c);
            }
        }

        return cleaned.toString();
    }
    public static int getHoeCounter() {
        try {
            if (mc.thePlayer.getHeldItem().getDisplayName().contains("Turing")) {
                final ItemStack stack = Minecraft.getMinecraft().thePlayer.getHeldItem();
                if (stack != null && stack.hasTagCompound()) {
                    final NBTTagCompound tag = stack.getTagCompound();
                    if (tag.hasKey("ExtraAttributes", 10)) {
                        final NBTTagCompound ea = tag.getCompoundTag("ExtraAttributes");
                        if (ea.hasKey("mined_crops", 99)) {
                            return ea.getInteger("mined_crops");
                        } else if (ea.hasKey("farmed_cultivating", 99)) {
                            return ea.getInteger("farmed_cultivating");
                        }
                    }
                }
            }
        } catch(Exception e){
        }
        return 0;
    }
    public static int getRemainingJacobTime(){
        try {
            String myData = "";
            for (String line : SkyblockUtils.getSidebarLines()) {
                String cleanedLine = SkyblockUtils.cleanSB(line);
                if (cleanedLine.contains("Sugar Cane") || cleanedLine.contains("Mushroom")) {
                    myData = cleanedLine;
                }
            }
            myData = myData.substring(myData.lastIndexOf(" ") + 1);
            myData = myData.substring(0, myData.length() - 1);
            String[] time = myData.split("m");
            return Integer.parseInt(time[0]) * 60 + Integer.parseInt(time[1]);

        }catch(Exception e) {
        }

        return 0;
    }

    public static int getJacobEventCounter(){
        try {
            for (String line : SkyblockUtils.getSidebarLines()) {
                String cleanedLine = SkyblockUtils.cleanSB(line);
                if (cleanedLine.contains("with")) {
                    return Integer.parseInt(cleanedLine.substring(cleanedLine.lastIndexOf(" ") + 1).replace(",", ""));
                }

            }
        }catch(Exception e) {
        }
        return 0;
    }
}
