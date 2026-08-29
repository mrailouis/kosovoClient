package com.mrailouis.kosovoclient.features.impl.player;

import com.mrailouis.kosovoclient.features.BooleanSetting;
import com.mrailouis.kosovoclient.features.Category;
import com.mrailouis.kosovoclient.features.ModeSetting;
import com.mrailouis.kosovoclient.features.Module;
import com.mrailouis.kosovoclient.features.NumberSetting;
import com.mrailouis.kosovoclient.features.impl.player.autogg.PatternHandler;
import com.mrailouis.kosovoclient.features.impl.player.autogg.PlaceholderAPI;
import com.mrailouis.kosovoclient.features.impl.player.autogg.Server;
import com.mrailouis.kosovoclient.features.impl.player.autogg.Trigger;
import com.mrailouis.kosovoclient.features.impl.player.autogg.TriggersSchema;
import com.mrailouis.kosovoclient.features.impl.player.autogg.WebHandler;
import lombok.Getter;
import net.minecraft.client.Minecraft;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.EnumChatFormatting;
import net.minecraftforge.client.event.ClientChatReceivedEvent;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

@Getter
public class AutoGG extends Module {
    private static final String[] PRIMARY_STRINGS = {"gg", "GG", "gf", "Good Game", "Good Fight", "Good Round! :D"};
    private static final String[] SECONDARY_STRINGS = {"Have a good day!", "<3", "gf", "Good Fight", "Good Round", ":D", "Well played!", "wp"};
    private static final String TRIGGERS_URL = "https://static.sk1er.club/autogg/regex_triggers_3.json";
    private static final ScheduledExecutorService POOL = Executors.newScheduledThreadPool(2);
    private static final AutoGG INSTANCE = new AutoGG();

    private final ModeSetting primaryPhrase = new ModeSetting("Phrase", "Main end-game GG message.", "gg", PRIMARY_STRINGS);
    private final NumberSetting delaySeconds = new NumberSetting("Delay (s)", "Delay in seconds before sending GG.", 1.0, 0.0, 5.0, 0.5);
    private final BooleanSetting casual = new BooleanSetting("Casual AutoGG", "Send GG for events that do not award karma.", false);
    private final BooleanSetting secondMessage = new BooleanSetting("Second Message", "Send a second follow-up message.", false);
    private final ModeSetting secondaryPhrase = new ModeSetting("Second Phrase", "Follow-up message to send.", "Have a good day!", SECONDARY_STRINGS);
    private final NumberSetting secondDelay = new NumberSetting("Second Delay (s)", "Delay in seconds after first message.", 1.0, 0.5, 5.0, 0.5);
    private final BooleanSetting antiGG = new BooleanSetting("Anti GG", "Hide other players' GG messages from chat.", false);
    private final BooleanSetting antiKarma = new BooleanSetting("Anti Karma", "Hide 'You earned +X Karma!' messages from chat.", false);
    private final BooleanSetting feedback = new BooleanSetting("Feedback", "Show notification when GG is triggered.", true);

    private TriggersSchema triggers;
    private volatile Server currentServer;
    private long lastGGTime = 0L;

    public static AutoGG getInstance() {
        return INSTANCE;
    }

    private AutoGG() {
        super("Auto GG", "Automatically says GG at the end of a game on supported servers like Hypixel.", Category.PLAYER, true);
        registerSetting(this.primaryPhrase);
        registerSetting(this.delaySeconds);
        registerSetting(this.casual);
        registerSetting(this.secondMessage);
        registerSetting(this.secondaryPhrase);
        registerSetting(this.secondDelay);
        registerSetting(this.antiGG);
        registerSetting(this.antiKarma);
        registerSetting(this.feedback);

        initTriggers();
    }

