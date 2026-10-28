package dev.cloudsmp.weapons;

import org.bukkit.*;
import org.bukkit.command.*;
import org.bukkit.entity.*;
import org.bukkit.event.*;
import org.bukkit.event.entity.*;
import org.bukkit.event.inventory.*;
import org.bukkit.event.player.*;
import org.bukkit.inventory.*;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.*;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Vector;

import java.util.*;

public final class CloudSMPWeapons extends JavaPlugin implements Listener, CommandExecutor {
    private NamespacedKey weaponKey;
    private NamespacedKey explosiveArrowKey;
    private final Map<UUID, Long> cooldowns = new HashMap<>();
    private final Map<UUID, BukkitTask> rapidFireTasks = new HashMap<>();
    private final Map<UUID, Long> stunnedUntil = new HashMap<>();

    private enum Weapon {
        SKYBREAKER("skybreaker", Material.MACE, "§b§lSkybreaker", "§7Launch your enemy skyward,",
                "§7then slam them back into the ground.", "§bAbility: §fLaunch & Slam", "§bCooldown: §f8 seconds"),
        RAPIDFIRE("rapidfire", Material.CROSSBOW, "§e§lTempest Repeater", "§7Unleash a five-second",
                "§7storm of arrows.", "§eAbility: §f5s rapid fire", "§eCooldown: §f18 seconds"),
        DEMOLITION("demolition", Material.BOW, "§c§lCataclysm Bow", "§7Arrows erupt on impact.",
                "§7Terrain damage is disabled.", "§cAbility: §fExplosive arrows", "§cCooldown: §f1.2 seconds"),
        TITAN("titan", Material.NETHERITE_AXE, "§6§lTitan's Grasp", "§7Strike to briefly root",
                "§7your opponent in place.", "§6Ability: §f2s stun", "§6Cooldown: §f6 seconds"),
        GRAVITON("graviton", Material.TRIDENT, "§d§lGraviton Trident", "§7Hit an enemy with a thrown",
                "§7trident to drag them to you.", "§dAbility: §fGravity pull", "§dCooldown: §f7 seconds");

        final String id; final Material material; final String name; final String lore1; final String lore2; final String lore3; final String lore4;
        Weapon(String id, Material material, String name, String lore1, String lore2, String lore3, String lore4) {
            this.id=id; this.material=material; this.name=name; this.lore1=lore1; this.lore2=lore2; this.lore3=lore3; this.lore4=lore4;
        }
    }

    @Override public void onEnable() {
        weaponKey = new NamespacedKey(this, "weapon_id");
        explosiveArrowKey = new NamespacedKey(this, "cloudsmp_explosive_arrow");
        Objects.requireNonNull(getCommand("weapons")).setExecutor(this);
        Objects.requireNonNull(getCommand("weapon")).setExecutor(this);
        getServer().getPluginManager().registerEvents(this, this);
        getLogger().info("CloudSMP Weapons enabled. Use /weapons.");
    }

    private ItemStack makeWeapon(Weapon weapon) {
        ItemStack item = new ItemStack(weapon.material);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(weapon.name);
        meta.setLore(List.of("", weapon.lore1, weapon.lore2, "", weapon.lore3, weapon.lore4, "", "§8CloudSMP • Custom Weapon"));
        meta.getPersistentDataContainer().set(weaponKey, PersistentDataType.STRING, weapon.id);
        meta.setUnbreakable(true);
        item.setItemMeta(meta);
        return item;
    }

    private Weapon getWeapon(ItemStack item) {
        if (item == null || !item.hasItemMeta()) return null;
        String id = item.getItemMeta().getPersistentDataContainer().get(weaponKey, PersistentDataType.STRING);
        if (id == null) return null;
        for (Weapon weapon : Weapon.values()) if (weapon.id.equals(id)) return weapon;
        return null;
    }

    private boolean ready(Player player, Weapon weapon, long millis) {
        long now = System.currentTimeMillis();
        long next = cooldowns.getOrDefault(player.getUniqueId(), 0L);
        if (next > now) {
            player.sendActionBar(ComponentText.of("§c" + String.format(Locale.US, "%.1f", (next-now)/1000.0) + "s cooldown"));
            return false;
        }
        cooldowns.put(player.getUniqueId(), now + millis);
        return true;
    }

    private static final class ComponentText {
        static net.kyori.adventure.text.Component of(String text) {
            return net.kyori.adventure.text.Component.text(text.replaceAll("§[0-9a-fk-or]", ""));
        }
    }

