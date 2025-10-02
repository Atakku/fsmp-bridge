// Copyright 2025 Atakku <https://atakku.dev>
//
// This project is dual licensed under MIT and Apache.

package dev.atakku.fsmp.bridge;

import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;
import java.util.regex.Pattern;

import net.minecraft.network.chat.ChatType;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.OutgoingChatMessage;
import net.minecraft.network.chat.PlayerChatMessage;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.stats.Stats;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.GameRules;

import club.minnced.discord.webhook.WebhookClientBuilder;
import club.minnced.discord.webhook.external.JDAWebhookClient;
import club.minnced.discord.webhook.send.AllowedMentions;
import club.minnced.discord.webhook.send.WebhookEmbedBuilder;
import club.minnced.discord.webhook.send.WebhookMessageBuilder;
import com.vdurmont.emoji.EmojiParser;
import it.unimi.dsi.fastutil.objects.Object2ObjectArrayMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.Message.Attachment;
import net.dv8tion.jda.api.entities.emoji.RichCustomEmoji;
import net.dv8tion.jda.api.events.guild.GuildReadyEvent;
import net.dv8tion.jda.api.events.guild.member.GuildMemberJoinEvent;
import net.dv8tion.jda.api.events.guild.member.GuildMemberUpdateEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.player.AdvancementEvent.AdvancementEarnEvent;
import net.neoforged.neoforge.event.server.ServerStartedEvent;
import net.neoforged.neoforge.event.server.ServerStoppedEvent;
import net.neoforged.neoforge.event.server.ServerStoppingEvent;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Mod(Bridge.MOD_ID)
public class Bridge {
  public static final String MOD_ID = "fsmp_bridge";
  public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

  public static final String CHANNEL_ID = System.getenv("DISCORD_CHANNEL_ID");
  public static final String OWNER = System.getenv("DISCORD_OWNER_ID");

  public static final JDAWebhookClient WEBHOOK;
  static {
    String THREAD_ID = System.getenv("DISCORD_THREAD_ID");
    JDAWebhookClient HOOK = new WebhookClientBuilder(System.getenv("DISCORD_WEBHOOK")).buildJDA();
    if (!THREAD_ID.isEmpty()) {
      WEBHOOK = HOOK.onThread(Long.parseLong(THREAD_ID));
    } else {
      WEBHOOK = HOOK;
    }
  }

  public static final JDA JDA = JDABuilder.createDefault(System.getenv("DISCORD_TOKEN"))
      .enableIntents(GatewayIntent.MESSAGE_CONTENT)
      .enableIntents(GatewayIntent.GUILD_MEMBERS)
      .addEventListeners(new ListenerAdapter() {
        @Override
        public void onGuildReady(GuildReadyEvent e) {
          e.getGuild().loadMembers(m -> {
            DISCORD_CACHE.put(m.getId(), m);
          });
        }

        public void onGuildMemberUpdate(GuildMemberUpdateEvent e) {
          DISCORD_CACHE.put(e.getMember().getId(), e.getMember());
        }

        public void onGuildMemberJoin(GuildMemberJoinEvent e) {
          DISCORD_CACHE.put(e.getMember().getId(), e.getMember());
        }
      })
      .build();

  private static String CHARSET = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789_.";

  private static HashMap<UUID, String> NAME_CACHE = new HashMap<>();
  private static HashMap<UUID, String> ID_CACHE = new HashMap<>();
  private static Random R = new Random();

  private static String cacheName(UUID uuid, String name, String id) {
    if (name.length() > 16) {
      name = name.substring(0, 16);
    }
    NAME_CACHE.remove(uuid);
    ID_CACHE.remove(uuid);
    if (NAME_CACHE.containsValue(name)) {
      if (name.length() >= 15) {
        name = name.substring(0, 14);
      }
      return cacheName(uuid,
          name + CHARSET.charAt(R.nextInt(CHARSET.length())) + CHARSET.charAt(R.nextInt(CHARSET.length())), id);
    }
    NAME_CACHE.put(uuid, name);
    ID_CACHE.put(uuid, id);
    return NAME_CACHE.get(uuid);
  }