    private void initTriggers() {
        Set<String> joined = new HashSet<String>();
        joined.addAll(Arrays.asList(PRIMARY_STRINGS));
        joined.addAll(Arrays.asList(SECONDARY_STRINGS));
        PlaceholderAPI.getInstance().registerPlaceholder("antigg_strings", String.join("|", joined));

        initFallbackTriggers();

        POOL.submit(() -> {
            try {
                TriggersSchema remote = WebHandler.fetchJson(TRIGGERS_URL, TriggersSchema.class);
                if (remote != null && remote.getServers() != null && remote.getServers().length > 0) {
                    triggers = remote;
                    for (Server s : remote.getServers()) {
                        if (s.getTriggers() != null) {
                            for (Trigger t : s.getTriggers()) {
                                PatternHandler.getInstance().getOrRegisterPattern(t.getPattern());
                            }
                        }
                    }
                }
            } catch (Exception ignored) {
            }
        });
    }

    private void initFallbackTriggers() {
        Trigger[] fallbackTriggers = new Trigger[]{
                new Trigger(0, "^ +1st Killer - ?\\[?\\w*\\+*\\]? \\w+ - \\d+(?: Kills?)?$"),
                new Trigger(0, "^ *1st (?:Place ?)?(?:-|:)? ?\\[?\\w*\\+*\\]? \\w+(?: : \\d+| - \\d+(?: Points?)?| - \\d+(?: x .)?| \\(\\w+ .{1,6}\\) - \\d+ Kills?|: \\d+:\\d+| - \\d+ (?:Zombie )?(?:Kills?|Blocks? Destroyed)| - \\[LINK\\])?$"),
                new Trigger(0, "^ +Winn(?:er #1 \\(\\d+ Kills\\): \\w+ \\(\\w+\\)|er(?::| - )(?:Hiders|Seekers|Defenders|Attackers|PLAYERS?|MURDERERS?|Red|Blue|RED|BLU|\\w+)(?: Team)?|ers?: ?\\[?\\w*\\+*\\]? \\w+(?:, ?\\[?\\w*\\+*\\]? \\w+)?|ing Team ?[\\:-] (?:Animals|Hunters|Red|Green|Blue|Yellow|RED|BLU|Survivors|Vampires))$"),
                new Trigger(0, "^ +Alpha Infected: \\w+ \\(\\d+ infections?\\)$"),
                new Trigger(0, "^ +Murderer: \\w+ \\(\\d+ Kills?\\)$"),
                new Trigger(0, "^ +You survived \\d+ rounds!$"),
                new Trigger(0, "^ +(?:UHC|SkyWars|Bridge|Sumo|Classic|OP|MegaWalls|Bow|NoDebuff|Blitz|Combo|Bow Spleef) (?:Duel|Doubles|3v3|4v4|Teams|Deathmatch|2v2v2v2|3v3v3v3)? ?- \\d+:\\d+$"),
                new Trigger(0, "^ +They captured all wools!$"),
                new Trigger(2, "^\\[?\\w*\\+*\\]? \\w+: (?:${antigg_strings})$"),
                new Trigger(3, "^\\+5 Karma!.*$")
        };

        Server hypixelFallback = new Server(
                "Hypixel Server",
                "SERVER_BRANDING",
                "Hypixel BungeeCord \\(.+\\) <- .+",
                "/ac",
                fallbackTriggers
        );

        this.triggers = new TriggersSchema(new Server[]{hypixelFallback});
        for (Trigger t : fallbackTriggers) {
            PatternHandler.getInstance().getOrRegisterPattern(t.getPattern());
        }
    }

    @SubscribeEvent
    public void onEntityJoinWorld(EntityJoinWorldEvent event) {
        if (!isEnabled()) {
            return;
        }

        Minecraft mc = Minecraft.getMinecraft();
        if (event.entity == mc.thePlayer) {
            POOL.submit(() -> {
                if (triggers != null && triggers.getServers() != null) {
                    for (Server s : triggers.getServers()) {
                        if (s.detect()) {
                            currentServer = s;
                            return;
                        }
                    }
                }
                currentServer = null;
            });
        }
    }