    private void openMenu(Player player) {
        Inventory gui = Bukkit.createInventory(null, 27, "§8✦ §bCloudSMP §7| §fWeapon Vault");
        ItemStack filler = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
        ItemMeta fm = filler.getItemMeta(); fm.setDisplayName(" "); filler.setItemMeta(fm);
        for (int i=0;i<27;i++) gui.setItem(i, filler);
        int[] slots = {10, 12, 14, 16, 22};
        Weapon[] weapons = Weapon.values();
        for (int i=0;i<weapons.length;i++) {
            ItemStack display = makeWeapon(weapons[i]);
            ItemMeta meta = display.getItemMeta();
            List<String> lore = new ArrayList<>(meta.getLore());
            lore.add(""); lore.add("§aClick to receive"); lore.add("§8One item per click");
            meta.setLore(lore); display.setItemMeta(meta);
            gui.setItem(slots[i], display);
        }
        ItemStack info = new ItemStack(Material.NETHER_STAR);
        ItemMeta im = info.getItemMeta(); im.setDisplayName("§b§lCLOUDSMP ARSENAL");
        im.setLore(List.of("§7Five custom weapons.", "§7Choose one to claim it.", "", "§8PvP abilities • Cooldowns"));
        info.setItemMeta(im); gui.setItem(4, info);
        player.openInventory(gui);
    }