  public static String getUserData(UUID uuid, boolean force) {
    if (uuid == null)
      return null;
    if (force || !NAME_CACHE.containsKey(uuid)) {
      try {
        URL url = new URI("https://link.neko.rs/whitelist?uuid=" + uuid.toString()).toURL();
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        if (conn.getResponseCode() == 200) {
          String[] data = IOUtils.toString(conn.getInputStream(), "UTF-8").split("\n");

          String name = data[0];
          String id = data[1];

          Member m = DISCORD_CACHE.get(id);
          if (m != null) {
            String fancy = m.getEffectiveName().replace(" ", "_").replace("__", "_").replaceAll("[^a-zA-Z0-9_.]", "");
            if (fancy.length() > 1) {
              name = fancy;
            }
          }

          return cacheName(uuid, name, id);
        }
      } catch (Exception ex) {
        Bridge.LOGGER.error(ex.getMessage());
        ex.printStackTrace();
      }
      if (!NAME_CACHE.containsKey(uuid)) {
        NAME_CACHE.put(uuid, null);
      }
    }
    return NAME_CACHE.get(uuid);
  }

  private static Object2ObjectOpenHashMap<String, Member> DISCORD_CACHE = new Object2ObjectOpenHashMap<>();

  public Bridge(IEventBus bus) {
    LOGGER.info("Initializing FSMP Bridge");
    sendSystemText("🟡 Server is starting");

    NeoForge.EVENT_BUS.addListener(this::onServerStart);
    NeoForge.EVENT_BUS.addListener(this::onServerStopping);
    NeoForge.EVENT_BUS.addListener(this::onServerStopped);
    bus.addListener(this::onPlayerAdvancement);
  }

  private void onServerStart(ServerStartedEvent event) {
    sendSystemText("🟢 Server started");
    JDA.addEventListener(new ListenerAdapter() {
      @Override
      public void onMessageReceived(MessageReceivedEvent e) {
        if (e.getAuthor().getIdLong() == WEBHOOK.getId())
          return;
        if (e.getChannel().getId().equals(CHANNEL_ID)) {
          String text = "";
          if (e.getMessage().getMessageReference() != null) {
            Message m = e.getMessage().getMessageReference().getMessage();
            if (m != null)
              text += "Replying to " + m.getAuthor().getEffectiveName() + ": ";
          }
          text += EmojiParser.parseToAliases(e.getMessage().getContentDisplay());
          for (Attachment at : e.getMessage().getAttachments()) {
            boolean nsfw = at.getFileName().startsWith("SPOILER_");
            text += " [[CICode,url=" + at.getUrl() + ",name=" + at.getFileName() + ",nsfw=" + nsfw + "]]";
          }
          broadcastMessage(event.getServer(), e.getMessage().getAuthor().getEffectiveName(), text);
        }
      }
    });
  }

  private void onServerStopping(ServerStoppingEvent event) {
    sendSystemText("🔴 Server is stopping");
  }

  private void onServerStopped(ServerStoppedEvent event) {
    sendSystemText("🛑 Server stopped");
    JDA.shutdown();
  }

  public static void onPlayerJoin(ServerPlayer player, boolean firstJoin) {
    sendSystemText("📥 **%s** joined the game (%s)", pingOrFallback(player), getPlayTime(player));
  }

  public static void onPlayerLeft(ServerPlayer player, Component reason) {
    sendSystemText("📤 **%s** left the game (%s)", pingOrFallback(player), reason.getString());
  }

  public static void onPlayerMessage(ServerPlayer player, PlayerChatMessage msg) {
    sendPlayerText(player, parseCustom(msg.decoratedContent().getString()));
  }

  private void onPlayerAdvancement(AdvancementEarnEvent event) {
    if (event.getEntity() instanceof ServerPlayer player) {
      event.getAdvancement().value().display().ifPresent(disp -> {
        if (disp.shouldAnnounceChat()
            && player.level().getGameRules().getBoolean(GameRules.RULE_ANNOUNCE_ADVANCEMENTS)) {
          switch (disp.getType()) {
            case TASK:
              sendSystemText("✨ **%s** has made the advancement **[%s]**", pingOrFallback(player),
                  disp.getTitle().getString());
              break;
            case CHALLENGE:
              sendSystemText("🎉 %s has completed the challenge **[%s]**", pingOrFallback(player),
                  disp.getTitle().getString());
              break;
            case GOAL:
              sendSystemText("🎊 **%s** has reached the goal **[%s]**", pingOrFallback(player),
                  disp.getTitle().getString());
              break;
          }
        }
      });
    }
  }