    @SubscribeEvent
    public void onClientChatReceived(ClientChatReceivedEvent event) {
        if (event.type == 2) {
            return;
        }

        if (!isEnabled()) {
            return;
        }

        Minecraft mc = Minecraft.getMinecraft();
        if (mc.thePlayer == null) {
            return;
        }

        String stripped = EnumChatFormatting.getTextWithoutFormattingCodes(event.message.getUnformattedText());
        if (stripped == null || stripped.isEmpty()) {
            return;
        }

        Server s = this.currentServer;
        if (s == null) {
            if (isHypixel(mc)) {
                if (this.triggers != null && this.triggers.getServers() != null && this.triggers.getServers().length > 0) {
                    s = this.triggers.getServers()[0];
                    this.currentServer = s;
                }
            }
        }

        if (s == null) {
            return;
        }

        Trigger[] triggerList = s.getTriggers();
        if (triggerList == null) {
            return;
        }

        for (Trigger trigger : triggerList) {
            switch (trigger.getType()) {
                case ANTI_GG:
                    if (this.antiGG.isEnabled()) {
                        Pattern p = PatternHandler.getInstance().getOrRegisterPattern(trigger.getPattern());
                        if (p != null && p.matcher(stripped).matches()) {
                            event.setCanceled(true);
                            return;
                        }
                    }
                    break;
                case ANTI_KARMA:
                    if (this.antiKarma.isEnabled()) {
                        Pattern p = PatternHandler.getInstance().getOrRegisterPattern(trigger.getPattern());
                        if (p != null && p.matcher(stripped).matches()) {
                            event.setCanceled(true);
                            return;
                        }
                    }
                    break;
                default:
                    break;
            }
        }

        final Server activeServer = s;
        POOL.submit(() -> {
            for (Trigger trigger : triggerList) {
                switch (trigger.getType()) {
                    case NORMAL: {
                        Pattern p = PatternHandler.getInstance().getOrRegisterPattern(trigger.getPattern());
                        if (p != null && p.matcher(stripped).matches()) {
                            invokeGG(activeServer);
                            return;
                        }
                        break;
                    }
                    case CASUAL: {
                        if (casual.isEnabled()) {
                            Pattern p = PatternHandler.getInstance().getOrRegisterPattern(trigger.getPattern());
                            if (p != null && p.matcher(stripped).matches()) {
                                invokeGG(activeServer);
                                return;
                            }
                        }
                        break;
                    }
                    default:
                        break;
                }
            }
        });
    }

    private void invokeGG(Server server) {
        long now = System.currentTimeMillis();
        if (now - this.lastGGTime < 10000L) {
            return;
        }
        this.lastGGTime = now;

        String prefix = server.getMessagePrefix();
        String ggMsg = this.primaryPhrase.getValue();
        long delay = (long) (this.delaySeconds.getValue() * 1000.0);

        POOL.schedule(() -> {
            Minecraft mc = Minecraft.getMinecraft();
            if (mc.thePlayer != null) {
                mc.thePlayer.sendChatMessage(prefix == null || prefix.isEmpty() ? ggMsg : prefix + " " + ggMsg);
                if (feedback.isEnabled()) {
                    mc.thePlayer.addChatMessage(new ChatComponentText("§a[Kosovo] §fAutoGG: " + ggMsg));
                }
            }
        }, delay, TimeUnit.MILLISECONDS);

        if (this.secondMessage.isEnabled()) {
            String secondMsg = this.secondaryPhrase.getValue();
            long secDelay = delay + (long) (this.secondDelay.getValue() * 1000.0);
            POOL.schedule(() -> {
                Minecraft mc = Minecraft.getMinecraft();
                if (mc.thePlayer != null) {
                    mc.thePlayer.sendChatMessage(prefix == null || prefix.isEmpty() ? secondMsg : prefix + " " + secondMsg);
                }
            }, secDelay, TimeUnit.MILLISECONDS);
        }
    }

    private boolean isHypixel(Minecraft mc) {
        if (mc.isSingleplayer()) {
            return false;
        }
        if (mc.getCurrentServerData() != null && mc.getCurrentServerData().serverIP != null) {
            String ip = mc.getCurrentServerData().serverIP.toLowerCase().trim();
            if (ip.contains(":")) {
                ip = ip.split(":")[0];
            }
            return ip.equals("hypixel.net") || ip.endsWith(".hypixel.net");
        }
        return false;
    }
}