    @Override public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            if (command.getName().equalsIgnoreCase("weapon") && args.length == 1) {
                Weapon weapon = findWeapon(args[0]);
                if (weapon != null) { sender.sendMessage("Use this command in-game to receive " + weapon.id + "."); return true; }
            }
            sender.sendMessage("This command is for in-game players."); return true;
        }
        if (command.getName().equalsIgnoreCase("weapons")) { openMenu(player); return true; }
        if (!player.hasPermission("cloudsmpweapons.admin")) { player.sendMessage("§cYou don't have permission."); return true; }
        if (args.length != 1) { player.sendMessage("§bUsage: §f/weapon <mace|rapidcrossbow|explosivebow|stunaxe|pulltrident>"); return true; }
        Weapon weapon = findWeapon(args[0]);
        if (weapon == null) { player.sendMessage("§cUnknown weapon. Use /weapons to browse."); return true; }
        player.getInventory().addItem(makeWeapon(weapon)).values().forEach(item -> player.getWorld().dropItemNaturally(player.getLocation(), item));
        player.sendMessage("§bCloudSMP §8» §fReceived " + weapon.name + "§f.");
        return true;
    }

    private Weapon findWeapon(String input) {
        return switch (input.toLowerCase(Locale.ROOT)) {
            case "mace", "skybreaker" -> Weapon.SKYBREAKER;
            case "rapidcrossbow", "crossbow", "rapidfire" -> Weapon.RAPIDFIRE;
            case "explosivebow", "bow", "demolition" -> Weapon.DEMOLITION;
            case "stunaxe", "axe", "titan" -> Weapon.TITAN;
            case "pulltrident", "trident", "graviton" -> Weapon.GRAVITON;
            default -> null;
        };
    }

    @EventHandler public void onMenuClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;
        if (!event.getView().getTitle().contains("CloudSMP") || !event.getView().getTitle().contains("Weapon Vault")) return;
        event.setCancelled(true);
        int slot = event.getRawSlot();
        int[] slots = {10, 12, 14, 16, 22};
        Weapon[] weapons = Weapon.values();
        for (int i=0;i<slots.length;i++) if (slot == slots[i]) {
            player.getInventory().addItem(makeWeapon(weapons[i])).values().forEach(item -> player.getWorld().dropItemNaturally(player.getLocation(), item));
            player.playSound(player.getLocation(), Sound.ENTITY_ITEM_PICKUP, 0.8f, 1.35f);
            player.sendMessage("§bCloudSMP §8» §fClaimed " + weapons[i].name + "§f.");
            player.closeInventory(); return;
        }
    }

    @EventHandler public void onInteract(PlayerInteractEvent event) {
        Weapon weapon = getWeapon(event.getItem());
        if (weapon == null) return;
        Player player = event.getPlayer();
        if (weapon == Weapon.RAPIDFIRE && (event.getAction().isRightClick())) {
            event.setCancelled(true);
            if (!ready(player, weapon, 18000)) return;
            BukkitTask previous = rapidFireTasks.remove(player.getUniqueId());
            if (previous != null) previous.cancel();
            player.sendMessage("§eTempest Repeater §8» §fRapid fire engaged!");
            final int[] ticks = {0};
            BukkitTask task = Bukkit.getScheduler().runTaskTimer(this, () -> {
                if (!player.isOnline() || ticks[0] >= 50 || getWeapon(player.getInventory().getItemInMainHand()) != Weapon.RAPIDFIRE) {
                    rapidFireTasks.remove(player.getUniqueId()); return;
                }
                Arrow arrow = player.launchProjectile(Arrow.class);
                arrow.setVelocity(player.getEyeLocation().getDirection().multiply(3.0));
                arrow.setPickupStatus(AbstractArrow.PickupStatus.DISALLOWED);
                ticks[0] += 2;
            }, 0L, 2L);
            rapidFireTasks.put(player.getUniqueId(), task);
        }
    }

    @EventHandler public void onShoot(EntityShootBowEvent event) {
        if (!(event.getEntity() instanceof Player player)) return;
        Weapon weapon = getWeapon(event.getBow());
        if (weapon == Weapon.DEMOLITION && event.getProjectile() instanceof Arrow arrow) {
            arrow.getPersistentDataContainer().set(explosiveArrowKey, PersistentDataType.BYTE, (byte)1);
            arrow.setCritical(true);
        }
    }

    @EventHandler public void onProjectileHit(ProjectileHitEvent event) {
        Projectile projectile = event.getEntity();
        if (projectile.getPersistentDataContainer().has(explosiveArrowKey, PersistentDataType.BYTE)) {
            Location at = projectile.getLocation();
            World world = at.getWorld();
            if (world != null) {
                world.spawnParticle(Particle.EXPLOSION, at, 1);
                world.playSound(at, Sound.ENTITY_GENERIC_EXPLODE, 1.2f, 1.1f);
                for (Entity entity : world.getNearbyEntities(at, 3.0, 3.0, 3.0)) {
                    if (entity instanceof LivingEntity target && entity != projectile.getShooter()) {
                        double distance = target.getLocation().distance(at);
                        if (distance <= 3.0) target.damage(Math.max(1.0, 7.0 * (1.0-distance/3.0)), projectile.getShooter());
                    }
                }
                // Deliberately no block explosion: CloudSMP builds stay safe.
            }
            projectile.remove(); return;
        }
        if (projectile instanceof Trident trident && trident.getShooter() instanceof Player shooter
                && getWeapon(shooter.getInventory().getItemInMainHand()) == Weapon.GRAVITON
                && event.getHitEntity() instanceof LivingEntity target && target != shooter) {
            Vector pull = shooter.getLocation().toVector().subtract(target.getLocation().toVector());
            double distance = pull.length();
            if (distance > 0.1) {
                pull.normalize().multiply(Math.min(2.2, 0.75 + distance * 0.08)).setY(0.35);
                target.setVelocity(pull);
                target.getWorld().spawnParticle(Particle.PORTAL, target.getLocation().add(0,1,0), 24, 0.4,0.5,0.4,0.08);
                target.getWorld().playSound(target.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 0.7f, 1.5f);
            }
        }
    }

    @EventHandler public void onDamage(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Player attacker) || !(event.getEntity() instanceof LivingEntity target)) return;
        Weapon weapon = getWeapon(attacker.getInventory().getItemInMainHand());
        if (weapon == Weapon.SKYBREAKER && target instanceof Player victim) {
            if (!ready(attacker, weapon, 8000)) return;
            victim.setFallDistance(0);
            victim.setVelocity(new Vector(0.0, 1.85, 0.0));
            victim.getWorld().spawnParticle(Particle.CLOUD, victim.getLocation(), 28, 0.6,0.2,0.6,0.08);
            victim.getWorld().playSound(victim.getLocation(), Sound.ENTITY_WIND_CHARGE_WIND_BURST, 1.0f, 0.7f);
            Bukkit.getScheduler().runTaskLater(this, () -> {
                if (!victim.isOnline() || victim.isDead()) return;
                victim.setVelocity(new Vector(0.0, -2.8, 0.0));
                victim.setFallDistance(12.0f);
                victim.getWorld().spawnParticle(Particle.CRIT, victim.getLocation(), 18, 0.4,0.4,0.4,0.1);
            }, 18L);
        } else if (weapon == Weapon.TITAN && target instanceof Player victim) {
            if (!ready(attacker, weapon, 6000)) return;
            stunnedUntil.put(victim.getUniqueId(), System.currentTimeMillis()+2000);
            victim.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 40, 10, false, true, true));
            victim.addPotionEffect(new PotionEffect(PotionEffectType.JUMP_BOOST, 40, 128, false, false, false));
            victim.setVelocity(new Vector(0,0,0));
            victim.getWorld().spawnParticle(Particle.ELECTRIC_SPARK, victim.getLocation().add(0,1,0), 22, 0.5,0.6,0.5,0.02);
            victim.getWorld().playSound(victim.getLocation(), Sound.BLOCK_BEACON_DEACTIVATE, 0.8f, 1.6f);
            Bukkit.getScheduler().runTaskLater(this, () -> {
                if (victim.isOnline()) {
                    victim.removePotionEffect(PotionEffectType.SLOWNESS);
                    victim.removePotionEffect(PotionEffectType.JUMP_BOOST);
                    stunnedUntil.remove(victim.getUniqueId());
                }
            }, 40L);
        }
    }

    @EventHandler public void onMove(PlayerMoveEvent event) {
        Long until = stunnedUntil.get(event.getPlayer().getUniqueId());
        if (until == null || until < System.currentTimeMillis() || event.getTo() == null) return;
        if (event.getFrom().getX() != event.getTo().getX() || event.getFrom().getZ() != event.getTo().getZ()) {
            Location to = event.getTo().clone();
            to.setX(event.getFrom().getX()); to.setZ(event.getFrom().getZ());
            event.setTo(to);
        }
    }

    @EventHandler public void onQuit(PlayerQuitEvent event) {
        BukkitTask task = rapidFireTasks.remove(event.getPlayer().getUniqueId());
        if (task != null) task.cancel();
        stunnedUntil.remove(event.getPlayer().getUniqueId());
    }
}