  public static void onPlayerDeath(ServerPlayer player, DamageSource source) {
    Component textDM = player.getCombatTracker().getDeathMessage();
    // if (textDM instanceof TranslatableText) {
    // TranslatableText tt_dm = (TranslatableText) textDM;
    // List<String> args = Arrays.stream(tt_dm.getArgs()).map(a ->
    // String.format("**%s**", a instanceof Text ? ((Text) a).getString() :
    // a.toString())).collect(Collectors.toList());
    // textDM = Text.translatable(tt_dm.getKey(), args.toArray());
    // }
    Map<String, UUID> temp = new Object2ObjectArrayMap<>();
    for (Map.Entry<UUID, String> entry : NAME_CACHE.entrySet()) {
      temp.put(entry.getValue(), entry.getKey());
    }

    ObjectArrayList<String> words = new ObjectArrayList<>(textDM.getString().split(" "));
    words.replaceAll(n -> temp.get(n) != null ? pingOrFallback(temp.get(n), n) : n);
    sendSystemText("💀 %s", String.join(" ", words));
  }

  private static String pingOrFallback(UUID uuid, String fallback) {
    String id = ID_CACHE.get(uuid);
    return id != null ? "<@" + id + ">" : "**" + fallback + "**";
  }

  private static String pingOrFallback(ServerPlayer e) {
    return pingOrFallback(e.getUUID(), e.getName().tryCollapseToString());
  }

  private static String getPlayTime(ServerPlayer player) {
    int pt = player.getStats().getValue(Stats.CUSTOM.get(Stats.PLAY_TIME)) / 20;
    int months = pt / 2592000;
    int days = pt % 2592000 / 86400;
    int hours = pt % 86400 / 3600;
    int mins = pt % 3600 / 60;
    int secs = pt % 60;

    if (months > 0)
      return i18n(months, "month");
    if (days > 0)
      return i18n(days, "day");
    if (hours > 0)
      return i18n(hours, "hour");
    if (mins > 0)
      return i18n(mins, "min");
    return i18n(secs, "sec");
  }

  private static String i18n(int num, String name) {
    return num + " " + name + (num > 1 ? "s" : "");
  }

  private static WebhookMessageBuilder getPlayerHook(Player p) {
    return new WebhookMessageBuilder().setAllowedMentions(AllowedMentions.none())
        .setUsername(p.getName().tryCollapseToString())
        .setAvatarUrl(minotar(p, "helm", 128));
  }

  private static String minotar(Player p, String type, int size) {
    return String.format("https://minotar.net/%s/%s/%s.png", type, p.getUUID().toString(), size);
  }

  private static WebhookMessageBuilder getSystemHook() {
    return new WebhookMessageBuilder().setAllowedMentions(AllowedMentions.none());
  }

  private static final Pattern p = Pattern.compile(":\\w+:");

  public static String parseCustom(String in) {
    return p.matcher(in).replaceAll(match -> {
      String input = match.group();
      List<RichCustomEmoji> list = JDA.getEmojisByName(input.replaceAll(":", ""), false);
      if (list.size() > 0)
        return list.get(0).getAsMention();
      return input;
    });
  }

  public static void broadcastMessage(MinecraftServer server, String src, String text) {
    for (ServerPlayer p : server.getPlayerList().getPlayers()) {
      p.sendChatMessage(OutgoingChatMessage.create(PlayerChatMessage.system(text)), false,
          ChatType.bind(ChatType.CHAT, p.serverLevel().registryAccess(), Component.literal(src)));
    }
  }

  public static void sendPlayerText(Player p, String text) {
    WEBHOOK.send(getPlayerHook(p).setContent(text).build());
  }

  public static void sendPlayerText(Player p, String format, Object... args) {
    sendPlayerText(p, String.format(format, args));
  }

  public static void sendSystemEmbed(EmbedBuilder embed) {
    WEBHOOK.send(getSystemHook().addEmbeds(WebhookEmbedBuilder.fromJDA(embed.build()).build()).build());
  }

  public static void sendSystemText(String text) {
    WEBHOOK.send(getSystemHook().setContent(text).build());
  }

  public static void sendSystemText(String format, Object... args) {
    sendSystemText(String.format(format, args));
  }
}
